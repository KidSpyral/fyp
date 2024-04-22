package com.example.fyp;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.fyp.common.AppUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.fyp.diaryEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedPrayers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    String User;
    DatabaseReference referenceProfile;
    private PrayerAdapter PrayerAdapter;
    private List<Prayer> PrayerList;

    private RecyclerView recyclerView2;
    private PieChart pieChart;
    private Spinner spinner;
    List<diaryEntry> diaryEntries;
    List<diaryEntry> diarySortedEntries;


    private void reDrawPieChart(String dataType){

        diarySortedEntries = new ArrayList<>();
        Date currentDate = AppUtils.getCurrentDate();

        if(dataType.equals("All")){
            diarySortedEntries.addAll(diaryEntries);
            calculateEmotionFrequencies(diarySortedEntries);
        }else if (dataType.equals("Last 7 days")){

            for (int jack = 0; jack < diaryEntries.size(); jack++) {

                if (AppUtils.isDifference7Days(currentDate, AppUtils.parseFetchedDate(diaryEntries.get(jack).getEntryDate()))) {
                    //dateArray[jack] = FireStoreDB.graphMoodsList.get(jack).getPainNoteDate();
                    diarySortedEntries.add(diaryEntries.get(jack));
                }
            }

            calculateEmotionFrequencies(diarySortedEntries);


        }else if (dataType.equals("Last 30 days")){
            for (int jack = 0; jack < diaryEntries.size(); jack++) {

                if (AppUtils.isDifference30Days(currentDate, AppUtils.parseFetchedDate(diaryEntries.get(jack).getEntryDate()))) {
                    //dateArray[jack] = FireStoreDB.graphMoodsList.get(jack).getPainNoteDate();
                    diarySortedEntries.add(diaryEntries.get(jack));
                }
            }

            calculateEmotionFrequencies(diarySortedEntries);

        }else if (dataType.equals("Last 90 days")){
            for (int jack = 0; jack < diaryEntries.size(); jack++) {

                if (AppUtils.isDifference90Days(currentDate, AppUtils.parseFetchedDate(diaryEntries.get(jack).getEntryDate()))) {
                    //dateArray[jack] = FireStoreDB.graphMoodsList.get(jack).getPainNoteDate();
                    diarySortedEntries.add(diaryEntries.get(jack));
                }
            }

            calculateEmotionFrequencies(diarySortedEntries);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_prayers);

        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser().getUid();

        diaryEntries = new ArrayList<>();

        pieChart = findViewById(R.id.pieChart);
        spinner = findViewById(R.id.dataList);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reDrawPieChart(parent.getItemAtPosition(position).toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView2 = findViewById(R.id.ContentRecyclerView);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setHasFixedSize(true);

        retrieveDiaryEntries();

        PrayerList = new ArrayList<>();
        PrayerAdapter = new PrayerAdapter(PrayerList, this);
        recyclerView2.setAdapter(PrayerAdapter);
        fetchPrayers();
    }

    private void retrieveDiaryEntries() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference diaryRef = FirebaseDatabase.getInstance().getReference("Diary Entries").child(User);
            diaryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    diaryEntries = new ArrayList<>();
                    for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                        diaryEntry diaryEntry = entrySnapshot.getValue(diaryEntry.class);
                        if (diaryEntry != null) {
                            diaryEntries.add(diaryEntry);
                        }
                    }
                    calculateEmotionFrequencies(diaryEntries);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SavedPrayers.this, "Failed to retrieve diary entries: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void calculateEmotionFrequencies(List<diaryEntry> diaryEntries) {
        // Define list of emotions
        String[] emotions = {"anger", "fear", "joy", "love", "sadness", "surprise"};

        // Initialize emotion frequency map
        HashMap<String, Integer> emotionFrequencies = new HashMap<>();
        for (String emotion : emotions) {
            emotionFrequencies.put(emotion, 0);
        }

        // Iterate through diary entries and update emotion frequencies
        for (diaryEntry entry : diaryEntries) {
            String emotion = entry.getSentiment();
            if (emotionFrequencies.containsKey(emotion)) {
                emotionFrequencies.put(emotion, emotionFrequencies.get(emotion) + 1);
            }
        }

        // Generate pie chart with emotion frequencies
        generatePieChart(emotionFrequencies);
    }

    private void generatePieChart(HashMap<String, Integer> emotionFrequencies) {
        // Prepare data for the pie chart
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        // Customize colors based on emotion
        for (Map.Entry<String, Integer> entry : emotionFrequencies.entrySet()) {
            String emotion = entry.getKey();
            int frequency = entry.getValue();

            if (frequency > 0) {
                entries.add(new PieEntry(frequency, emotion));

                switch (emotion) {
                    case "anger":
                        colors.add(Color.RED);
                        break;
                    case "fear":
                        colors.add(Color.BLUE);
                        break;
                    case "joy":
                        colors.add(Color.GREEN);
                        break;
                    case "love":
                        colors.add(Color.MAGENTA);
                        break;
                    case "sadness":
                        colors.add(Color.GRAY);
                        break;
                    case "surprise":
                        colors.add(Color.YELLOW);
                        break;
                }
            }
        }

        // Create dataset and configure the pie chart
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f); // Set text size for the pie chart values

        PieData data = new PieData(dataSet);

        // Configure pie chart
        pieChart.setData(data);
        pieChart.setEntryLabelTextSize(0); // Set text size for the pie chart labels
        pieChart.setDescription(null); // Disable description
        pieChart.setDrawEntryLabels(true);
        pieChart.setUsePercentValues(true);
        pieChart.invalidate(); // Refresh chart
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
                    }
                }
                PrayerAdapter.notifyDataSetChanged(); //
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SavedPrayers.this, "Error fetching diary entries", Toast.LENGTH_SHORT).show();
            }
        });
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
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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
        else if (itemId == R.id.profile) {
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            finish();         }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}