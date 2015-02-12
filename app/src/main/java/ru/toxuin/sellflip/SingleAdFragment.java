package ru.toxuin.sellflip;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;

import java.text.DateFormat;
import java.text.NumberFormat;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.LoadingCallback;
import ru.toxuin.sellflip.library.VideoControllerView;
import ru.toxuin.sellflip.restapi.ApiConnector;

public class SingleAdFragment extends Fragment implements
        SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        VideoControllerView.MediaPlayerControl {
    public static final String TAG = "SINGLE_AD_UI";
    public static final String VIDEO_URL = "http://nighthunters.ca/minecraft/TEST_VIDEO_PLEASE_IGNORE.mp4";

    private View rootView;
    private String adId;
    private SingleAd thisAd;

    // Video with controls
    private MediaPlayer player;
    private VideoControllerView controller;

    public SingleAdFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    /**
     * This is a constructor extension for chain-setting the id parameter
     * Use like this: new SingleAdFragment().setId("lalal")
     * @param id is that should be retrieved
     * @return same instance that would be returned with constructor
     */
    public SingleAdFragment setId(String id) {
        adId = id;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_singlead, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);

        ApiConnector api = ApiConnector.getInstance();

        if (adId == null) {
            throw new IllegalStateException("SingleAdFragment instantiated without id! Use .setId(\"lalal\")!");
        }

        final TextView adTitle = (TextView) rootView.findViewById(R.id.adTitle);
        final TextView adDescription = (TextView) rootView.findViewById(R.id.adDescription);
        final TextView adPrice = (TextView) rootView.findViewById(R.id.adPrice);
        final TextView adDate = (TextView) rootView.findViewById(R.id.adDate);

        final BootstrapButton contactEmailBtn = (BootstrapButton) rootView.findViewById(R.id.contact_mail_btn);
        final BootstrapButton contactPhoneBtn = (BootstrapButton) rootView.findViewById(R.id.contact_phone_btn);
        final BootstrapButton openMapBtn = (BootstrapButton) rootView.findViewById(R.id.mapButton);

        final SurfaceView videoSurface = (SurfaceView) rootView.findViewById(R.id.videoSurface);

        // SET UP VIDEO
        videoSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                controller.show();
                return false;
            }
        });
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);

        player = new MediaPlayer();
        controller = new VideoControllerView(getActivity());

        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(getActivity(), Uri.parse(VIDEO_URL));
            player.setOnPreparedListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        api.requestSingleAdForId(adId, new LoadingCallback<SingleAd>(getActivity()) {
            @Override
            public void onSuccess(final SingleAd ad, Response response) {
                thisAd = ad;
                Log.d(TAG, "GOT AD! " + ad.getId());
                BaseActivity.setContentTitle(ad.getTitle());

                // Set the fields
                adTitle.setText(ad.getTitle());
                adDescription.setText(ad.getDescription());

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
                            Intent intent = new Intent(getActivity(), MapPopupActivity.class);
                            intent.putExtra("coords", thisAd.getCoords());
                            intent.putExtra("title", thisAd.getTitle());
                            startActivity(intent);
                        }
                    });
                }

                if (ad.getPhone() != null && !ad.getPhone().equals("")) {
                    contactPhoneBtn.setVisibility(View.VISIBLE);
                    contactPhoneBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String uri = "tel:" + ad.getPhone().trim() ;
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
            public void onFailure(RetrofitError error) {
                Toast.makeText(getActivity(), "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
        player.prepareAsync();
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
        return player.getDuration();
    }

    @Override public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override public void seekTo(int pos) {
        player.seekTo(pos);
    }

    @Override public boolean isPlaying() {
        return player.isPlaying();
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
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) rootView.findViewById(R.id.videoSurfaceContainer));
        player.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
        }
    }
}
