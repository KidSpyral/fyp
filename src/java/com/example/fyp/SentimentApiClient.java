package com.example.fyp;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SentimentApiClient {
    private final SentimentService sentimentService;
    private final String flashAddr;

    public SentimentApiClient(String baseUrl, String flash) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl) // Replace with your FastAPI server IP
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        flashAddr = flash;
        sentimentService = retrofit.create(SentimentService.class);
    }

    public void predictMoodSentiment(String sentence, Callback<SentimentResponse> callback) {
        SentimentRequest request = new SentimentRequest(sentence);
        Call<SentimentResponse> call = sentimentService.predictMoodSentiment(request);
        call.enqueue(callback);
    }
    public void predictPrayerSentiment(String sentence, Callback<SentimentResponse> callback) {
        SentimentRequest request = new SentimentRequest(sentence);
        Call<SentimentResponse> call = sentimentService.predictPrayerSentiment(request);
        call.enqueue(callback);
    }
}
