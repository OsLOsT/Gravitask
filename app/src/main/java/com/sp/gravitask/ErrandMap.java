package com.sp.gravitask;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
//import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koalap.geofirestore.GeoFire;
import com.koalap.geofirestore.GeoLocation;
import com.koalap.geofirestore.GeoQuery;
import com.koalap.geofirestore.GeoQueryDataEventListener;
import com.koalap.geofirestore.GeoQueryEventListener;

import java.util.HashMap;
import java.util.Random;

public class ErrandMap extends FragmentActivity implements OnMapReadyCallback, SensorEventListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Marker marker_start, marker_end;
    private SharedPreferences prefs_start, prefs_end;
    private GoogleMap mMap;
    private Double lat_start, lng_start, lat_end, lng_end;
    private LatLng START, END;
    private float[] mRotationMatrix = new float[16];
    private float mDeclination;
    private SensorManager mSensorManager;
    private Sensor mRotVectSensor;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private CollectionReference errandsRef, locationsRef;
    private GeoFire geoFire;
    private String docId, geoStart, geoEnd;
    private static final String TAG = ErrandMap.class.getName();
    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 300193;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String uid;
    private SharedPreferences.Editor startEdit, endEdit;
    private GeoQuery geoQueryStart, geoQueryEnd;

    private static int UPDATE_INTERVAL = 5000; //5 seconds
    private static int FASTEST_INTERVAL = 3000; //3 seconds
    private static int DISPLACEMENT = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_errand_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.errand_map);
        mapFragment.getMapAsync(this);

        //Get Firebase firestore reference
        db = FirebaseFirestore.getInstance();

        //Collection reference for the Errands
        errandsRef = db.collection("Errands");


        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //Get Firebase current user
        user = auth.getCurrentUser();


        locationsRef = db.collection("Locations");
        geoFire = new GeoFire(locationsRef);


        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mRotVectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        prefs_start = ErrandMap.this.getSharedPreferences("LatLng_start", MODE_PRIVATE);
        prefs_end = ErrandMap.this.getSharedPreferences("LatLng_end", MODE_PRIVATE);

        lat_start = getIntent().getDoubleExtra("Lat_start", 0);
        lng_start = getIntent().getDoubleExtra("Lng_start", 0);

        lat_end = getIntent().getDoubleExtra("Lat_end", 0);
        lng_end = getIntent().getDoubleExtra("Lng_end", 0);

        startEdit = prefs_start.edit();
        endEdit = prefs_end.edit();

        uid = auth.getUid();

        geoStart = uid + "Start";
        geoEnd = uid + "End";

        setUpLocation();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //MYPOSITION = new LatLng(myLat, myLon);
        START = new LatLng(lat_start, lng_start);
        END = new LatLng(lat_end, lng_end);


        mMap.setMyLocationEnabled(true);

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MYPOSITION, 15));

        /*ME = mMap.addMarker(new MarkerOptions().position(MYPOSITION)
                .snippet("ME")
                .title("ME")
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_maker)));*/


        if (START.equals(END)) {

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng start) {
                    marker_start = mMap.addMarker(new MarkerOptions().position(start)
                            .snippet("Start")
                            .title("Start")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_start2)));

                    /* This code will save your location coordinates in SharedPrefrence when you click on the map and later you use it  */
                    startEdit.putString("Lat_start", String.valueOf(start.latitude)).apply();
                    startEdit.putString("Lng_start", String.valueOf(start.longitude)).apply();

                    String lat = prefs_start.getString("Lat_start", "");
                    String lng = prefs_start.getString("Lng_start", "");

                    START = new LatLng(lat_start, lng_start);

                    Toast.makeText(ErrandMap.this, lat + lng + "start", Toast.LENGTH_LONG).show();

                    geoFire.setLocation(geoStart, new GeoLocation(START.latitude, START.longitude), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, Exception exception) {
                            //Add GeoQuery
                            //0.02f = 0.0
                            // .02km = 20m
                            geoQueryStart = geoFire.queryAtLocation(new GeoLocation(START.latitude, START.longitude), 0.02f);
                            geoQueryStart.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    sendNotification("User", String.format("%s there is a task near you. Go find it!", key));
                                }

                                @Override
                                public void onKeyExited(String key) {
                                    sendNotification("User", String.format("%s , you have gave up finding the task?", key));
                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {
                                    Log.d("MOVE", String.format("%s near the task [%f/%f]", key, location.latitude, location.longitude));

                                }

                                @Override
                                public void onGeoQueryReady() {

                                }

                                @Override
                                public void onGeoQueryError(Exception error) {
                                    Log.e("ERROR START", "" + error);

                                }
                            });

                        }
                    });

                }
            });

            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng end) {
                    marker_end = mMap.addMarker(new MarkerOptions().position(end)
                            .snippet("End")
                            .title("End")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_end2)));

                    /* This code will save your location coordinates in SharedPrefrence when you click on the map and later you use it  */
                    endEdit.putString("Lat_end", String.valueOf(end.latitude)).apply();
                    endEdit.putString("Lng_end", String.valueOf(end.longitude)).apply();

                    String lat = prefs_end.getString("Lat_end", "");
                    String lng = prefs_end.getString("Lng_end", "");
                    Toast.makeText(ErrandMap.this, lat + lng + " end", Toast.LENGTH_LONG).show();

                    END = new LatLng(lat_end, lng_end);

                    geoQueryEnd = geoFire.queryAtLocation(new GeoLocation(END.latitude, END.longitude), 0.005f);
                    geoQueryEnd.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            sendNotification("User", String.format("%s you are approaching your end goal", key));

                        }

                        @Override
                        public void onKeyExited(String key) {

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {

                        }

                        @Override
                        public void onGeoQueryError(Exception error) {
                            Log.e("ERROR END", "" + error);
                        }
                    });

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

        } else {

            MarkerOptions mpStart = new MarkerOptions();

            mpStart.position(START);
            mpStart.title("Start");
            mpStart.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_start2));
            mMap.addMarker(mpStart);

            MarkerOptions mpEnd = new MarkerOptions();

            mpEnd.position(END);
            mpEnd.title("End");
            mpEnd.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_end2));
            mMap.addMarker(mpEnd);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;

        }
    }

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {

            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            //Update to firebase
            geoFire.setLocation(uid, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, Exception exception) {
                    Toast.makeText(ErrandMap.this, "GEOLOCATION SET FOR UID", Toast.LENGTH_SHORT).show();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12.0f));
                }
            });

            Log.d("OsLOsT", String.format("Your location was changed: %f / %f", latitude, longitude));
        } else {
            Log.d("OsLOsT", "Cannot get your location");
        }

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else {
                Toast.makeText(this, "Device not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void sendNotification(String title, String content) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_task)
                .setContentTitle(title)
                .setContentText(content);

        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, ErrandMap.class);
        PendingIntent contentIntent = (PendingIntent) PendingIntent.getActivity(this,0 ,intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        manager.notify(new Random().nextInt(),notification);

    }

    @Override
    public void onLocationChanged(Location location) {
        GeomagneticField field = new GeomagneticField(
                (float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                System.currentTimeMillis()
        );
        mLastLocation = location;
        displayLocation();

        // getDeclination returns degrees
        mDeclination = field.getDeclination();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
            try {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(
                        mRotationMatrix, event.values);
                float[] orientation = new float[3];
                SensorManager.getOrientation(mRotationMatrix, orientation);
                    float bearing = (float) Math.toDegrees(orientation[0]) + mDeclination;
                    updateCamera(bearing);
            }
        }catch(Exception e) {

        }
    }

    private void updateCamera(float bearing) {
        CameraPosition oldPos = mMap.getCameraPosition();

        CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing-45).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));

    }



    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mRotVectSensor, SensorManager.SENSOR_STATUS_ACCURACY_LOW);

    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();

    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
