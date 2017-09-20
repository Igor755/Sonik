package com.example.imetlin.sonik.internet;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by i.metlin on 20.09.2017.
 */

public class RetroClient {

    private static Retrofit retrofit = null;
    public static final String GOOG_URL = "https://maps.googleapis.com/maps/api/place/";

    private static Retrofit getClient(){

        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(GOOG_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }
        return retrofit;
    }
}
