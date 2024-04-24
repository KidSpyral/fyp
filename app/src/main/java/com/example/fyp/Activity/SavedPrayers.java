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
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.fyp.Adapter.PrayerAdapter;
import com.example.fyp.Model.Prayer;
import com.example.fyp.Model.diaryEntry;
import com.example.fyp.R;
import com.example.fyp.common.AppUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private com.example.fyp.Adapter.PrayerAdapter PrayerAdapter;
    private List<Prayer> PrayerList;

    private RecyclerView recyclerView2;
    private PieChart pieChart;
    private Spinner spinner;
    List<diaryEntry> diaryEntries;
    List<diaryEntry> diarySortedEntries;
    private LineChart lineChart;


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

        lineChart = findViewById(R.id.line1);



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

    private void lineChartgo() {

        // Configure line chart
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(true);

//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[] {"anger", "fear", "joy", "love", "sadness", "surprise"}));

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // start at zero
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Call method to update line chart with emotion data for last 7 days
        updateLineChart();
    }

    // Add this method to calculate emotion frequencies and update the line chart
    private void updateLineChart() {
        // Assuming diaryEntries contains all diary entries
        List<diaryEntry> last7DaysEntries = new ArrayList<>();
        Date currentDate = AppUtils.getCurrentDate();

        for (diaryEntry entry : diaryEntries) {
            if (AppUtils.isDifference7Days(currentDate, AppUtils.parseFetchedDate(entry.getEntryDate()))) {
                last7DaysEntries.add(entry);
            }
        }

//        //"anger", "fear", "joy", "love", "sadness", "surprise"
//        ArrayList<Entry> anger = new ArrayList<>();
//        for(int index = 0;index<last7DaysEntries.size();index++){
//            if(last7DaysEntries.get(index).getSentiment().equals("anger")){
//                anger.add(new Entry(index,1));
//            }
//        }
//
//        ArrayList<Entry> joy = new ArrayList<>();
//        for(int index = 0;index<last7DaysEntries.size();index++){
//            if(last7DaysEntries.get(index).getSentiment().equals("joy")){
//                joy.add(new Entry(index,1));
//            }
//        }
//
//        ArrayList<Entry> sadness = new ArrayList<>();
//        for(int index = 0;index<last7DaysEntries.size();index++){
//            if(last7DaysEntries.get(index).getSentiment().equals("sadness")){
//                sadness.add(new Entry(index,1));
//            }
//        }



//        for (diaryEntry entry : diaryEntries) {
//            if (AppUtils.isDifference90Days(currentDate, AppUtils.parseFetchedDate(entry.getEntryDate()))) {
//                last7DaysEntries.add(entry);
//            }
//        }


        HashMap<String, Integer> emotionFrequencies = calculateEmotionFrequencies(last7DaysEntries);
        //HashMap<String, Integer> emoncies = calculateEmotionForLineGraph(last7DaysEntries);

        String[] dates = AppUtils.getPreviousSevenDates(AppUtils.getCurrentDate());

        HashMap<String, List<diaryEntry>> dateEntries = new HashMap<>();
        for (String date : dates) {
            dateEntries.put(date, new ArrayList<>());
        }

        // Populate the map with entry objects for each date
        for (diaryEntry entry : diaryEntries) {
            String date = entry.getEntryDate();
            if (dateEntries.containsKey(date)) {
                List<diaryEntry> entries = dateEntries.get(date);
                entries.add(entry);
                dateEntries.put(date, entries);
            }
        }





        HashMap<String, HashMap<String, Integer>> sentimentCountsMap = new HashMap<>();

        // Populate the map with sentiment counts for each date
        for (Map.Entry<String, List<diaryEntry>> entry : dateEntries.entrySet()) {
            String date = entry.getKey();
            List<diaryEntry> entries = entry.getValue();

            // Initialize HashMap to store sentiment counts for the current date
            HashMap<String, Integer> sentimentCounts = new HashMap<>();

            // Count the occurrences of each sentiment on the same date
            for (diaryEntry diaryEntry : entries) {
                String sentiment = diaryEntry.getSentiment();
                sentimentCounts.put(sentiment, sentimentCounts.getOrDefault(sentiment, 0) + 1);
            }

            // Put sentiment counts for the current date in the sentimentCountsMap
            sentimentCountsMap.put(date, sentimentCounts);
        }



        ArrayList<Entry> anger = new ArrayList<>();
        ArrayList<Entry> fear = new ArrayList<>();
        ArrayList<Entry> joy = new ArrayList<>();
        ArrayList<Entry> love = new ArrayList<>();
        ArrayList<Entry> sadness = new ArrayList<>();
        ArrayList<Entry> surprise = new ArrayList<>();

// Loop through sentimentCountsMap
        for (Map.Entry<String, HashMap<String, Integer>> entry : sentimentCountsMap.entrySet()) {
            String date = entry.getKey();
            HashMap<String, Integer> sentimentCounts = entry.getValue();

            // Iterate through each sentiment count

            for (Map.Entry<String, Integer> sentimentCount : sentimentCounts.entrySet()) {
                String sentiment = sentimentCount.getKey();
                int count = sentimentCount.getValue();
                float yValue = count; // Use count as the Y-value

                // Convert date to timestamp
                long xValue = AppUtils.convertDateToTimestamp(date);

                // Create Entry object for the sentiment count
                Entry entryNew = new Entry(xValue, yValue);
                // Add Entry object to the appropriate ArrayList based on sentiment
                switch (sentiment) {
                    case "anger":
                        anger.add(entryNew);
                        break;
                    case "fear":
                        fear.add(entryNew);
                        break;
                    case "joy":
                        joy.add(entryNew);
                        break;
                    case "love":
                        love.add(entryNew);
                        break;
                    case "sadness":
                        sadness.add(entryNew);
                        break;
                    case "surprise":
                        surprise.add(entryNew);
                        break;
                    default:
                        // Handle unknown sentiment
                }

            }
        }

        for(int i = 0;i<anger.size();i++){
            anger.get(i).setX(i);
        }
        // Create data sets for each line
        LineDataSet dataSet1 = new LineDataSet(anger, "Anger");
        dataSet1.setColor(Color.RED);
        dataSet1.setValueTextColor(Color.RED);

        for(int i = 0;i<joy.size();i++){
            joy.get(i).setX(i);
        }

        LineDataSet dataSet2 = new LineDataSet(joy, "Joy");
        dataSet2.setColor(Color.GREEN);
        dataSet2.setValueTextColor(Color.GREEN);


        for(int i = 0;i<sadness.size();i++){
            sadness.get(i).setX(i);
        }

        LineDataSet dataSet3 = new LineDataSet(sadness, "Sadness");
        dataSet3.setColor(Color.GRAY);
        dataSet3.setValueTextColor(Color.GRAY);

        for(int i = 0;i<surprise.size();i++){
            surprise.get(i).setX(i);
        }


        LineDataSet dataSet4 = new LineDataSet(surprise, "Surprise");
        dataSet4.setColor(Color.YELLOW);
        dataSet4.setValueTextColor(Color.YELLOW);

        for(int i = 0;i<fear.size();i++){
            fear.get(i).setX(i);
        }


        LineDataSet dataSet5 = new LineDataSet(fear, "Fear");
        dataSet5.setColor(Color.BLUE);
        dataSet5.setValueTextColor(Color.BLUE);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        // Add data sets to the chart
        dataSets.add(dataSet1);
        dataSets.add(dataSet2);
        dataSets.add(dataSet3);
        dataSets.add(dataSet4);
        dataSets.add(dataSet5);

        // Create LineData object from the data sets
        LineData lineData = new LineData(dataSets);



        // Customize chart appearance
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);

        // Customize x-axis
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        //xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[] {"17/4/2024","18/4/2024", "19/4/2024", "20/4/2024", "21/4/2024", "22/4/2024", "23/4/2024"}));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));


        // Customize y-axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(Color.BLACK);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Customize legend
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);
        // Set the data to the chart
        lineChart.setData(lineData);
        lineChart.invalidate();




//        int index = 0;
//        for (Map.Entry<String, Integer> entry : emotionFrequencies.entrySet()) {
//            entries.add(new Entry(index++, entry.getValue()));
//        }
//
//        LineDataSet dataSet = new LineDataSet(entries, "Emotion Frequency");
//        LineData lineData = new LineData(dataSet);
//        lineChart.setData(lineData);
//        lineChart.invalidate();
    }

    private HashMap<String, Integer> calculateEmotionForLineGraph(List<diaryEntry> diaryEntries) {

        String[] dates = AppUtils.getPreviousSevenDates(AppUtils.getCurrentDate());

        HashMap<String, List<diaryEntry>> dateEntries = new HashMap<>();
        for (String date : dates) {
            dateEntries.put(date, new ArrayList<>());
        }

        // Populate the map with entry objects for each date
        for (diaryEntry entry : diaryEntries) {
            String date = entry.getEntryDate();
            if (dateEntries.containsKey(date)) {
                List<diaryEntry> entries = dateEntries.get(date);
                entries.add(entry);
                dateEntries.put(date, entries);
            }
        }





        HashMap<String, HashMap<String, Integer>> sentimentCountsMap = new HashMap<>();

        // Populate the map with sentiment counts for each date
        for (Map.Entry<String, List<diaryEntry>> entry : dateEntries.entrySet()) {
            String date = entry.getKey();
            List<diaryEntry> entries = entry.getValue();

            // Initialize HashMap to store sentiment counts for the current date
            HashMap<String, Integer> sentimentCounts = new HashMap<>();

            // Count the occurrences of each sentiment on the same date
            for (diaryEntry diaryEntry : entries) {
                String sentiment = diaryEntry.getSentiment();
                sentimentCounts.put(sentiment, sentimentCounts.getOrDefault(sentiment, 0) + 1);
            }

            // Put sentiment counts for the current date in the sentimentCountsMap
            sentimentCountsMap.put(date, sentimentCounts);
        }



        ArrayList<Entry> anger = new ArrayList<>();
        ArrayList<Entry> fear = new ArrayList<>();
        ArrayList<Entry> joy = new ArrayList<>();
        ArrayList<Entry> love = new ArrayList<>();
        ArrayList<Entry> sadness = new ArrayList<>();
        ArrayList<Entry> surprise = new ArrayList<>();

// Loop through sentimentCountsMap
        for (Map.Entry<String, HashMap<String, Integer>> entry : sentimentCountsMap.entrySet()) {
            String date = entry.getKey();
            HashMap<String, Integer> sentimentCounts = entry.getValue();

            // Iterate through each sentiment count
            for (Map.Entry<String, Integer> sentimentCount : sentimentCounts.entrySet()) {
                String sentiment = sentimentCount.getKey();
                int count = sentimentCount.getValue();
                float yValue = count; // Use count as the Y-value

                // Convert date to timestamp
                long xValue = AppUtils.convertDateToTimestamp(date);

                // Create Entry object for the sentiment count
                Entry entryNew = new Entry(xValue, yValue);

                // Add Entry object to the appropriate ArrayList based on sentiment
                switch (sentiment) {
                    case "anger":
                        anger.add(entryNew);
                        break;
                    case "fear":
                        fear.add(entryNew);
                        break;
                    case "joy":
                        joy.add(entryNew);
                        break;
                    case "love":
                        love.add(entryNew);
                        break;
                    case "sadness":
                        sadness.add(entryNew);
                        break;
                    case "surprise":
                        surprise.add(entryNew);
                        break;
                    default:
                        // Handle unknown sentiment
                }
            }
        }



        // Initialize emotion frequency map
//        HashMap<String, Integer> dateFrequencies = new HashMap<>();
//        for (String date : dates) {
//            dateFrequencies.put(date, 0);
//        }
//        for (diaryEntry entry : diaryEntries) {
//            String date = entry.getEntryDate();
//            if (dateFrequencies.containsKey(date)) {
//                dateFrequencies.put(date, dateFrequencies.get(date) + 1);
//            }
//        }


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

        return emotionFrequencies;
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
                    lineChartgo();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SavedPrayers.this, "Failed to retrieve diary entries: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private HashMap<String, Integer> calculateEmotionFrequencies(List<diaryEntry> diaryEntries) {
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

        return emotionFrequencies;
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