package edu.virginia.cs4720.besafe;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, LocationListener, SensorEventListener {

    private GoogleMap mMap;

    private static final int GPS_PERMISSION = 1;
    private static final int REQUEST_PHONE_CALL = 1;

    SensorManager sensorManager;
    private Sensor mAccelerometer;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.00F;

    private Button btnFindPath;
    private List<Marker> searchMarkers = new ArrayList<>();
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    private LocationManager locationManager;
    private LatLng curLatLng;

    private boolean volumeKeyDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION }, GPS_PERMISSION);
        }

        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 10, this);
        }
        catch (SecurityException e) {
            Log.e("Error", e.getMessage());
        }

        //Accelerometer set up
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // set up first location
        Location firstLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        curLatLng = new LatLng(firstLoc.getLatitude(), firstLoc.getLongitude());
    }

    public void goBack(View view) {
        Intent goBack = new Intent();
        setResult(Activity.RESULT_CANCELED, goBack);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add a line to register the Session Manager Listener
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onPause() {
        // Add a line to unregister the Sensor Manager
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        //get the latitude and longitude from the location
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        curLatLng = new LatLng(latitude, longitude);
        if (mMap != null) {
            CameraPosition myPosition = new CameraPosition.Builder().target(curLatLng).zoom(17).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition));
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            volumeKeyDown = true;
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            volumeKeyDown = false;
        }
        return true;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nothing needs to be added here.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Add code here to handle what happens when a sensor event occurs.
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        Float f = new Float(gX * gX + gY * gY + gZ * gZ);
        Double d = Math.sqrt(f.doubleValue());
        float gForce = d.floatValue();

        if (gForce > SHAKE_THRESHOLD_GRAVITY && volumeKeyDown) {
            String msg = "Help! My current location is: (lat: " + curLatLng.latitude + ", lon: " + curLatLng.longitude + ")";
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String primaryNumber = extras.getString("primaryNumber");

                try {
                    SmsManager smsManager = SmsManager.getDefault();

                    smsManager.sendTextMessage(primaryNumber, null, msg, null, null);

                    Toast.makeText(MapsActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Toast.makeText(MapsActivity.this, "Message not Sent. " + "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ primaryNumber));
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                }
                startActivity(callIntent);
            }

            //Toast.makeText(this, "Emergency", Toast.LENGTH_SHORT).show();
        }

    }


    public void onSearch(View view) {
        EditText location_tf = (EditText) findViewById(R.id.TFaddress);
        String location = location_tf.getText().toString();
        List<Address> addressList = null;
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);


            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                for (Marker marker: searchMarkers) {
                    marker.remove();
                }
                searchMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng).title("Marker")));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
            else {
                Toast.makeText(this, "Please enter search address!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void onZoom(View view) {
        if (view.getId() == R.id.Bzoomin) {
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
        if (view.getId() == R.id.Bzoomout) {
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }

    public void changeType(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(mMap!=null){
                mMap.setMyLocationEnabled(true);

                Location myLocation = mMap.getMyLocation();
                if(myLocation !=null) {
                    curLatLng = new LatLng(myLocation.getLatitude(),
                            myLocation.getLongitude());
                }
            }


            if(mMap!=null){
                if (curLatLng != null) {
                    CameraPosition myPosition = new CameraPosition.Builder().target(curLatLng).zoom(17).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition));
                }
            }
        }
    }

    private void sendRequest() {
        String origin = ""+ curLatLng.latitude+", "+curLatLng.longitude;
        EditText location_tf = (EditText) findViewById(R.id.TFaddress);
        String destination = location_tf.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                progressDialog.cancel();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 10000);

        if (searchMarkers != null) {
            for (Marker marker : searchMarkers) {
                marker.remove();
            }
        }

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        //Toast.makeText(this, "success", Toast.LENGTH_LONG);


        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

//            originMarkers.add(mMap.addMarker(new MarkerOptions().title(route.startAddress).position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions().title(route.endAddress).position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().geodesic(true).color(Color.BLUE).width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }
}
