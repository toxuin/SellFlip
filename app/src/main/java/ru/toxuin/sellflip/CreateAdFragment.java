package ru.toxuin.sellflip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import java.io.File;
import android.widget.SeekBar;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.FontAwesomeText;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import java.io.File;

import ru.toxuin.sellflip.entities.Coordinates;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.Utils;
import ru.toxuin.sellflip.restapi.ApiConnector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class CreateAdFragment extends Fragment {
    private static final String TAG = "CREATE_AD_FRAG";
    private static final int MAP_ACTIVITY_RESULT = 90;
    private View rootView;
    private Thread frameGrabberThread;
    private Handler frameGrabHandler;
    private String filename; // glued video

    private float price;
    private Coordinates coord;

    private Spinner locationSpinner;
    private ArrayAdapter<String> locationAdapter;

    private Map<String, Pair<Coordinates, String>> knownCoordinates;
    
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
        final EditText priceEdit = (EditText) rootView.findViewById(R.id.create_price_edit);
        final EditText descriptionEdit = (EditText) rootView.findViewById(R.id.create_description);
        locationSpinner = (Spinner) rootView.findViewById(R.id.create_location_spinner);
        final BootstrapButton postBtn = (BootstrapButton) rootView.findViewById(R.id.create_post_btn);
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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.length() > 50) titleEdit.setError("Title is too long! 50 letters max.");
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.length() == 0) titleEdit.setError("Title can not be empty!");
            }
        });

        priceEdit.addTextChangedListener(new TextWatcher() {
            String oldValue = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) oldValue = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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


        //TODO: remove
        takeVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
            }
        });
        //TODO: remove

        redrawLocationSpinner();

        getActivity().setTitle(getString(R.string.create_ad));

        postBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ApiConnector.getInstance().createNewAd(
                        new SingleAd("NEW AD", titleEdit.getText().toString(), price, null, null, "CATEGORY!!!", descriptionEdit.getText().toString(), coord, null),
                        new Callback<Void>() {
                            @Override
                            public void success(Void nothing, Response response) {
                                Log.d(TAG, "POSTED NEW AD!");
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.d(TAG, "COULD NOT POST NEW AD!");
                            }
                        }
                );
            }
        });

        String title = getString(R.string.create_ad);
        getActivity().setTitle(title);

        return rootView;
    }

    private void redrawLocationSpinner() {
        if (locationSpinner == null) return;
        knownCoordinates = new HashMap<>();

        final Map<Integer, String> positionToKey = new HashMap<>();

        SharedPreferences sPref = getActivity().getSharedPreferences(getString(R.string.location_preference_key), Context.MODE_PRIVATE);
        Set<String> savedLocations = sPref.getStringSet("SAVED_LOCATIONS_KEYS", null);

        // IF HAS SAVED LOCATIONS
        if (sPref.contains("SAVED_LOCATIONS_KEYS") && savedLocations != null && savedLocations.size() > 0) {
            for (String key : savedLocations) {
                Coordinates coord = Utils.getCoordinatesFromPreferences(getActivity(), key);
                String name = sPref.getString("LOCATION_NAME_" + key, "No name");
                knownCoordinates.put(key, new Pair<>(coord, name));
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
                knownCoordinates.put("ANONYMOUS", new Pair<>(coord, address));
            }
        }

        locationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "POSITION: " + position + ", " + parent.getCount());
                if (position == parent.getCount() - 1) { // LAST ONE
                    openMapSelectActivity();
                } else {
                    if (positionToKey.isEmpty()) return;
                    coord = knownCoordinates.get(positionToKey.get(position)).first;
                    Log.d(TAG, "SELECTED " + coord);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        if (locationAdapter == null) locationAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        else locationAdapter.clear();

        // FILL POSITION MAP
        Iterator<String> iterator = knownCoordinates.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            positionToKey.put(i++, iterator.next());
        }

        // LOAD EVERYTHING FROM knownLocations TO ADAPTER
        for (Integer position : positionToKey.keySet()) {
            String key = positionToKey.get(position);
            locationAdapter.insert(knownCoordinates.get(key).second, position);
        }

        locationAdapter.add(getString(R.string.create_new_location));
        locationSpinner.setVisibility(View.VISIBLE);

        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (locationSpinner.getAdapter() == null) locationSpinner.setAdapter(locationAdapter);
        locationAdapter.notifyDataSetChanged();

        if (coord != null) {
            locationSpinner.setSelection(0);
        }
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
                    redrawLocationSpinner();
                }
                break;
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        new File(filename).delete();
    }
}
