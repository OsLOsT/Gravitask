package com.sp.gravitask;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

public class ErrandMap extends FragmentActivity implements OnMapReadyCallback {

    Marker marker = null;
    SharedPreferences prefs = null;
    private GoogleMap mMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_errand_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.errand_map);
        mapFragment.getMapAsync(this);

        prefs = ErrandMap.this.getSharedPreferences("LatLng", MODE_PRIVATE);

        for (Map.Entry<String, ?> something : prefs.getAll().entrySet()) {
            Log.d("Key: ", something.getKey());
            Log.d("Value: ", something.getValue().toString());
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Move the camera to Singapore
        LatLng singapore = new LatLng(1.290270, 103.851959);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(singapore));

        //Check whether your preferences contains any values then we get those values
        if ((prefs.contains("Lat")) && (prefs.contains("Lng"))) {

            String lat = prefs.getString("Lat", "");
            String lng = prefs.getString("Lng", "");
            LatLng destination = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            Marker mdest = mMap.addMarker(new MarkerOptions().position(destination));
        }


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng point) {
                marker = mMap.addMarker(new MarkerOptions().position(point)
                        .snippet("Start")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_start2)));

                /* This code will save your location coordinates in SharedPrefrence when you click on the map and later you use it  */
                prefs.edit().putString("Lat",String.valueOf(point.latitude)).apply();
                prefs.edit().putString("Lng",String.valueOf(point.longitude)).apply();

                String lat = prefs.getString("Lat", "");
                String lng = prefs.getString("Lng", "");
                Toast.makeText(ErrandMap.this, lat + lng, Toast.LENGTH_LONG).show();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker arg0) {
                //Your marker removed
                arg0.remove();
                return true;
            }
        });

    }



}
