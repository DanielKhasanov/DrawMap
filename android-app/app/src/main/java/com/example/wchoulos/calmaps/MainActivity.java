package com.example.wchoulos.calmaps;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static com.example.wchoulos.calmaps.R.layout.mainactivity;

public class MainActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Projection projection;
    public double latitude;
    public double longitude;
    Boolean Is_MAP_Moveable = false;
    private ArrayList<LatLng> val = new ArrayList<LatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mainactivity);
        FrameLayout fram_map = (FrameLayout) findViewById(R.id.fram_map);
        Button btn_draw_State = (Button) findViewById(R.id.btn_draw_State);
        setUpMapIfNeeded();
       // customMapFragment = (MySupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
       // mMap = customMapFragment.getMap();

        btn_draw_State.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Is_MAP_Moveable = !Is_MAP_Moveable;

            }
        });

        fram_map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //val = new ArrayList<LatLng>();
                float x = event.getX();
                float y = event.getY();

                int x_co = Math.round(x);
                int y_co = Math.round(y);

                projection = mMap.getProjection();
                Point x_y_points = new Point(x_co, y_co);

                LatLng latLng = mMap.getProjection().fromScreenLocation(x_y_points);
                latitude = latLng.latitude;

                longitude = latLng.longitude;

                int eventAction = event.getAction();
                switch (eventAction) {
                    case MotionEvent.ACTION_UP:
                        // finger leaves the screen
                        Log.v("Action", "Action Up");
                        if (Is_MAP_Moveable) {
                            val.add(new LatLng(latitude, longitude));
                        }
                        break;

                    case MotionEvent.ACTION_DOWN:
                        // finger touches the screen
                        Log.v("Action", "Action Down");
                        if (Is_MAP_Moveable) {
                            val.add(new LatLng(latitude, longitude));
                        }

                    case MotionEvent.ACTION_MOVE:
                        // finger moves on the screen
                        Log.v("Action", "Action Move");
                        if (Is_MAP_Moveable) {
                            val.add(new LatLng(latitude, longitude));
                            Draw_Map();
                        }




                }

                if (Is_MAP_Moveable == true) {
                    return true;

                } else {
                    return false;
                }
            }
        });
    }

    public void Draw_Map() {
        PolylineOptions rectOptions = new PolylineOptions();
        rectOptions.addAll(val);
        rectOptions.color(Color.BLUE);
        rectOptions.width(7);
        Polyline polyline = mMap.addPolyline(rectOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker on UC Berkeley Campus.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(37.8700, -122.2590)).title("Marker"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.8700, -122.2590), 14);
        mMap.animateCamera(cameraUpdate);
    }
}
