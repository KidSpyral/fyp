package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AddNewDiaryEntry.OnDiaryEntrySavedListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    String User;
    DatabaseReference referenceProfile;
    FloatingActionButton fab;
    FloatingActionButton prayers;
    private RecyclerView recyclerView, recyclerView2;
    private DiaryEntryAdapter DiaryEntryAdapter;
    private List<diaryEntry> diaryEntriesList;
    private PrayerAdapter PrayerAdapter;
    private List<Prayer> PrayerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);
        prayers = findViewById(R.id.prayers);

        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser().getUid();

        recyclerView = findViewById(R.id.DERecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView2 = findViewById(R.id.ContentRecyclerView);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setHasFixedSize(true);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewDiaryEntry dialog = AddNewDiaryEntry.newInstance();
                dialog.setOnDiaryEntrySavedListener(MainActivity.this);
                dialog.show(getSupportFragmentManager(), AddNewDiaryEntry.TAG);
            }
        });

        prayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SavedPrayers.class);
                startActivity(intent);
                finish();

            }
        });

        diaryEntriesList = new ArrayList<>();
        DiaryEntryAdapter = new DiaryEntryAdapter(diaryEntriesList, this);
        recyclerView.setAdapter(DiaryEntryAdapter);
        fetchTasks();

        PrayerList = new ArrayList<>();
        PrayerAdapter = new PrayerAdapter(PrayerList, this);
        recyclerView2.setAdapter(PrayerAdapter);
        fetchPrayers();



    }

    private void fetchTasks() {
        DatabaseReference DEReference = FirebaseDatabase.getInstance().getReference("Diary Entries").child(User);
        DEReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                diaryEntriesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    diaryEntry diaryEntry = snapshot.getValue(diaryEntry.class);
                    if (diaryEntry != null) {
                        diaryEntriesList.add(diaryEntry);
                    }
                }
                DiaryEntryAdapter.notifyDataSetChanged(); //
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error fetching diary entries", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPrayers() {
        DatabaseReference DEReference = FirebaseDatabase.getInstance().getReference("Prayers").child(User);
        DEReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PrayerList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Prayer Prayer = snapshot.getValue(Prayer.class);
                    if (Prayer != null) {
                        PrayerList.add(Prayer);
                        if (PrayerList.size() > 0) {
                            // Get the most recent entry (assuming PrayerList is sorted by date or has some timestamp)
                            Prayer mostRecentPrayer = PrayerList.get(PrayerList.size() - 1);

                            // Create a new list with only the most recent entry
                            List<Prayer> mostRecentList = new ArrayList<>();
                            mostRecentList.add(mostRecentPrayer);

                            // Update the PrayerAdapter with the most recent entry
                            PrayerAdapter.updateData(mostRecentList);
                        }
                    }
                }
                PrayerAdapter.notifyDataSetChanged(); //
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error fetching diary entries", Toast.LENGTH_SHORT).show();
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
        else if (itemId == R.id.profile) {
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            finish();         }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDiaryEntrySaved(String description) {

    }
}