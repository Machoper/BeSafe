package edu.virginia.cs4720.besafe;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, SensorEventListener, LocationListener {

    private GoogleMap mMap;

    private FirebaseAuth firebaseAuth;

    SensorManager sensorManager;
    private Sensor mAccelerometer;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.00F;

    private LocationManager locationManager;
    private LatLng curLatLng;

    static final int LAUNCH = 1;
    static final int LOGGEDIN = 2;
    static final int NEW_DESTINATION = 3;
    static final int REPORT = 4;
    static final int CONTACT = 5;

    private static final int GPS_PERMISSION = 1;
    private static final int REQUEST_PHONE_CALL = 1;

    private String primaryNumber;
    private String primaryName;

    private boolean volumeKeyDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Start Launch Screen
        Intent i = new Intent(MainActivity.this, LaunchScreen.class);
        startActivityForResult(i, LAUNCH);

        // Set up drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Handle Permissions
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION }, GPS_PERMISSION);
        }

        //Accelerometer set up
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // set up GPS
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 10, this);
        }
        catch (SecurityException e) {
            Log.e("Error", e.getMessage());
        }

        Location firstLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        curLatLng = new LatLng(firstLoc.getLatitude(), firstLoc.getLongitude());

        firebaseAuth = FirebaseAuth.getInstance();
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
    public void onDestroy() {
        super.onDestroy();
        firebaseAuth.signOut();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onLocationChanged(Location location) {
        //get the latitude and longitude from the location
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        curLatLng = new LatLng(latitude, longitude);
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
            try {
                SmsManager smsManager = SmsManager.getDefault();
                String msg = "Help! My current location is: (lat: " + curLatLng.latitude + ", lon: " + curLatLng.longitude + ")";
                smsManager.sendTextMessage(primaryNumber, null, msg, null, null);
                Toast.makeText(MainActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
            }
            catch (Exception e) {
                Toast.makeText(MainActivity.this, "Message not Sent. " + "Error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + primaryNumber));
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
            }
            else {
                startActivity(callIntent);
            }

            //Toast.makeText(this, "Emergency", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_profile) {
            Intent i = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_contacts) {
            Intent i = new Intent(MainActivity.this, ContactActivity.class);
            startActivityForResult(i, CONTACT);

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(com.google.android.gms.maps.GoogleMap googleMap) {
        mMap = googleMap;
//        if ( Build.VERSION.SDK_INT >= 23 &&
//                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION }, GPS_PERMISSION);
//        }

        if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }

            if (mMap != null) {
                Location myLocation = mMap.getMyLocation();


                if (myLocation != null) {
                    LatLng myLatLng = new LatLng(myLocation.getLatitude(),
                            myLocation.getLongitude());


                    CameraPosition myPosition = new CameraPosition.Builder()
                            .target(myLatLng).zoom(17).bearing(90).tilt(30).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition));
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case(LAUNCH): {
                if (resultCode == Activity.RESULT_OK) {
                    Intent i = new Intent(MainActivity.this, LogInActivity.class);
                    startActivityForResult(i, LOGGEDIN);
                }
                else {
                }
                break;
            }
            case(NEW_DESTINATION): {
                if (resultCode == Activity.RESULT_OK) {

                }
                else {

                }
                break;
            }
            case(REPORT): {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Report submitted!", Toast.LENGTH_LONG).show();
                }
                else {

                }
                break;
            }
            case(CONTACT): {
                if (resultCode == Activity.RESULT_OK) {
                    primaryName = data.getStringExtra("primaryName");
                    primaryNumber = data.getStringExtra("primaryNumber");
                    TextView pName = (TextView) findViewById(R.id.primaryName);
                    TextView pNumber = (TextView) findViewById(R.id.primaryNumber);
                    pName.setText(primaryName);
                    pNumber.setText(primaryNumber);
                }
                else {
                }
                break;
            }
        }
    }

    // report button
    public void createReport(View view) {
        Intent i = new Intent(MainActivity.this, ReportActivity.class);
        startActivityForResult(i, REPORT);
    }

    // start trip button
    public void startTrip(View view) {
        Intent i = new Intent(MainActivity.this, MapsActivity.class);
        i.putExtra("primaryNumber", primaryNumber);
        startActivityForResult(i, NEW_DESTINATION);
    }

//    public void manageContacts(View view) {
//        Intent i = new Intent(MainActivity.this, ContactActivity.class);
//        startActivityForResult(i, CONTACT);
//    }

}
