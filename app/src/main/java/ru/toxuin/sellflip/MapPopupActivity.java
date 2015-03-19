package ru.toxuin.sellflip;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.toxuin.sellflip.entities.Coordinates;

public class MapPopupActivity extends ActionBarActivity implements OnMapReadyCallback {
    private static final String TAG = "MAP_UI";
    private static final LatLng DEFAULT_MAP_CENTER = new LatLng(39.106355, -94.525074); // KANSAS CITY
    private static final float ALLOWED_MARKER_DRAG = 2000; // IN METERS
    private static final double MIN_CIRCLE_RADIUS = 60; // IN METERS. IF LESS, REMOVES CIRCLE
    private static final double MAX_CIRCLE_RADIUS = 8000; // IN METERS. IF MORE THEN THIS VALUE
    private static final double NEAR_MARKER_TOUCH_RADIUS = 40; // IN DP
    private static final double DEFAULT_CIRCLE_RADIUS = 300; // IN METERS

    public static FragmentManager fragmentManager;
    private FrameLayout mapZoomer;
    private GoogleMap map;
    private LocationManager locationManager;
    private MapPopupActivity self;

    private ScaleGestureDetector scaleDetector;
    private Location gpsLocation;
    private Coordinates coords;

    private String title;
    private Marker marker;
    private Circle markerMoveBoundaries;

    private Circle circle;
    private MenuItem nextBtn;

    private boolean isMapFrozen = false;
    private boolean knowsAboutRadius = false;

    private GoogleMap.OnMarkerDragListener markerDrag = new GoogleMap.OnMarkerDragListener() {
        private LatLng lastKnownPosition;

        @Override
        public void onMarkerDragStart(Marker marker) {
            lastKnownPosition = marker.getPosition();
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            float[] distance = new float[2];
            Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude,
                    gpsLocation.getLatitude(), gpsLocation.getLongitude(), distance);
            if (distance[0] > ALLOWED_MARKER_DRAG || gpsLocation == null) {
                marker.setPosition(lastKnownPosition);
                return;
            }
            lastKnownPosition = marker.getPosition();
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            lastKnownPosition = null;
            float[] distance = new float[2];
            Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude,
                    gpsLocation.getLatitude(), gpsLocation.getLongitude(), distance);
            if (distance[0] > ALLOWED_MARKER_DRAG || gpsLocation == null) {
                marker.setPosition(new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude()));
            }

            coords = new Coordinates((float) marker.getPosition().latitude, (float) marker.getPosition().longitude, 0);
        }
    };

    private final GoogleMap.OnInfoWindowClickListener removeRadius = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            marker.hideInfoWindow();
            if (circle == null) return;
            circle.remove();
            circle = null;
            // NO ACTION LISTENER
            mapZoomer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            });
        }
    };





    // BLUE CIRCLE STUFF
    private final ScaleGestureDetector.OnScaleGestureListener onScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {
        double radius = 1;

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            if (circle != null) {
                circle.setRadius(radius * scaleGestureDetector.getScaleFactor());
                if (circle.getRadius() > MAX_CIRCLE_RADIUS) {
                    circle.setRadius(MAX_CIRCLE_RADIUS);
                }
                return false;
            }
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            if (marker == null) return false;
            if (circle == null) {
                radius = DEFAULT_CIRCLE_RADIUS;
                addCircle(marker.getPosition(), (float) radius);
                return true;
            }
            radius = circle.getRadius();
            return isMapFrozen;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            isMapFrozen = false;
            if (circle == null) return;
            if (circle.getRadius() < MIN_CIRCLE_RADIUS) {
                circle.remove();
                circle = null;
                return;
            } else if (circle.getRadius() > MAX_CIRCLE_RADIUS) {
                circle.setRadius(MAX_CIRCLE_RADIUS);
            }
            coords.setRadius((float) circle.getRadius());
        }
    };

    private final View.OnTouchListener zoomTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            Point touched = new Point(Math.round(event.getX()), Math.round(event.getY()));
            Point markerPoint = map.getProjection().toScreenLocation(marker.getPosition());
            if (isNear(touched, markerPoint) || isInsideCircle(touched)) {
                isMapFrozen = true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) isMapFrozen = false;
            if (isMapFrozen) return scaleDetector.onTouchEvent(event);
            return false;
        }

        private boolean isInsideCircle(Point point) {
            if (map == null) return false;
            if (circle == null) return false;
            float[] distance = new float[2];
            LatLng latLng = map.getProjection().fromScreenLocation(point);
            Location.distanceBetween(latLng.latitude, latLng.longitude,
                    circle.getCenter().latitude, circle.getCenter().longitude, distance);
            return (distance[0] < circle.getRadius());
        }

        private boolean isNear(Point point1, Point point2) {
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) NEAR_MARKER_TOUCH_RADIUS, getResources().getDisplayMetrics());
            return Math.abs(point1.x - point2.x) < px && Math.abs(point1.y - point2.y) < px;
        }
    };

    private final GoogleMap.OnInfoWindowClickListener addRadius = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            if (circle != null) return;
            addCircle(marker.getPosition(), (float) DEFAULT_CIRCLE_RADIUS);
            marker.hideInfoWindow();
            mapZoomer.setOnTouchListener(zoomTouchListener);
        }
    };





    // GPS STUFF
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            handleLocationChange(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        setContentView(R.layout.activity_map_popup);
        mapZoomer = (FrameLayout) findViewById(R.id.map_zoom_layer);

        coords = getIntent().getParcelableExtra("coords");
        title = getIntent().getStringExtra("title");
        if (title == null || !getIntent().hasExtra("title")) {
            title = getString(R.string.determining_location);
        }

        setTitle(title);
        fragmentManager = getSupportFragmentManager();

        // HANDLES BLUE CIRCLE
        scaleDetector = new ScaleGestureDetector(self, onScaleListener);

        initMap();
    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_MAP_CENTER));

        if (coords != null) {
            // WE HAVE COORDINATES
            if (coords.getRadius() != 0) addCircle(coords.getLatLng(), coords.getRadius());
            addMarker(coords.getLatLng()).setTitle(title);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
        } else {
            // PICKING THE COORDINATES

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean enabledWiFi = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            String provider = locationManager.getBestProvider(new Criteria(), false);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                handleLocationChange(location);
            }
            if (enabledGPS) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                Log.d(TAG, "WAITING FOR SIGNAL FROM GPS...");
            } else if (enabledWiFi) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
                Log.d(TAG, "WAITING FOR SIGNAL FROM WIFI...");
            } else {
                Log.d(TAG, "NO LOCATION PROVIDERS ENABLED!!!");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }


        }
    }

    private Marker addMarker(LatLng latlng) {
        if (map == null) return null;
        if (marker == null) {
            marker = map.addMarker(new MarkerOptions()
                    .position(latlng)
                    .title("Your location")
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
        }
        return marker;
    }

    private Circle addCircle(LatLng latlng, float radius) {
        if (circle == null) {
            circle = map.addCircle(new CircleOptions()
                    .center(latlng)
                    .radius(radius)
                    .strokeColor(Color.BLUE)
                    .strokeWidth(1)
                    .fillColor(0x400069e0));
        }
        return circle;
    }

    private void toggleInfoWindow() {
        if (marker == null) return;
        if (marker.getTitle().startsWith("Add")) {
            marker.setTitle("Remove blue circle");
            map.setOnInfoWindowClickListener(removeRadius);
        } else {
            marker.setTitle("Add blue circle");
            map.setOnInfoWindowClickListener(addRadius);
        }
    }

    public void handleLocationChange(final Location location) {
        Log.d(TAG, "GOT LOCATION! ACCURACY: " + location.getAccuracy());
        if (gpsLocation != null) {
            if (gpsLocation.getAccuracy() < location.getAccuracy()) {
                gpsLocation = location;
                markerMoveBoundaries.setCenter(new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude()));
            }
        } else gpsLocation = location;
        locationManager.removeUpdates(locationListener);
        setTitle(getString(R.string.map_move_marker));
        final Runnable flymap = new Runnable() {
            public void run() {
                if (map == null || gpsLocation == null) return;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude()), 14));
            }
        };
        mapZoomer.postDelayed(flymap, 50);
        addMarker(new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude()));
        if (markerMoveBoundaries == null) {
            markerMoveBoundaries = map.addCircle(new CircleOptions()
                            .center(new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude()))
                            .radius(ALLOWED_MARKER_DRAG)
                            .strokeWidth(3)
                            .strokeColor(Color.GREEN)
            );
        }

        marker.setDraggable(true);
        map.setOnMarkerDragListener(markerDrag);
    }






    @Override
    public void onPause() {
        super.onPause();
        if (locationManager != null) locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager != null) {
            String provider = locationManager.getBestProvider(new Criteria(), false);
            locationManager.requestLocationUpdates(provider, 400, 1, locationListener);
        }
        initMap();
    }






    // MENU STUFF
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        if (coords == null) {
            nextBtn = menu.findItem(R.id.action_map_ready);
            nextBtn.setVisible(true);
            this.invalidateOptionsMenu();
        }
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
            case R.id.action_map_ready:
                if (coords == null) {
                    SuperToast superToast = new SuperToast(getBaseContext(), Style.getStyle(Style.RED, SuperToast.Animations.POPUP));
                    superToast.setDuration(SuperToast.Duration.VERY_SHORT);
                    superToast.setText("Select location to continue!");
                    superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                    superToast.show();
                    return false;
                } else if (coords.getRadius() == 0 && !knowsAboutRadius) {
                    SuperToast superToast = new SuperToast(getBaseContext(), Style.getStyle(Style.BLUE, SuperToast.Animations.POPUP));
                    superToast.setDuration(SuperToast.Duration.SHORT);
                    superToast.setText(getString(R.string.map_specify_radius));
                    superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                    superToast.show();
                    setTitle("Adjust approximation of your location");
                    knowsAboutRadius = true;
                    marker.setDraggable(false);
                    toggleInfoWindow();
                    marker.showInfoWindow();
                } else if (knowsAboutRadius) {
                    // TODO: SAVE LOCATION TO PREFS

                    if (circle != null) coords.setRadius((float) circle.getRadius());
                    Intent intent = new Intent();
                    intent.putExtra("coords", coords);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
