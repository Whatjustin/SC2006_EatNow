package com.example.eatnow;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eatnow.model.Deal;
import com.example.eatnow.utility.ImageConverter;

/*
    Represents Dialog Fragment (Pop-up) for showing deal
 */
public class ShowDeal extends DialogFragment {

    // Temporary storage for image and message
    byte[] image;
    String message;
    // UIs
    ImageView exitButton;
    ImageView imageView;
    TextView textView;

    // Constructor for Dialog Fragment
    public static ShowDeal newInstance(Deal deal) {

        // Stored in bundle to allow access to data upon creation
        Bundle args = new Bundle();
        args.putByteArray("image", deal.getImage());
        args.putString("message", deal.getText());

        // Create fragment and set bundle
        ShowDeal fragment = new ShowDeal();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.deal_popup, container, false);

        imageView = view.findViewById(R.id.deal_popup_image);
        textView = view.findViewById(R.id.deal_popup_text);

        // Retrieve data from bundle
        image = getArguments().getByteArray("image");
        message = getArguments().getString("message");

        // Set ImageView based on deal's image
        try {
            Bitmap bmp = ImageConverter.convertASCIItoBitmap(image);
            imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
        } catch (Exception e) {
        }
        // Set TextView based on deal's text
        textView.setText(message);
        textView.setMovementMethod(new ScrollingMovementMethod());

        // Set up exit button to close pop-up
        exitButton = view.findViewById(R.id.deal_popup_exit);
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
