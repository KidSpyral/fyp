package com.example.fyp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class PrayerAdapter extends RecyclerView.Adapter<PrayerAdapter.TaskViewHolder> {

    private List<Prayer> PrayerList;
    private MainActivity activity;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Context context;

    public PrayerAdapter(List<Prayer> PrayerList, Context context){
        this.PrayerList = PrayerList;
        this.context = context;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView Description;

        // ViewHolder components (e.g., TextViews for tag, description, etc.)

        public TaskViewHolder(View itemView) {
            super(itemView);
            // Initialize ViewHolder components
            Description = itemView.findViewById(R.id.Description);
        }
    }

    @NonNull
    @Override
    public PrayerAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new PrayerAdapter.TaskViewHolder(itemView);
    }

    public Context getContext(){
        return context;
    }

    public void updateData(List<Prayer> newList) {
        this.PrayerList = newList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerAdapter.TaskViewHolder holder, int position) {
        Prayer Prayer = PrayerList.get(position);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && Prayer != null) {
            holder.Description.setText(Prayer.getPrayer());
        }
    }

    @Override
    public int getItemCount() {
        if (PrayerList == null) {
            Log.d("PrayerAdapter", "PrayerList is null");
            return 0;
        }
        return PrayerList.size();
    }
}
