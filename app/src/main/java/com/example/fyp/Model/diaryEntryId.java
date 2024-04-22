package com.example.fyp.Model;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

public class diaryEntryId {


    @Exclude
    public String diaryEntryId;

    public diaryEntryId() {
        // Default constructor required for Firebase
    }
    public String getDiaryEntryId() {
        return diaryEntryId;
    }
    public void setDiaryEntryId(String diaryEntryId) {
        this.diaryEntryId = diaryEntryId;
    }

    public <T extends diaryEntryId> T withId(@NonNull final String id) {
        this.diaryEntryId = id;
        return (T) this;
    }
}
