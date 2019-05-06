package com.example.rhinspeak.Util;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private static final String TAG = "RetrofitService";
    private static RetrofitService mRetrofitService = new RetrofitService();
    public static RetrofitService getInstance(){
        Log.d(TAG, "getInstance");
        return mRetrofitService;
    }

    private RetrofitService(){ Log.d(TAG, "RetrofitService() 생성자"); };

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://106.10.33.63/speech-pathology/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    IRetrofitService service = retrofit.create(IRetrofitService.class);

    public IRetrofitService getService(){
        Log.d(TAG, "getService()");
        return service;
    }
}
