package com.example.imetlin.sonik.model;

import com.example.imetlin.sonik.internet.Link;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by i.metlin on 20.09.2017.
 */

public class PlaseDetails {

    public class AddressComponent {
        String long_name;
        String short_name;
        String[] types;
    }

    public class Photo {
        int height;
        int width;
        String photo_reference;
    }

    public class Place {

        @SerializedName("place_id")
        private String place_id;

        @SerializedName("name")
        private String name;

        @SerializedName("address_components")
        private List<AddressComponent> address_components;

        @SerializedName("formatted_address")
        private String formatted_address;

        @SerializedName("photos")
        private List<Photo> photos;

        @SerializedName("result")
        private Place result;

        @SerializedName("status")
        private String status;


        public Place getPlace() {
            return result;
        }

        public String getID() {
            return place_id;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return formatted_address;
        }

        public List<Photo> getPhotos() {
            return photos;
        }

        public String getPhotoURL(int maxwidth) {
            if (!photos.isEmpty()) {
                String key = Link.KEY;
                Photo photo = photos.get(0);
                String ref = photo.photo_reference;
                String url = String.format("https://maps.googleapis.com/maps/api/place/photo?maxwidth=%d&photoreference=%s&key=%s", maxwidth, ref, key);
                return url;
            }
            return null;
        }

        @Override
        public String toString() {
            return "Place{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }


}

