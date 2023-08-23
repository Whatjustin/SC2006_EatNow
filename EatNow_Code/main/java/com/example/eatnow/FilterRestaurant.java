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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eatnow.fragment.SearchFragment;

import java.util.ArrayList;
import java.util.Collections;

/*
    Represents Dialog Fragment (Pop-up) for filtering restaurants
 */
public class FilterRestaurant extends DialogFragment {
    // CONSTANTS to check action code in parent fragment
    public static final int RESTAURANT_FILTER = 12;
    public static final int RESET_RESTAURANT_FILTER = 13;
    static final String[] features = {"Reservable", "Not Reservable"};
    // List of filters
    ArrayList<String> areas;
    ArrayList<String> categories;
    /// UIs
    Button setFilter;
    ImageView exitButton;
    AutoCompleteTextView areaTextView;
    AutoCompleteTextView categoryTextView;
    AutoCompleteTextView featureTextView;
    ArrayAdapter<String> areaAdapter;
    ArrayAdapter<String> categoryAdapter;
    ArrayAdapter<String> featureAdapter;
    // Store user's selected filters
    String areaFilter = "";
    String categoryFilter = "";
    String featureFilter = "";

    // Constructor for Dialog Fragment
    public FilterRestaurant(ArrayList<String> areas, ArrayList<String> categories) {
        // Sort in alphabetical order
        Collections.sort(areas);
        Collections.sort(categories);
        this.areas = areas;
        this.categories = categories;
    }

    // Called when view is created
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.restaurants_filter_popup, container, false);
        // Setting up UIs
        areaTextView = view.findViewById(R.id.area_text_filter);
        categoryTextView = view.findViewById(R.id.category_text_filter);
        featureTextView = view.findViewById(R.id.feature_text_filter);
        // Setting up adapters based on filters
        areaAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.dropdown_item, areas);
        categoryAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.dropdown_item, categories);
        featureAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.dropdown_item, features);
        areaTextView.setAdapter(areaAdapter);
        categoryTextView.setAdapter(categoryAdapter);
        featureTextView.setAdapter(featureAdapter);

        // Setting up clickable UIs for TextViews
        areaTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                areaFilter = adapterView.getItemAtPosition(i).toString();
            }
        });
        categoryTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                categoryFilter = adapterView.getItemAtPosition(i).toString();
            }
        });
        featureTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                featureFilter = adapterView.getItemAtPosition(i).toString();
            }
        });
        // Setting up set filter button to confirm filter
        setFilter = view.findViewById(R.id.button_set_filter);
        setFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();

                bundle.putString("area", areaFilter);
                bundle.putString("category", categoryFilter);
                bundle.putString("feature", featureFilter);

                Intent intent = new Intent().putExtras(bundle);
                // Callback to parent fragment
                getTargetFragment().onActivityResult(SearchFragment.SEARCH_FRAGMENT, RESTAURANT_FILTER, intent);

                getDialog().dismiss();
            }
        });
        // Setting up exit button to exit
        exitButton = view.findViewById(R.id.restaurant_filter_exit);
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
