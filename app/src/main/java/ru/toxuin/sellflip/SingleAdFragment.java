package ru.toxuin.sellflip;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.SpiceFragment;
import ru.toxuin.sellflip.library.VideoControllerView;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;
import ru.toxuin.sellflip.restapi.spicerequests.SingleAdRequest;

public class SingleAdFragment extends SpiceFragment implements
        SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        VideoControllerView.MediaPlayerControl {
    public static final String TAG = "SINGLE_AD_UI";
    private static final int VIDEO_RESIZE = 666;
    public static String videoUrl;

    private View rootView;
    private String adId;
    private SingleAd thisAd;

    // Video with controls
    private MediaPlayer player;
    private VideoControllerView controller;

    private boolean playerReady = false;
    private boolean playerIsPreparing = false;
    protected SpiceManager spiceManager = new SpiceManager(SellFlipSpiceService.class);

    private TextView adTitle;
    private TextView adDescription;
    private TextView adPrice;
    private TextView adDate;
    private TextView adAddress;

    private BootstrapButton contactEmailBtn;
    private BootstrapButton contactPhoneBtn;
    private BootstrapButton openMapBtn;

    private SurfaceView videoSurface;
    private float ratio = 1;

    public SingleAdFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    /**
     * This is a constructor extension for chain-setting the id parameter
     * Use like this: new SingleAdFragment().setAdId("lalal")
     * @param id is that should be retrieved
     * @return same instance that would be returned with constructor
     */
    public SingleAdFragment setAdId(String id) {
        adId = id;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_singlead, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);

        if (adId == null) {
            throw new IllegalStateException("SingleAdFragment instantiated without id! Use .setAdId(\"lalal\")!");
        }

        adTitle = (TextView) rootView.findViewById(R.id.adTitle);
        adDescription = (TextView) rootView.findViewById(R.id.adDescription);
        adPrice = (TextView) rootView.findViewById(R.id.adPrice);
        adDate = (TextView) rootView.findViewById(R.id.adDate);
        adAddress = (TextView) rootView.findViewById(R.id.adAddress);

        contactEmailBtn = (BootstrapButton) rootView.findViewById(R.id.contact_mail_btn);
        contactPhoneBtn = (BootstrapButton) rootView.findViewById(R.id.contact_phone_btn);
        openMapBtn = (BootstrapButton) rootView.findViewById(R.id.mapButton);

        videoSurface = (SurfaceView) rootView.findViewById(R.id.videoSurface);

        return rootView;
    }

    @Override
    public void onStart() {
        // SET UP VIDEO
        videoSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                controller.show();
                return false;
            }
        });
        final SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);

        player = new MediaPlayer();
        controller = new VideoControllerView(getActivity());

        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            videoUrl = SellFlipSpiceService.getEndpointUrl() + "/api/v1/adsItems/" + adId + "/video";
            player.setDataSource(getActivity(), Uri.parse(videoUrl));
            player.setOnPreparedListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final ProgressDialog loading = new ProgressDialog(getActivity());
        loading.setTitle("Loading");
        loading.setIndeterminate(true);
        loading.setMessage("Wait while loading...");
        loading.show();

        SingleAdRequest request = new SingleAdRequest(adId);
        spiceManager.execute(request, request.getCacheKey(), DurationInMillis.ONE_MINUTE * 5, new RequestListener<SingleAd>() {
            @Override
            public void onRequestSuccess(final SingleAd ad) {
                thisAd = ad;

                // VIDEO SIZE ADJUST
                if (ad.getVideoWidth() > 0 && ad.getVideoHeight() > 0) {
                    ratio = (float) ad.getVideoWidth() / ad.getVideoHeight();
                    videoResizeHandler.sendEmptyMessage(VIDEO_RESIZE);
                }

                loading.dismiss();
                BaseActivity.setContentTitle(ad.getTitle());

                adTitle.setText(ad.getTitle());
                adDescription.setText(ad.getDescription());

                String addr = "Show me the map!";
                Geocoder geo = new Geocoder(getActivity());
                try {
                    Address address = geo.getFromLocation(ad.getCoords().getLat(), ad.getCoords().getLng(), 1).get(0);
                    ArrayList<String> addressFragments = new ArrayList<>();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));
                    }
                    addr = TextUtils.join(", ", addressFragments).trim();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                adAddress.setText("Close to: " + addr);
                adAddress.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openMap();
                    }
                });

                DateFormat dateFormat = DateFormat.getDateInstance();
                adDate.setText(dateFormat.format(ad.getDate()));

                if (ad.getPrice() == 0) {
                    adPrice.setText("Free");
                } else if (ad.getPrice() == -1) {
                    adPrice.setText("Please contact");
                } else {
                    NumberFormat formatter = NumberFormat.getCurrencyInstance();
                    adPrice.setText(formatter.format(ad.getPrice()));
                }

                if (ad.getCoords() != null) {
                    openMapBtn.setVisibility(View.VISIBLE);
                    openMapBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openMap();
                        }
                    });
                }

                if (ad.getPhone() != null && !ad.getPhone().equals("")) {
                    contactPhoneBtn.setVisibility(View.VISIBLE);
                    contactPhoneBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String uri = "tel:" + ad.getPhone().trim();
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse(uri));
                            startActivity(intent);
                        }
                    });
                }
                contactEmailBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", ad.getEmail(), null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "About your ad on SellFlip: " + ad.getTitle());
                        //emailIntent.putExtra(Intent.EXTRA_TEXT, "Sent from SellFlip, item \"" + ad.getTitle() + "\" (" + ad.getId() + ")");
                        startActivity(Intent.createChooser(emailIntent, "Send email"));
                    }
                });
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                loading.dismiss();
                Toast.makeText(getActivity(), "ERROR: " + spiceException.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        super.onStart();
    }

    private void openMap() {
        Intent intent = new Intent(getActivity(), MapPopupActivity.class);
        intent.putExtra("coords", thisAd.getCoords());
        intent.putExtra("title", thisAd.getTitle());
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "SAVING STATE! ID: " + adId);
        outState.putString("adId", adId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "TRYING TO RESTORE STATE!");
        if (savedInstanceState != null) {
            adId = savedInstanceState.getString("adId", null);
            Log.d(TAG, "GOT ID: " + adId);
        }
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
        if (!playerReady && !playerIsPreparing) {
            playerIsPreparing = true;
            player.prepareAsync();
        }
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override public void start() {
        player.start();
    }

    @Override public void pause() {
        player.pause();
    }

    @Override public int getDuration() {
        if (playerReady && !playerIsPreparing) return player.getDuration();
        return 0;
    }

    @Override public int getCurrentPosition() {
        if (playerReady && !playerIsPreparing) return player.getCurrentPosition();
        return 0;
    }

    @Override public void seekTo(int pos) {
        player.seekTo(pos);
    }

    @Override public boolean isPlaying() {
        if (playerReady && !playerIsPreparing) return player.isPlaying();
        return false;
    }

    @Override public int getBufferPercentage() {
        return 0;
    }

    @Override public boolean canPause() {
        return true;
    }

    @Override public boolean canSeekBackward() {
        return true;
    }

    @Override public boolean canSeekForward() {
        return true;
    }

    @Override public boolean isFullScreen() {
        return false;
    }

    @Override public void toggleFullScreen() {
        Intent intent = new Intent(getActivity(), FullScreenVideoActivity.class);
        intent.putExtra("position", player.getCurrentPosition());
        startActivity(intent);
    }

    @Override public void onPrepared(MediaPlayer mp) {
        playerReady = true;
        playerIsPreparing = false;
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) rootView.findViewById(R.id.videoSurfaceContainer));
        player.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null && playerReady) {
            player.stop();
            player.reset();
            player.release();
            playerReady = false;
            playerIsPreparing = false;
        }
    }

    @Override
    public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    Handler videoResizeHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what) {
                case VIDEO_RESIZE:
                    surfaceResize();
                    break;
            }
            return false;
        }
    });

    public void surfaceResize() {
        if (videoSurface == null || videoSurface.getLayoutParams() == null || ratio == 0) return;
        android.view.ViewGroup.LayoutParams surfaceParams = videoSurface.getLayoutParams();
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size); // TODO: THIS IS NAUGHTY
        surfaceParams.height = (int) ((1 / ratio) * (float) size.x);
        videoSurface.setLayoutParams(surfaceParams);
    }
}
