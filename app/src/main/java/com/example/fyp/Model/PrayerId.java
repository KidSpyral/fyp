package com.example.fyp.Model;

import com.google.firebase.database.Exclude;

public class PrayerId {

    @Exclude
    public String PrayerId;


    public PrayerId() {
        // Default constructor required for Firebase
    }

    public String getPrayerId() {
        return PrayerId;
    }
    public void setPrayerId(String PrayerId) {
        this.PrayerId = PrayerId;
    }

}
