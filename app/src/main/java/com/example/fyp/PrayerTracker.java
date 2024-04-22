package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;

public class PrayerTracker extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    TextView numbertextview;
    TextView nametextview;
    FirebaseAuth mAuth;
    String User;
    DatabaseReference referenceProfile;
    private TextView fajrtracker, dhuhrtracker, asrtracker, maghribtracker, ishatracker ;
    private SharedPreferences sharedPreferences;
    String prayerKey;
    boolean[] isPrayerDone;
    private static final String lastResetKey = "last_reset_date";


    private static final String PREF_NAME = "ChecklistPrefs";
    private static final String LAST_RESET_TIME = "lastResetTime";

    public static void setLastResetTime(Context context, long timeInMillis) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(LAST_RESET_TIME, timeInMillis);
        editor.apply();
    }

    public static long getLastResetTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(LAST_RESET_TIME, 0); // Default value is 0
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_tracker);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); // Initialize SharedPreferences object

        // Initialize prayerKey with an appropriate value (e.g., user's ID)
        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser().getUid();
        prayerKey = User;

        // Update the last reset date to today
        Calendar today = Calendar.getInstance();
        long todayInMillis = today.getTimeInMillis();
        sharedPreferences.edit().putLong(lastResetKey, todayInMillis).apply();

        // Retrieve last reset date from SharedPreferences
        long lastResetDateInMillis = sharedPreferences.getLong(lastResetKey, 0);
        Calendar lastResetDate = Calendar.getInstance();
        lastResetDate.setTimeInMillis(lastResetDateInMillis);

        // Check if the last reset date is not today
//        if (today.get(Calendar.DAY_OF_YEAR) != lastResetDate.get(Calendar.DAY_OF_YEAR)) {
//            // Reset the prayer status
//            resetPrayerStatus();
//
//            // Update the last reset date to today
//            sharedPreferences.edit().putLong(lastResetKey, todayInMillis).apply();
//        }
        long lastResetTime = getLastResetTime(this);
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastResetTime;

        if (timeDifference >= 24 * 60 * 60 * 1000) { // 24 hours in milliseconds
            setLastResetTime(this, currentTime);
            for(int index =0;index<5;index++){
                sharedPreferences.edit().putBoolean(getPrayerKey(index), false).apply();
            }

        }

        fajrtracker = findViewById(R.id.fajrtracker);
        dhuhrtracker = findViewById(R.id.dhuhrtracker);
        asrtracker = findViewById(R.id.asrtracker);
        maghribtracker = findViewById(R.id.maghribtracker);
        ishatracker = findViewById(R.id.ishatracker);

        // Retrieve prayer status for all prayers from SharedPreferences
        boolean fajrStatus = sharedPreferences.getBoolean("fajr_" + prayerKey, false);
        boolean dhuhrStatus = sharedPreferences.getBoolean("dhuhr_" + prayerKey, false);
        boolean asrStatus = sharedPreferences.getBoolean("asr_" + prayerKey, false);
        boolean maghribStatus = sharedPreferences.getBoolean("maghrib_" + prayerKey, false);
        boolean ishaStatus = sharedPreferences.getBoolean("isha_" + prayerKey, false);

        // Initialize prayer status array
        isPrayerDone = new boolean[]{fajrStatus, dhuhrStatus, asrStatus, maghribStatus, ishaStatus};

        // Set initial drawable based on prayer status
        updateDrawables();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        // Set click listeners for prayer trackers
        fajrtracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePrayerStatus(0);
            }
        });

        dhuhrtracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePrayerStatus(1);
            }
        });

        asrtracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePrayerStatus(2);
            }
        });

        maghribtracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePrayerStatus(3);
            }
        });

        ishatracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePrayerStatus(4);
            }
        });
    }

    // Method to reset the prayer status
    private void resetPrayerStatus() {
        // Reset the prayer status for all prayers
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("fajr_" + prayerKey, false);
        editor.putBoolean("dhuhr_" + prayerKey, false);
        editor.putBoolean("asr_" + prayerKey, false);
        editor.putBoolean("maghrib_" + prayerKey, false);
        editor.putBoolean("isha_" + prayerKey, false);
        editor.apply();

        // Update the drawables
        updateDrawables();
    }

    // Update drawables for all prayer trackers
    private void updateDrawables() {
        updateDrawable(fajrtracker, isPrayerDone[0]);
        updateDrawable(dhuhrtracker, isPrayerDone[1]);
        updateDrawable(asrtracker, isPrayerDone[2]);
        updateDrawable(maghribtracker, isPrayerDone[3]);
        updateDrawable(ishatracker, isPrayerDone[4]);
    }

    // Update the drawable for a specific prayer tracker
    private void updateDrawable(TextView textView, boolean isDone) {
        if (isDone) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.prayer_trackerv2, 0); // Set your green drawable here
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.prayer_tracker, 0); // Set your default drawable here
        }
    }

    // Toggle the status of a specific prayer
    private void togglePrayerStatus(int index) {
        // Toggle the flag
        isPrayerDone[index] = !isPrayerDone[index];

        // Save the prayer status to SharedPreferences
        sharedPreferences.edit().putBoolean(getPrayerKey(index), isPrayerDone[index]).apply();

        // Update the drawable
        updateDrawable(getPrayerTextView(index), isPrayerDone[index]);
    }

    // Get the SharedPreferences key for a specific prayer
    private String getPrayerKey(int index) {
        String[] prayerKeys = {"fajr", "dhuhr", "asr", "maghrib", "isha"};
        return prayerKeys[index] + "_" + prayerKey;
    }

    // Get the TextView for a specific prayer
    private TextView getPrayerTextView(int index) {
        switch (index) {
            case 0:
                return fajrtracker;
            case 1:
                return dhuhrtracker;
            case 2:
                return asrtracker;
            case 3:
                return maghribtracker;
            case 4:
                return ishatracker;
            default:
                return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu2, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.backButton){
            Intent intent = new Intent(getApplicationContext(), prayer_times.class);
            startActivity(intent);
            finish();        }
        else if (itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);       }
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
            startActivity(intent);
            finish();        }
        else if (itemId == R.id.profile) {
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            finish();         }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}