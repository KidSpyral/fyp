package com.example.fyp.Adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.Activity.MainActivity;
import com.example.fyp.Model.diaryEntry;
import com.example.fyp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class DiaryEntryAdapter extends RecyclerView.Adapter<DiaryEntryAdapter.TaskViewHolder> {

    private List<diaryEntry> diaryEntriesList;
    private MainActivity activity;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Context context;

    public DiaryEntryAdapter(List<diaryEntry> diaryEntriesList, Context context){
        this.diaryEntriesList = diaryEntriesList;
        this.context = context;


    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView Title;
        TextView Date;
        ImageView Mood;
        TextView Description;

        // ViewHolder components (e.g., TextViews for tag, description, etc.)

        public TaskViewHolder(View itemView) {
            super(itemView);
            // Initialize ViewHolder components
            Title = itemView.findViewById(R.id.Title);
            Date = itemView.findViewById(R.id.Date);
            Mood = itemView.findViewById(R.id.Mood);
            Description = itemView.findViewById(R.id.Description);


        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new TaskViewHolder(itemView);
    }

    public void deleteDiaryEntry(int position) {
        if (diaryEntriesList != null && !diaryEntriesList.isEmpty()) {
            Log.d("DiaryEntryAdapter", "Initial Position: " + position);
            Log.d("DiaryEntryAdapter", "List contents before deletion: " + diaryEntriesList);

            if (position >= 0 && position < diaryEntriesList.size()) {
                diaryEntry diaryEntries = diaryEntriesList.get(position);
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser != null) {
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance()
                            .getReference("Diary Entries")
                            .child(currentUser.getUid());

                    String entryId = diaryEntries.getDiaryEntryId();

                    if (entryId != null) {
                        Log.d("DiaryEntryAdapter", "Deleting entry with ID: " + entryId);

                        referenceProfile.child(entryId).removeValue()
                                .addOnCompleteListener(task -> {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        if (task.isSuccessful()) {
                                            if (position >= 0 && position < diaryEntriesList.size()) {
                                                Log.d("DiaryEntryAdapter", "Firebase deletion successful");

                                                // Log position before and after deletion
                                                Log.d("DiaryEntryAdapter", "Position before deletion: " + position);

                                                // Remove the item from the list only if Firebase deletion is successful
                                                diaryEntriesList.remove(position);

                                                // Notify the adapter
                                                notifyDataSetChanged();

                                                // Log list contents after deletion
                                                Log.d("DiaryEntryAdapter", "List contents after deletion: " + diaryEntriesList);

                                                Log.d("DiaryEntryAdapter", "Position after deletion: " + position);
                                            }
                                        }else {
                                            Log.e("DiaryEntryAdapter", "Firebase deletion failed", task.getException());
                                        }
                                    });
                                });
                    } else {
                        Log.e("DiaryEntryAdapter", "Entry ID is null");
                    }
                } else {
                    Log.e("DiaryEntryAdapter", "Current user is null");
                }
            } else {
                Log.e("DiaryEntryAdapter", "Invalid position: " + position);
            }
        } else {
            Log.e("DiaryEntryAdapter", "Invalid list or empty list");
        }
    }


    public void updateData(List<diaryEntry> newData) {
        diaryEntriesList.clear(); // Clear existing data
        diaryEntriesList.addAll(newData); // Add new data
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }



    public Context getContext(){
        return context;
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        diaryEntry diaryEntry = diaryEntriesList.get(position);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && diaryEntry != null) {
            holder.Title.setText(diaryEntry.getTitle());
            holder.Date.setText(diaryEntry.getEntryDate());
            holder.Description.setText(diaryEntry.getDescription());

            String sentiment = diaryEntry.getSentiment();
            int moodResource;

            switch (sentiment) {
                case "fear":
                    moodResource = R.drawable.fear;
                    break;
                case "sadness":
                    moodResource = R.drawable.sadness;
                    break;
                case "anger":
                    moodResource = R.drawable.angry;
                    break;
                case "joy":
                    moodResource = R.drawable.joy;
                    break;
                case "love":
                    moodResource = R.drawable.love;
                    break;
                case "surprise":
                    moodResource = R.drawable.surprise;
                    break;
                default:
                    // Throw an exception if sentiment doesn't match any known mood
                    throw new IllegalArgumentException("Unknown sentiment: " + sentiment);
            }

            holder.Mood.setImageResource(moodResource);
        }
    }

    @Override
    public int getItemCount() {
        if (diaryEntriesList == null) {
            Log.d("DiaryEntryAdapter", "diaryEntriesList is null");
            return 0;
        }
        return diaryEntriesList.size();
    }
}
