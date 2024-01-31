package com.example.fyp;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SentimentApiClient {
    private final SentimentService sentimentService;

    public SentimentApiClient(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl) // Replace with your FastAPI server IP
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sentimentService = retrofit.create(SentimentService.class);
    }

    public void predictSentiment(String sentence, Callback<SentimentResponse> callback) {
        SentimentRequest request = new SentimentRequest(sentence);
        Call<SentimentResponse> call = sentimentService.predictSentiment(request);
        call.enqueue(callback);
    }
}
