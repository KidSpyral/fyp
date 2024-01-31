package com.example.fyp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SentimentService {
    @POST("/predict")
    Call<SentimentResponse> predictSentiment(@Body SentimentRequest request);
}

