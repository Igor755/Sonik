package com.example.imetlin.sonik.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.imetlin.sonik.R;
import com.google.android.gms.location.places.PlaceBuffer;

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
        holder.nameTextView.setText(placeName);
        holder.adressTextView.setText(placeAdress);

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

        public PlaceViewHolder(View itemView){
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            adressTextView = (TextView) itemView.findViewById(R.id.adressTextView);
        }

    }


}
