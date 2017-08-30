package com.example.imetlin.sonik.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.imetlin.sonik.R;
import com.example.imetlin.sonik.modelclass.ItemCafe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by i.metlin on 30.08.2017.
 */

public class RecyclerView extends Fragment {


    private RecyclerView listCafe;
    private List<ItemCafe> items;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recyclerview, container, false);

        items = new ArrayList<>();

     

        return v;
    }


}

        //RecyclerView = (RecyclerView) v.findViewById(R.id.list_item);


        //DividerItemDecoration divider = new DividerItemDecoration(listCafe.getContext(), DividerItemDecoration.VERTICAL);
       // divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.deviderik));
       // listCafe.addItemDecoration(divider);

/*
        adapter = new MyAdapter(database.getAllData(), getActivity(), new OnRecyclerClickListener() {


            @Override
            public void onItemClick(View v, int position, String uri) {
                Intent intent = new Intent(getContext(), GoogleMaps.class);
                intent.putExtra("newsURL", uri);
                startActivityForResult(intent, 1);


            }
        });
        listCafe.setLayoutManager(new LinearLayoutManager(getActivity()));
        listCafe.setAdapter(adapter);


    }
}
*/