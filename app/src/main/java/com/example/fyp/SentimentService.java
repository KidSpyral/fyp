package com.example.fyp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SentimentService {
    @POST("/predict_moodsentiment")
    Call<SentimentResponse> predictMoodSentiment(@Body SentimentRequest request);
    @POST("/predict_prayersentiment")
    Call<SentimentResponse> predictPrayerSentiment(@Body SentimentRequest request);
}

