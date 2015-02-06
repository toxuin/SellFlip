package ru.toxuin.sellflip;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import ru.toxuin.sellflip.entities.Coordinates;

public class MapPopupActivity extends Activity {

    private static final String TAG = "MAP_UI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_popup);

        Coordinates coords = getIntent().getParcelableExtra("coords");
        if (coords == null) {
            throw new IllegalStateException("NO COORDINATES SET! USE setExtra!");
        }

        Log.d(TAG, "COORDINATES: " + coords.getLat() + " :: " + coords.getLng() + " :: " + coords.getRadius());
    }
}
