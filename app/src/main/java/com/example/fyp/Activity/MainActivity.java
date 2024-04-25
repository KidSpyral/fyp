package com.example.fyp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.fyp.Adapter.DiaryEntryAdapter;
import com.example.fyp.Adapter.PrayerAdapter;
import com.example.fyp.Model.Prayer;
import com.example.fyp.Model.diaryEntry;
import com.example.fyp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AddNewDiaryEntry.OnDiaryEntrySavedListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    String User;
    DatabaseReference referenceProfile;
    FloatingActionButton fab;
    FloatingActionButton prayers;
    FloatingActionButton prayer_times;
    private RecyclerView recyclerView, recyclerView2;
    private com.example.fyp.Adapter.DiaryEntryAdapter DiaryEntryAdapter;
    private List<diaryEntry> diaryEntriesList;
    private com.example.fyp.Adapter.PrayerAdapter PrayerAdapter;
    private List<Prayer> PrayerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);
        prayers = findViewById(R.id.prayers);
        prayer_times = findViewById(R.id.prayer_times);

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

        prayer_times.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Prayer_Times.class);
                startActivity(intent);
                finish();

            }
        });

        diaryEntriesList = new ArrayList<>();
        DiaryEntryAdapter = new DiaryEntryAdapter(diaryEntriesList, this);
        recyclerView.setAdapter(DiaryEntryAdapter);
        fetchTasks("");
        fetchUniqueMoodEntries();

        PrayerList = new ArrayList<>();
        PrayerAdapter = new PrayerAdapter(PrayerList, this);
        recyclerView2.setAdapter(PrayerAdapter);
        fetchPrayers();



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
                            // most recent entry (assuming PrayerList is sorted by date or has some timestamp)
                            Prayer mostRecentPrayer = PrayerList.get(PrayerList.size() - 1);

                            // new list with only the most recent entry
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

    private void fetchUniqueMoodEntries() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Diary Entries")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> uniqueMoods = new HashSet<>();

                uniqueMoods.add("");

                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    diaryEntry entry = entrySnapshot.getValue(diaryEntry.class);
                    if (entry != null) {
                        uniqueMoods.add(entry.getSentiment());
                    }
                }
                // Now, 'uniqueMoods' set contains unique mood entries
                // Populate the spinner with these entries
                populateSpinner(uniqueMoods);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    private void populateSpinner(Set<String> uniqueMoods) {
        // Convert set to list for spinner adapter
        List<String> moodList = new ArrayList<>(uniqueMoods);

        // Create an ArrayAdapter and set it to the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moodList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner moodSpinner = findViewById(R.id.moodList);
        moodSpinner.setAdapter(adapter);

        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedMood = moodList.get(position);
                if (selectedMood.isEmpty()) {
                    // Handle the case where no mood is selected
                    fetchTasks("");
                } else {
                    // Handle the selected mood
                    fetchTasks(selectedMood);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Fetch all entries when nothing is selected
                fetchTasks("");
            }
        });
    }


    private void fetchTasks(String selectedMood) {
        DatabaseReference DEReference = FirebaseDatabase.getInstance().getReference("Diary Entries").child(User);
        DEReference.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                diaryEntriesList.clear();
                List<diaryEntry> newEntries = new ArrayList<>(); // Create a new list for updated data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    diaryEntry diaryEntry = snapshot.getValue(diaryEntry.class);
                    if (diaryEntry != null&& (selectedMood.isEmpty() || selectedMood.equals(diaryEntry.getSentiment()))) {
                        // Add each diary entry to the beginning of the list
                        newEntries.add(0, diaryEntry);
                    }
                }
                DiaryEntryAdapter.updateData(newEntries);
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

    @Override
    public void onDiaryEntrySaved(String description) {

    }
}