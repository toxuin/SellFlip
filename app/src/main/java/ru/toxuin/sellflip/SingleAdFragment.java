package ru.toxuin.sellflip;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.FontAwesomeText;

import java.text.DateFormat;
import java.text.NumberFormat;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.LoadingCallback;
import ru.toxuin.sellflip.restapi.ApiConnector;

public class SingleAdFragment extends Fragment implements SurfaceTextureListener {
    public static final String TAG = "SINGLE_AD_UI";
    public static final String VIDEO_URL = "http://nighthunters.ca/minecraft/TEST_VIDEO_PLEASE_IGNORE.mp4";

    private View rootView;
    private String adId;
    private SingleAd thisAd;
    private MediaPlayer mediaPlayer;

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

        final FontAwesomeText play_icon = (FontAwesomeText) rootView.findViewById(R.id.play_icon);
        final BootstrapButton openMapBtn = (BootstrapButton) rootView.findViewById(R.id.mapButton);
        final TextureView textureView = (TextureView) rootView.findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        //FLASHING BUTTON
        final Animation animAlpha = AnimationUtils.loadAnimation(getActivity(), R.anim.button_alpha_anim);
        play_icon.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    play_icon.startAnimation(animAlpha);
                    play_icon.setTextColor(getResources().getColor(R.color.bbutton_default_pressed));
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // play_icon.setAlpha(1f);
                    play_icon.setTextColor(getResources().getColor(R.color.bbutton_inverse));
                }
                return true;
            }
        });
        api.requestSingleAdForId(adId, new LoadingCallback<SingleAd>(getActivity()) {
            @Override
            public void onSuccess(SingleAd ad, Response response) {
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
            }

            @Override
            public void onFailure(RetrofitError error) {
                Toast.makeText(getActivity(), "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Surface surface = new Surface(surfaceTexture);

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(rootView.getContext().getApplicationContext(), Uri.parse(VIDEO_URL));
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
//                    mediaPlayer.start();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
