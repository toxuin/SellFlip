package ru.toxuin.sellflip;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import ru.toxuin.sellflip.entities.Coordinates;

public class MapPopupActivity extends ActionBarActivity implements OnMapReadyCallback {
    private static final String TAG = "MAP_UI";
    private static final LatLng DEFAULT_MAP_CENTER = new LatLng(39.106355, -94.525074); // KANSAS CITY
    public static FragmentManager fragmentManager;
    private GoogleMap map;
    private MapPopupActivity self;
    private Coordinates coords;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        setContentView(R.layout.activity_map_popup);

        coords = getIntent().getParcelableExtra("coords");
        title = getIntent().getStringExtra("title");

        if (coords == null || !getIntent().hasExtra("coords")) {
            throw new IllegalStateException("NO COORDINATES SET! USE setExtra!");
        }
        if (title == null || !getIntent().hasExtra("title")) {
            throw new IllegalStateException("NO AD TITLE SET! USE setExtra!");
        }

        setTitle(title);
        fragmentManager = getSupportFragmentManager();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_MAP_CENTER));

        //Log.d(TAG, "COORDINATES: " + coords.getLat() + " : " + coords.getLng() + ", R: " + coords.getRadius());

        if (coords.getRadius() != 0) {
            map.addCircle(new CircleOptions()
                    .center(coords.getLatLng())
                    .radius(coords.getRadius())
                    .strokeColor(Color.BLUE)
                    .strokeWidth(1)
                    .fillColor(0x400069e0));
        }
        map.addMarker(new MarkerOptions()
                .position(coords.getLatLng())
                .title(title)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(coords.getLatLng(), 14));
    }


    // MENU STUFF
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map_sattelite:
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.action_map_terrain:
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.action_map_hybrid:
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.action_map_normal:
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
