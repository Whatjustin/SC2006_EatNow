package com.example.eatnow;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eatnow.fragment.DealsFragment;
import com.example.eatnow.model.Deal;
import com.example.eatnow.utility.ListConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
    Represents Dialog Fragment (Pop-up) for filtering deal
 */
public class FilterDeal extends DialogFragment {

    // CONSTANTS to check action code in parent fragment
    public static final int DEAL_FILTER = 2;
    public static final int RESET_DEAL_FILTER = 3;
    // List of filters
    ArrayList<String> brands;
    ArrayList<String> cuisines;
    ArrayList<String> locations;
    // UIs
    Button setFilter;
    ImageView exitButton;
    AutoCompleteTextView brandTextView;
    AutoCompleteTextView cuisineTextView;
    AutoCompleteTextView locationTextView;
    ArrayAdapter<String> brandAdapter;
    ArrayAdapter<String> cuisineAdapter;
    ArrayAdapter<String> locationAdapter;
    // User's selected filters
    String brandFilter = "";
    String cuisineFilter = "";
    String locationFilter = "";

    // Constructor for Dialog Fragment
    public static FilterDeal newInstance(ArrayList<Deal> deals) {

        // Get list of filters for brands, cuisines, and locations
        ArrayList<String> brands = new ArrayList<String>();
        ArrayList<String> cuisines = new ArrayList<String>();
        ArrayList<String> locations = new ArrayList<String>();

        String location;

        for (Deal deal : deals) {
            if (!(brands.contains(deal.getBrand()))) {
                brands.add(deal.getBrand());
            }

            if (!(cuisines.contains(deal.getCuisine()))) {
                cuisines.add(deal.getCuisine());
            }

            location = deal.getLocations();
            if (!location.isEmpty()) {
                List<String> splitted = ListConverter.convertStringToListByComma(location);
                for (String split : splitted) {
                    if (!(locations.contains(split))) {
                        locations.add(split);
                    }
                }
            }
        }
        // Sorting the ArrayList in alphabetical order
        Collections.sort(brands);
        Collections.sort(cuisines);
        Collections.sort(locations);
        // Creation of bundle to store data
        Bundle args = new Bundle();
        args.putStringArrayList("brand", brands);
        args.putStringArrayList("cuisine", cuisines);
        args.putStringArrayList("location", locations);
        // Creation of fragment and setting bundle
        FilterDeal fragment = new FilterDeal();
        fragment.setArguments(args);
        return fragment;
    }

    // Called when view is created
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.deals_filter_popup, container, false);

        // Setting up UIs
        brandTextView = view.findViewById(R.id.brand_text_filter);
        cuisineTextView = view.findViewById(R.id.cuisine_text_filter);
        locationTextView = view.findViewById(R.id.location_text_filter);
        // Retrieving data from bundle
        brands = getArguments().getStringArrayList("brand");
        cuisines = getArguments().getStringArrayList("cuisine");
        locations = getArguments().getStringArrayList("location");
        // Setting up Adapters based on filters
        brandAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.dropdown_item, brands);
        cuisineAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.dropdown_item, cuisines);
        locationAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.dropdown_item, locations);
        brandTextView.setAdapter(brandAdapter);
        cuisineTextView.setAdapter(cuisineAdapter);
        locationTextView.setAdapter(locationAdapter);

        // Setting up clickable UIs for TextViews
        brandTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                brandFilter = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getActivity(), brandFilter, Toast.LENGTH_SHORT).show();
            }
        });

        cuisineTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cuisineFilter = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getActivity(), cuisineFilter, Toast.LENGTH_SHORT).show();
            }
        });

        locationTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                locationFilter = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getActivity(), locationFilter, Toast.LENGTH_SHORT).show();
            }
        });

        // Setting up set filter button to confirm filter
        setFilter = view.findViewById(R.id.button_set_filter);
        setFilter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();

                bundle.putString("brand", brandFilter);
                bundle.putString("cuisine", cuisineFilter);
                bundle.putString("location", locationFilter);

                Intent intent = new Intent().putExtras(bundle);
                // Callback to parent fragment
                getTargetFragment().onActivityResult(DealsFragment.DEAL_FRAGMENT, DEAL_FILTER, intent);

                getDialog().dismiss();
            }
        });

        // Setting up exit button to exit
        exitButton = view.findViewById(R.id.deal_filter_exit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    // Adjust Layout of Pop-up to maximize size
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
