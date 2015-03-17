package ru.toxuin.sellflip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import ru.toxuin.sellflip.entities.Coordinates;
import ru.toxuin.sellflip.library.Utils;


public class CreateAdFragment extends Fragment {
    private static final String TAG = "CREATE_AD_FRAG";
    private static final int MAP_ACTIVITY_RESULT = 90;
    private View rootView;

    public CreateAdFragment() {
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_createad, container, false);

        final TextView adTitle = (TextView) rootView.findViewById(R.id.adTitle);
        final EditText titleEdit = (EditText) rootView.findViewById(R.id.titleEdit);
        final ImageView adPic = (ImageView) rootView.findViewById(R.id.adPic);
        final ImageButton takeVideoBtn = (ImageButton) rootView.findViewById(R.id.takeVideoBtn);
        final Button locationSelectBtn = (Button) rootView.findViewById(R.id.location_select_btn);

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
                adPic.setImageBitmap(Utils.getVideoFrame(Utils.videoName, 1000)); // Being chicky TODO: remove
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
}
