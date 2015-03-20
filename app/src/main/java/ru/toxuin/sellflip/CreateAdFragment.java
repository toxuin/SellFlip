package ru.toxuin.sellflip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.FontAwesomeText;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import ru.toxuin.sellflip.entities.Coordinates;
import ru.toxuin.sellflip.library.TaggingAdapter;
import ru.toxuin.sellflip.library.Utils;


public class CreateAdFragment extends Fragment {
    private static final String TAG = "CREATE_AD_FRAG";
    private static final int MAP_ACTIVITY_RESULT = 90;
    private View rootView;
    private Thread frameGrabberThread;
    private Handler frameGrabHandler;
    private String filename; // glued video

    private float price;
    private Coordinates coord;

    private Button locationSelectBtn;
    private TaggingAdapter<Coordinates> locationAdapter;
    
    public CreateAdFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_createad, container, false);

        final TextView adTitle = (TextView) rootView.findViewById(R.id.adTitle);
        final EditText titleEdit = (EditText) rootView.findViewById(R.id.titleEdit);
        final ImageView adPic = (ImageView) rootView.findViewById(R.id.adPic);
        final ImageButton takeVideoBtn = (ImageButton) rootView.findViewById(R.id.takeVideoBtn);
        final RadioButton freeRadioBtn = (RadioButton) rootView.findViewById(R.id.radioButtonFree);
        final RadioButton contactRadioBtn = (RadioButton) rootView.findViewById(R.id.radioButtonContact);
        final EditText descriptionEdit = (EditText) rootView.findViewById(R.id.create_description);
        locationSelectBtn = (Button) rootView.findViewById(R.id.create_location_btn);
        final SeekBar frameSeekBar = (SeekBar) rootView.findViewById(R.id.frameSeekBar);
        final EditText priceEdit = (EditText) rootView.findViewById(R.id.priceEdit);
        final FontAwesomeText backArrowBtn = (FontAwesomeText) rootView.findViewById(R.id.backArrowBtn);
        final FontAwesomeText nextArrowBtn = (FontAwesomeText) rootView.findViewById(R.id.nextArrowBtn);

        Bundle args = getArguments();
        filename = args.getString("filename");
        if (filename == null) {
            SuperToast superToast = new SuperToast(getActivity(), Style.getStyle(Style.RED, SuperToast.Animations.POPUP));
            superToast.setDuration(SuperToast.Duration.MEDIUM);
            superToast.setText(getString(R.string.create_save_video_error));
            superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
            superToast.show();

            BaseActivity.setContent(new SearchResultFragment());
        }

        backArrowBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        takeVideoBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.length() > 50) titleEdit.setError(getString(R.string.create_title_too_long));
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.length() == 0) titleEdit.setError(getString(R.string.create_title_empty));
            }
        });

        priceEdit.addTextChangedListener(new TextWatcher() {
            String oldValue = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) oldValue = s.toString();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();

                // SOMETHING HAS CHANGED
                if (oldValue != null && !input.equals(oldValue)) {
                    freeRadioBtn.setChecked(false);
                    contactRadioBtn.setChecked(false);
                }
                if (adTitle.length() == 0) {
                    adTitle.setText("Ad Title");
                }

                // CHECK IF LINE OF ZEROES
                int zeroCount = input.length() - input.replace("0", "").length();
                if (zeroCount == input.length()) {
                    freeRadioBtn.setChecked(true);
                }
            }
        });


        /*
        PRICE LOGIC:
            FREE/CONTACT RADIOS HAS HIGHER PRECEDENCE THAN PRICE INPUT.
                MEANS: IF USER ENTERS PRICE AND SELECTS FREE – IT IS FREE
         */

        locationAdapter = new TaggingAdapter<>(getActivity());
        locationSelectBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setAdapter(locationAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (!locationAdapter.isEmpty() && locationAdapter.getItem(position).second == null) {
                                    openMapSelectActivity();
                                } else {
                                    coord = locationAdapter.getItemContents(position);
                                    locationSelectBtn.setText(locationAdapter.getItem(position).first);
                                }
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
        repopulateLocations();

        getActivity().setTitle(getString(R.string.create_ad));

        return rootView;
    }

    private void repopulateLocations() {
        if (locationAdapter == null) return;
        locationAdapter.clear();
        SharedPreferences sPref = getActivity().getSharedPreferences(getString(R.string.location_preference_key), Context.MODE_PRIVATE);
        Set<String> savedLocations = sPref.getStringSet("SAVED_LOCATIONS_KEYS", null);

        // IF HAS SAVED LOCATIONS
        if (sPref.contains("SAVED_LOCATIONS_KEYS") && savedLocations != null && savedLocations.size() > 0) {
            for (String key : savedLocations) {
                Coordinates coord = Utils.getCoordinatesFromPreferences(getActivity(), key);
                String name = sPref.getString("LOCATION_NAME_" + key, "No name");
                locationAdapter.add(new Pair<>(name, coord));
            }
        }

        // IF HAS coord
        if (coord != null) {
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses;
            String address = "Your location";
            try {
                addresses = geocoder.getFromLocation(coord.getLat(), coord.getLng(), 1);
                if (addresses != null && addresses.size() > 0) address = addresses.get(0).getPostalCode();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Pair <String, Coordinates> coordPair = new Pair<>(address, coord);
                locationAdapter.add(coordPair);
            }
        }

        locationAdapter.add(new Pair<>(getString(R.string.create_new_location), null));
    }

    private void openMapSelectActivity() {
        Intent intent = new Intent(getActivity(), MapPopupActivity.class);
        startActivityForResult(intent, MAP_ACTIVITY_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case MAP_ACTIVITY_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    coord = intent.getParcelableExtra("coords");
                    Log.d(TAG, "result: " + coord.getLat() + ", " + coord.getLng() + ", " + coord.getRadius());
                    repopulateLocations();
                }
                break;
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        new File(filename).delete();
    }
}
