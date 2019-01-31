package com.sp.gravitask;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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

    Marker marker_start, marker_end = null;
    SharedPreferences prefs_start, prefs_end;
    private GoogleMap mMap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_errand_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.errand_map);
        mapFragment.getMapAsync(this);


        prefs_start = ErrandMap.this.getSharedPreferences("LatLng_start", MODE_PRIVATE);
        prefs_end = ErrandMap.this.getSharedPreferences("LatLng_end", MODE_PRIVATE);



        /*for (Map.Entry<String, ?> something : prefs_start.getAll().entrySet()) {
            Log.d("Key: ", something.getKey());
            Log.d("Value: ", something.getValue().toString());
        }
*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Move the camera to Singapore
        LatLng singapore = new LatLng(1.290270, 103.851959);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(singapore));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng start) {
                marker_start = mMap.addMarker(new MarkerOptions().position(start)
                        .snippet("Start")
                        .title("Start")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_start2)));

                /* This code will save your location coordinates in SharedPrefrence when you click on the map and later you use it  */
                prefs_start.edit().putString("Lat_start",String.valueOf(start.latitude)).apply();
                prefs_start.edit().putString("Lng_start",String.valueOf(start.longitude)).apply();

                String lat = prefs_start.getString("Lat_start", "");
                String lng = prefs_start.getString("Lng_start", "");

                Toast.makeText(ErrandMap.this, lat + lng + "start", Toast.LENGTH_LONG).show();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng end) {
                marker_end = mMap.addMarker(new MarkerOptions().position(end)
                        .snippet("Start")
                        .title("End")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_end2)));

                /* This code will save your location coordinates in SharedPrefrence when you click on the map and later you use it  */
                prefs_end.edit().putString("Lat_end", String.valueOf(end.latitude)).apply();
                prefs_end.edit().putString("Lng_end",String.valueOf(end.longitude)).apply();

                String lat = prefs_end.getString("Lat_end", "");
                String lng = prefs_end.getString("Lng_end", "");
                Toast.makeText(ErrandMap.this, lat + lng + "end", Toast.LENGTH_LONG).show();
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
