package com.example.fyp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fyp.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Qibla extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    String User;
    DatabaseReference referenceProfile;
    private Compass compass;
    private ImageView compassOuter, compassDegree, compassArrow;
    private TextView textCity, textDegree;
    private ImageButton btnDeveloper, btnJadwal;
    private float currentAzimuth;
    private String city;
    SharedPreferences prefs;
    GPSTracker gps;
    Geocoder geocoder;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qibla);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        prefs = getSharedPreferences("", MODE_PRIVATE);
        gps = new GPSTracker(this);

        compassOuter = (ImageView) findViewById(R.id.compassOuter);
        compassDegree = (ImageView) findViewById(R.id.compassDegree);
        compassArrow = (ImageView) findViewById(R.id.compassArrow);

        textCity = (TextView) findViewById(R.id.textCity);
        textDegree = (TextView) findViewById(R.id.textDegree);

        setupCompass();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, proceed with your operations
            // Call your method to access the location here
            fetchGPS();
        }


        String qiblaDeg = Math.round(GetFloat("qibla_degree")) + "°";

        textCity.setText(city);
        textDegree.setText(qiblaDeg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if the permission request is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with your operations
                // Call your method to access the location here
            } else {
                fetchGPS();
                // Permission is denied, handle accordingly
                // You may inform the user or disable location-related functionality
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (compass != null) {
            compass.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (compass != null) {
            compass.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (compass != null) {
            compass.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (compass != null) {
            compass.start();
        }
    }

    public void SaveBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean GetBoolean(String key) {
        return prefs.getBoolean(key, false);
    }

    public void SaveFloat(String key, Float value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public Float GetFloat(String key) {
        return prefs.getFloat(key, 0);
    }

    @SuppressLint("ObsoleteSdkInt")
    private void setupCompass() {
        Boolean permissionGranted = GetBoolean("permission_granted");

        if (permissionGranted) {
            getBearing();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        compass = new Compass(this);
        Compass.CompassListener compassListener = new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {
                adjustCompassDegree(azimuth);
                adjustCompassArrow(azimuth);
            }
        };

        compass.setListener(compassListener);
    }

    public void adjustCompassDegree(float azimuth) {
        Animation animation = new RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        currentAzimuth = (azimuth);
        animation.setDuration(500);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);
        compassDegree.setAnimation(animation);
    }

    public void adjustCompassArrow(float azimuth) {
        float qiblaDegree = GetFloat("qibla_degree");
        Animation animation = new RotateAnimation(-(currentAzimuth) + qiblaDegree, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        currentAzimuth = (azimuth);

        int minAzimuth = (int) (Math.floor(GetFloat("qibla_degree")) - 3);
        int maxAzimuth = (int) (Math.ceil(GetFloat("qibla_degree")) + 3);

        if (currentAzimuth >= minAzimuth && currentAzimuth <= maxAzimuth ) {
            compassOuter.setImageResource(R.drawable.compass_outer_green);
        } else {
            compassOuter.setImageResource(R.drawable.compass_outer_gray);
        }

        animation.setDuration(500);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);
        compassArrow.startAnimation(animation);
    }

    @SuppressLint("MissingPermission")
    public void getBearing() {
        float qiblaDegree = GetFloat("qibla_degree");
        if (qiblaDegree <= 0.0001) {
            fetchGPS();
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == 1) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                SaveBoolean("permission_granted", true);
//                setupCompass();
//            } else {
//                Toast.makeText(getApplicationContext(), "Permission denied!", Toast.LENGTH_LONG).show();
//                finish();
//            }
//            return;
//        }
//
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }

    public void fetchGPS() {
        double result;
        gps = new GPSTracker(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        if (gps.canGetLocation()) {
            double myLat = gps.getLatitude();
            double myLon = gps.getLongitude();

            try {
                List<Address> addresses = geocoder.getFromLocation(myLat, myLon, 1);
                if (addresses != null) {
                    city = addresses.get(0).getSubAdminArea();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (myLat >= 0.001 && myLon >= 0.001) {
                // Kaaba's position
                double kabahLat = Math.toRadians(21.422487);
                double kabahLon = 39.826206;
                double myRadiansLat = Math.toRadians(myLat);
                double lonDiff = Math.toRadians(kabahLon - myLon);
                double y = Math.sin(lonDiff) * Math.cos(kabahLat);
                double x = Math.cos(myRadiansLat) * Math.sin(kabahLat) - Math.sin(myRadiansLat) * Math.cos(kabahLat) * Math.cos(lonDiff);
                result = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
                SaveFloat("qibla_degree", (float) result);
            }
        } else {
            gps.showSettingAlert();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item2) {
        int itemId = item2.getItemId();
        if (itemId == R.id.home){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();        }
        if (itemId == R.id.qibla){
            Intent intent = new Intent(getApplicationContext(), Qibla.class);
            startActivity(intent);
            finish();        }
        if (itemId == R.id.sawm){
            Intent intent = new Intent(getApplicationContext(), Sawm.class);
            startActivity(intent);
            finish();        }
        if (itemId == R.id.settings){
            Intent intent = new Intent(getApplicationContext(), AppSettings.class);
            intent.putExtra("openedFromDrawer", true); // Pass the flag indicating it was opened from the drawer
            startActivity(intent);  }
        else if (itemId == R.id.profile) {
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            finish();         }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}