package com.example.imetlin.sonik.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.imetlin.sonik.R;
import com.google.android.gms.location.places.PlaceBuffer;
import com.squareup.picasso.Picasso;
import com.example.imetlin.sonik.adapter.PlaceListAdapter;
import com.example.imetlin.sonik.base.MyBase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;



/**
 * Created by i.metlin on 04.09.2017.
 */

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {

    private Context mContext;
    private PlaceBuffer mPlaces;

    public PlaceListAdapter(Context context, PlaceBuffer placeBuffer){
        this.mContext = context;
        this.mPlaces = placeBuffer;

    }



    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.one_list_item,parent,false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {



        String placeName = mPlaces.get(position).getName().toString();
        String placeAdress = mPlaces.get(position).getAddress().toString();
        String placePhoto = mPlaces.get(position).getId().toString();
        Picasso.with(mContext).load(mPlaces.get(position).getId().toString());

        holder.nameTextView.setText(placeName);
        holder.adressTextView.setText(placeAdress);
        holder.imageView.setImageURI(Uri.parse(placePhoto));


       // PlacePhotoMetadataBuffer photoMetadataBuffer;
       // PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
// Get a full-size bitmap for the photo.
      //  Bitmap image = photo.getPhoto(mClient).await()
            //    .getBitmap();

    }
    public void SwapPlaces(PlaceBuffer newPlaces){
        mPlaces = newPlaces;
        if (mPlaces != null){
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        if (mPlaces == null)
        return 0;
        return mPlaces.getCount();
    }
    class PlaceViewHolder extends RecyclerView.ViewHolder{

        TextView nameTextView;
        TextView adressTextView;
        ImageView imageView;

        public PlaceViewHolder(View itemView){
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            adressTextView = (TextView) itemView.findViewById(R.id.adressTextView);
            imageView = (ImageView) itemView.findViewById(R.id.myImage);
        }

    }


}
