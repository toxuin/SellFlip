package ru.toxuin.sellflip;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.FontAwesomeText;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import java.io.File;

import ru.toxuin.sellflip.entities.Coordinates;
import ru.toxuin.sellflip.library.Utils;


public class CreateAdFragment extends Fragment {
    private static final String TAG = "CREATE_AD_FRAG";
    private static final int MAP_ACTIVITY_RESULT = 90;
    private View rootView;
    private Thread frameGrabberThread;
    private Handler frameGrabHandler;
    private String filename; // glued video

    public CreateAdFragment() {
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_createad, container, false);

        final TextView adTitle = (TextView) rootView.findViewById(R.id.adTitle);
        final EditText titleEdit = (EditText) rootView.findViewById(R.id.titleEdit);
        final ImageView adPic = (ImageView) rootView.findViewById(R.id.adPic);
        final ImageButton takeVideoBtn = (ImageButton) rootView.findViewById(R.id.takeVideoBtn);
        final Button locationSelectBtn = (Button) rootView.findViewById(R.id.location_select_btn);
        final SeekBar frameSeekBar = (SeekBar) rootView.findViewById(R.id.frameSeekBar);
        final RadioButton radioButtonFree = (RadioButton) rootView.findViewById(R.id.radioButtonFree);
        final RadioButton radioButtonContact = (RadioButton) rootView.findViewById(R.id.radioButtonContact);
        final EditText priceEdit = (EditText) rootView.findViewById(R.id.priceEdit);
        final FontAwesomeText backArrowBtn = (FontAwesomeText) rootView.findViewById(R.id.backArrowBtn);
        final FontAwesomeText nextArrowBtn = (FontAwesomeText) rootView.findViewById(R.id.nextArrowBtn);

        Bundle args = getArguments();
        filename = args.getString("filename");
        if (filename == null) {
            SuperToast superToast = new SuperToast(getActivity(), Style.getStyle(Style.RED, SuperToast.Animations.POPUP));
            superToast.setDuration(SuperToast.Duration.MEDIUM);
            superToast.setText("Error saving video. Please, try again");
            superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
            superToast.show();

            BaseActivity.setContent(new SearchResultFragment());
        }

        backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        radioButtonFree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    radioButtonContact.setChecked(false);
                    priceEdit.setText("");
                }
            }
        });

        radioButtonContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    radioButtonFree.setChecked(false);
                    priceEdit.setText("");
                }
            }
        });

        priceEdit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                radioButtonContact.setChecked(false);
                radioButtonFree.setChecked(false);
            }
        });

        takeVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        frameSeekBar.setMax((int) Utils.getVideoDuration(filename) * 1000);

        frameSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                frameGrabHandler = new Handler();
                frameGrabberThread = new Thread(new Runnable() {
                    @Override public void run() {
                        final Bitmap bmp = Utils.getVideoFrame(filename, progress);
                        frameGrabHandler.post(new Runnable() {
                            @Override public void run() {
                                adPic.setImageBitmap(bmp);
                            }
                        });
                    }
                });


            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {
                if (frameGrabberThread != null) {
                    frameGrabHandler.removeCallbacks(frameGrabberThread);
                    frameGrabberThread = null;
                }
            }

            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (frameGrabberThread != null) {

                    frameGrabberThread.start();
                }
            }
        });

        titleEdit.addTextChangedListener(new TextWatcher() {
            Boolean valid = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                valid = s.length() < 20;
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override public void afterTextChanged(Editable s) {
                if (valid && adTitle.length() != 0) {
                    adTitle.setText(s.toString());
                }
                if (adTitle.length() == 0) {
                    adTitle.setText("Ad Title");
                }

            }
        });

        locationSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapPopupActivity.class);
                startActivityForResult(intent, MAP_ACTIVITY_RESULT);
            }
        });

        String title = getString(R.string.create_ad);
        getActivity().setTitle(title);

        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case MAP_ACTIVITY_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    Coordinates coords = intent.getParcelableExtra("coords");
                    Log.d(TAG, "result: " + coords.getLat() + ", " + coords.getLng() + ", " + coords.getRadius());
                    // TODO: GET ADDRESS FROM COORDS
                }
                break;
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        new File(filename).delete();
    }
}
