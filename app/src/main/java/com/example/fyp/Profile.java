package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    TextView numbertextview;
    TextView nametextview;
    FirebaseAuth mAuth;
    String User;
    DatabaseReference referenceProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser().getUid();
        numbertextview = findViewById(R.id.numberTextDisplay);
        nametextview = findViewById(R.id.nameTextDisplay);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        if (User == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        else
            referenceProfile.child((User)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ReadWriteUserDetails userprofile  = snapshot.getValue(ReadWriteUserDetails.class);
                    if (userprofile != null){
                        String name = userprofile.name;
                        String number = userprofile.phone;

                        nametextview.setText(name);
                        numbertextview.setText(number);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    Toast.makeText(Profile.this, "Error!", Toast.LENGTH_SHORT).show();

                }
            });
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
        else if (itemId == R.id.profile) {
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            finish();         }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}