package com.example.imetlin.sonik.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.imetlin.sonik.GoogleMaps;

import com.example.imetlin.sonik.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;


public class RecyclerViewPlace extends Fragment implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {


    private static int PERMISSION_REQUEST_FINE_LOCATION = 111;
    private static int PLACE_PICKER_REQUEST = 1;

    //private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private boolean mIsEnabled;
    private GoogleApiClient mClient;







    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recyclerviewplace, container, false);

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab_add);
        assert fab != null;

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

               Intent intent = new Intent(getContext(), GoogleMaps.class);
                //intent.putExtra("newsURL", uri);
               startActivityForResult(intent, 1);

            }
        });
        return v;
    }





    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
