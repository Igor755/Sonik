package com.example.imetlin.sonik.photo;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.example.imetlin.sonik.R;
import com.example.imetlin.sonik.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by i.metlin on 11.09.2017.
 */
public class GooglePlacePhoto {

    String photoUrl = String.format("https://maps.googleapis.com/maps/api/place/photo?maxwidth=%s&photoreference=%s&key=%s");
}
/*


    public Place[] parse(JSONObject jObject){

        JSONArray jPlaces = null;
        try {

            jPlaces = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jPlaces);
    }


    private Place[] getPlaces(JSONArray jPlaces){
        int placesCount = jPlaces.length();
        Place[] places = new Place[placesCount];


        for(int i=0; i<placesCount;i++){
            try {

                places[i] = getPlace((JSONObject)jPlaces.get(i));


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return places;
    }


    private Place getPlace(JSONObject jPlace){

        Place place = new Place();



        try {
            // Extracting Place name, if available
            if(!jPlace.isNull("name")){
                place.mPlaceName = jPlace.getString("name");
            }

            // Extracting Place Vicinity, if available
            if(!jPlace.isNull("vicinity")){
                place.mVicinity = jPlace.getString("vicinity");
            }

            if(!jPlace.isNull("photos")){
                JSONArray photos = jPlace.getJSONArray("photos");
                place.mPhotos = new Photo[photos.length()];
                for(int i=0;i<photos.length();i++){
                    place.mPhotos[i] = new Photo();
                    place.mPhotos[i].mWidth = ((JSONObject)photos.get(i)).getInt("width");
                    place.mPhotos[i].mHeight = ((JSONObject)photos.get(i)).getInt("height");
                    place.mPhotos[i].mPhotoReference = ((JSONObject)photos.get(i)).getString("photo_reference");
                    JSONArray attributions = ((JSONObject)photos.get(i)).getJSONArray("html_attributions");
                    place.mPhotos[i].mAttributions = new Attribution[attributions.length()];
                    for(int j=0;j<attributions.length();j++){
                        place.mPhotos[i].mAttributions[j] = new Attribution();
                        place.mPhotos[i].mAttributions[j].mHtmlAttribution = attributions.getString(j);
                    }
                }
            }

            place.mLat = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            place.mLng = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");



        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("EXCEPTION", e.toString());
        }
        return place;
    }
}*/