package com.example.eatnow;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eatnow.model.Restaurant;
import com.example.eatnow.utility.ListConverter;

public class ShowRestaurantDetails extends DialogFragment {

    // UIs
    TextView restaurantWeb;
    TextView restaurantPhone;
    TextView restaurantAddr;
    TextView restaurantArea;
    TextView restaurantCat;
    TextView restaurantFeat;
    ImageView exitImage;

    Restaurant restaurant;

    public ShowRestaurantDetails(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.restaurant_show_details, container, false);

        restaurantWeb = view.findViewById(R.id.restaurant_website);
        restaurantPhone = view.findViewById(R.id.restaurant_phone);
        restaurantAddr = view.findViewById(R.id.restaurant_address);
        restaurantArea = view.findViewById(R.id.restaurant_area);
        restaurantCat = view.findViewById(R.id.restaurant_categories);
        restaurantFeat = view.findViewById(R.id.restaurant_features);

        restaurantWeb.setText(restaurant.getWebsite());
        restaurantPhone.setText(restaurant.getPhone());
        restaurantAddr.setText(restaurant.getAddress());
        restaurantArea.setText(restaurant.getArea());
        restaurantCat.setText(ListConverter.convertStringListToString(restaurant.getCategories()));
        restaurantFeat.setText(ListConverter.convertStringListToString(restaurant.getFeatures()));

        exitImage = view.findViewById(R.id.restaurant_exit_ic);
        exitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
