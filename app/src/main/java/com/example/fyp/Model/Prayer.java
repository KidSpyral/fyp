package com.example.fyp.Model;

public class Prayer extends PrayerId
{

    private String prayer;


    public Prayer(){

    }

    public Prayer(String prayer) {
        this.prayer = prayer;
    }

    public String getPrayer() {
        return prayer;
    }

    public void setPrayer(String prayer) {
        this.prayer = prayer;
    }

    @Override
    public String toString() {
        return "Prayer{id='" + PrayerId + "', prayer='" + prayer + "'}";
    }

}
