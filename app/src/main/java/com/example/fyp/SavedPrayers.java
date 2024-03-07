package com.example.fyp;

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
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavedPrayers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    String User;
    private DatabaseReference diaryEntriesReference;
    private PrayerAdapter PrayerAdapter;
    private List<Prayer> PrayerList;
    private RecyclerView recyclerView2;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_prayers);

        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser().getUid();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            diaryEntriesReference = FirebaseDatabase.getInstance().getReference("Diary Entries").child(uid);

            retrieveEmotionsAndPopulateChart();
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

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

        PrayerList = new ArrayList<>();
        PrayerAdapter = new PrayerAdapter(PrayerList, this);
        recyclerView2.setAdapter(PrayerAdapter);
        fetchPrayers();
    }

    private void retrieveEmotionsAndPopulateChart() {

        diaryEntriesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> daysOfWeek = getDaysOfWeek();

                for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                    diaryEntry entry = entrySnapshot.getValue(diaryEntry.class);

                    if (entry != null) {
                        // Assuming 'entrydate' is the field in your diaryEntry model
                        // and it contains the date in "dd/MM/yyyy" format
                        String entryDate = entry.getEntryDate();

                        // Parse the date string into a Date object
                        Date date = parseDate(entryDate);

                        // Get the day of the week from the parsed date
                        int dayOfWeek = getDayOfWeek(date);

                        // Assuming 'sentiment' is the field in your diaryEntry model
                        String sentiment = entry.getSentiment();

                        entries.add(new BarEntry(dayOfWeek, getEmotionIndex(sentiment)));
                    }
                }

                BarDataSet dataSet = new BarDataSet(entries, "Emotion Values, 1. anger, 2. fear, 3. joy, 4. love, 5. sadness, 6. surprise.");
                BarData barData = new BarData(dataSet);
                dataSet.setColor(getResources().getColor(R.color.accent_color));

                BarChart barChart = findViewById(R.id.MoodChartData);
                barChart.setData(barData);

                // Customize chart appearance
                barChart.getDescription().setEnabled(false);
                barChart.setDrawGridBackground(false);

                XAxis xAxis = barChart.getXAxis();
                xAxis.setGranularity(1f);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));

                YAxis leftAxis = barChart.getAxisLeft();
                leftAxis.setAxisMinimum(0f);

                YAxis rightAxis = barChart.getAxisRight();
                rightAxis.setEnabled(false);

                barChart.invalidate(); // Refresh the chart
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
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

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(); // Return a default date or handle the error accordingly
        }
    }

    private int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    private List<String> getDaysOfWeek() {
        List<String> daysOfWeek = new ArrayList<>();
        daysOfWeek.add("Sat");
        daysOfWeek.add("Sun");
        daysOfWeek.add("Mon");
        daysOfWeek.add("Tue");
        daysOfWeek.add("Wed");
        daysOfWeek.add("Thu");
        daysOfWeek.add("Fri");
        daysOfWeek.add("Sat");
        return daysOfWeek;
    }

    private int getEmotionIndex(String sentiment) {
        // Map sentiments to emotion indices
        // Customize this based on your specific sentiments
        switch (sentiment.toLowerCase()) {
            case "anger":
                return 1;
            case "fear":
                return 2;
            case "joy":
                return 3;
            case "love":
                return 4;
            case "sadness":
                return 5;
            case "surprise":
                return 6;
            default:
                return 0;
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