package com.example.fyp.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppUtils {



    public static Date getCurrentDate(){
        Date currentDate = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

        try {
            String formattedDateStr = sdf.format(currentDate);
            Date formattedDate = sdf.parse(formattedDateStr);
            return formattedDate;//Sat Apr 20 00:00:00 GMT+05:00 2024
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isDifference30Days(Date currentDate, Date fetchedDate) {
        // Get the time in milliseconds for both dates
        long currentTimeMillis = currentDate.getTime();
        long fetchedTimeMillis = fetchedDate.getTime();

        // Calculate the difference in milliseconds
        long differenceMillis = Math.abs(currentTimeMillis - fetchedTimeMillis);

        // Calculate the number of days from milliseconds
        long daysDifference = differenceMillis / (1000 * 60 * 60 * 24);

        // Check if the difference is exactly 7 days
        return daysDifference <= 30;
    }

    public static boolean isDifference90Days(Date currentDate, Date fetchedDate) {
        // Get the time in milliseconds for both dates
        long currentTimeMillis = currentDate.getTime();
        long fetchedTimeMillis = fetchedDate.getTime();

        // Calculate the difference in milliseconds
        long differenceMillis = Math.abs(currentTimeMillis - fetchedTimeMillis);

        // Calculate the number of days from milliseconds
        long daysDifference = differenceMillis / (1000 * 60 * 60 * 24);

        // Check if the difference is exactly 7 days
        return daysDifference <= 90;
    }

    public static boolean isDifference7Days(Date currentDate, Date fetchedDate) {
        // Get the time in milliseconds for both dates
        long currentTimeMillis = currentDate.getTime();
        long fetchedTimeMillis = fetchedDate.getTime();

        // Calculate the difference in milliseconds
        long differenceMillis = Math.abs(currentTimeMillis - fetchedTimeMillis);

        // Calculate the number of days from milliseconds
        long daysDifference = differenceMillis / (1000 * 60 * 60 * 24);

        // Check if the difference is exactly 7 days
        return daysDifference <= 7;
    }

    public static boolean isDifference1Day(Date currentDate, Date fetchedDate) {
        // Get the time in milliseconds for both dates
        long currentTimeMillis = currentDate.getTime();
        long fetchedTimeMillis = fetchedDate.getTime();

        // Calculate the difference in milliseconds
        long differenceMillis = Math.abs(currentTimeMillis - fetchedTimeMillis);

        // Calculate the number of days from milliseconds
        long daysDifference = differenceMillis / (1000 * 60 * 60 * 24);

        // Check if the difference is exactly 7 days
        return daysDifference == 0;
    }
    public static Date parseFetchedDate(String dateString){
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

        try {
            Date date = sdf.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle parsing exception
        }
        return null;
    }
}
