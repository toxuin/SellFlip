package ru.toxuin.sellflip;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import ru.toxuin.sellflip.library.Utils;


public class CreateAdFragment extends Fragment {
    private static final String TAG = "CREATE_AD_FRAG";
    Thread frameGrabberThread;
    private View rootView;
    private Handler saveFileHandler;
    public CreateAdFragment() {
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_createad, container, false);

        final TextView adTitle = (TextView) rootView.findViewById(R.id.adTitle);
        final EditText titleEdit = (EditText) rootView.findViewById(R.id.titleEdit);
        final ImageView adPic = (ImageView) rootView.findViewById(R.id.adPic);
        final ImageButton takeVideoBtn = (ImageButton) rootView.findViewById(R.id.takeVideoBtn);
        final SeekBar frameSeekBar = (SeekBar) rootView.findViewById(R.id.frameSeekBar);

        Bundle args = getArguments();
        final String filename = args.getString("filename");


        frameSeekBar.setMax((int) Utils.getVideoDuration(filename) * 1000);

        frameSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                saveFileHandler = new Handler();
                frameGrabberThread = new Thread(new Runnable() {
                    @Override public void run() {
                        final Bitmap bmp = Utils.getVideoFrame(filename, progress);
                        saveFileHandler.post(new Runnable() {
                            @Override public void run() {
                                adPic.setImageBitmap(bmp);
                            }
                        });
                    }
                });


            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {
                if (frameGrabberThread != null) {
                    saveFileHandler.removeCallbacks(frameGrabberThread);
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

            }
        });

        //TODO: remove
        takeVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
            }
        });

        String title = getString(R.string.create_ad);
        getActivity().setTitle(title);

        return rootView;
    }
}
