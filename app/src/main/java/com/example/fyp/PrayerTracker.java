package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_tracker);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); // Initialize SharedPreferences object

        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser().getUid();

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

        fajrtracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the flag
                isPrayerDone[0] = !isPrayerDone[0];

                // Save the prayer status to SharedPreferences
                sharedPreferences.edit().putBoolean("fajr_" + prayerKey, isPrayerDone[0]).apply();

                // Update the drawable
                updateDrawable(fajrtracker, isPrayerDone[0]);
            }
        });

        dhuhrtracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the flag
                isPrayerDone[0] = !isPrayerDone[0];

                // Save the prayer status to SharedPreferences
                sharedPreferences.edit().putBoolean("dhuhr_" + prayerKey, isPrayerDone[0]).apply();

                // Update the drawable
                updateDrawable(dhuhrtracker, isPrayerDone[0]);
            }
        });

        asrtracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the flag
                isPrayerDone[0] = !isPrayerDone[0];

                // Save the prayer status to SharedPreferences
                sharedPreferences.edit().putBoolean("asr_" + prayerKey, isPrayerDone[0]).apply();

                // Update the drawable
                updateDrawable(asrtracker, isPrayerDone[0]);
            }
        });

        maghribtracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the flag
                isPrayerDone[0] = !isPrayerDone[0];

                // Save the prayer status to SharedPreferences
                sharedPreferences.edit().putBoolean("maghrib_" + prayerKey, isPrayerDone[0]).apply();

                // Update the drawable
                updateDrawable(maghribtracker, isPrayerDone[0]);
            }
        });

        ishatracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the flag
                isPrayerDone[0] = !isPrayerDone[0];

                // Save the prayer status to SharedPreferences
                sharedPreferences.edit().putBoolean("isha_" + prayerKey, isPrayerDone[0]).apply();

                // Update the drawable
                updateDrawable(ishatracker, isPrayerDone[0]);
            }
        });
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

    private void setPrayerDone(boolean done) {
        if (done) {
            fajrtracker.setBackground(ContextCompat.getDrawable(this, R.drawable.text_bg_selector));
            dhuhrtracker.setBackground(ContextCompat.getDrawable(this, R.drawable.text_bg_selector));
            asrtracker.setBackground(ContextCompat.getDrawable(this, R.drawable.text_bg_selector));
            maghribtracker.setBackground(ContextCompat.getDrawable(this, R.drawable.text_bg_selector));
            ishatracker.setBackground(ContextCompat.getDrawable(this, R.drawable.text_bg_selector));
        } else {
            fajrtracker.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            dhuhrtracker.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            asrtracker.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            maghribtracker.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            ishatracker.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
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