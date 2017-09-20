package com.example.imetlin.sonik.model;

import com.google.android.gms.location.places.Places;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by i.metlin on 20.09.2017.
 */

public class MyItemsJson {
    public class Place {
        @SerializedName("place_id")
        private String placeID;
        @SerializedName("description")
        private String description;
        @SerializedName("types")
        private String[] types;
        @SerializedName("predictions")
        private List<Places> predictions;
        @SerializedName("status")
        private String status;




        public Place(String placeID, String description, String[] types){
            this.placeID = placeID;
            this.description = description;
            this.types = types;
        }

        public String getPlaceID(){
            return placeID;
        }
        public String getDescription(){
            return description;
        }
        public List<Places> getPlaces(){
            return predictions;
        }
        public String getStatus(){
            return  status;
        }
        @Override
        public String toString(){
            return "Place{" +
                    "description=" + description + "/" +"}";
        }
    }
}
