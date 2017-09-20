package com.example.imetlin.sonik.internet;

import com.example.imetlin.sonik.model.MyItemsJson;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;



/**
 * Created by i.metlin on 20.09.2017.
 */

public interface Link {


    public static final String KEY = "AIzaSyBZPXHUCqXQFVOSsnBUVcNvl8ABg25L13I";


    @GET("details/json")
    Call<MyItemsJson> getMyJson(@Query("key") String key,
                                @Query("placeid") String placeid);

}
