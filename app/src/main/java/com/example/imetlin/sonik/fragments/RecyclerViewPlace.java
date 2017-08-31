package com.example.imetlin.sonik.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
//import com.example.imetlin.sonik.GoogleMaps;

import com.example.imetlin.sonik.R;


public class RecyclerViewPlace extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RecyclerViewPlace() {
        // Required empty public constructor
    }


    public static RecyclerViewPlace newInstance(String param1, String param2) {
        RecyclerViewPlace fragment = new RecyclerViewPlace();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recyclerviewplace, container, false);

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab_add);
        assert fab != null;

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

               // Intent intent = new Intent(getContext(), GoogleMaps.class);
                //intent.putExtra("newsURL", uri);
               // startActivityForResult(intent, 1);

            }
        });
        return v;
    }





    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
