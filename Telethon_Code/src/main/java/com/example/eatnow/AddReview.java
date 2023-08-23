package com.example.eatnow;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eatnow.model.Restaurant;
import com.example.eatnow.model.UserAccount;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.ImageConverter;
import com.example.eatnow.utility.TimeConverter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/*
    Represents Dialog Fragment to show pop-up for adding review.
 */
public class AddReview extends DialogFragment implements ErrorPopUp {
    // CONSTANT to check action code in parent fragment
    public static final int ADD_REVIEW = 20;
    // CONSTANT to check successful upload image
    static final int UPLOAD_IMAGE = 22;
    // CONSTANTS to limit user's inputs
    static final int MAX_CHAR_CAPTION = 100;
    static final int MAX_CHAR_TEXT = 300;
    // UIs
    RatingBar ratingBar;
    TextInputEditText captionText;
    TextInputEditText commentText;
    ImageView reviewImage;
    MaterialButton addPhoto;
    MaterialButton submitButton;
    ImageView exit;
    // Store restaurant to add the review in
    Restaurant restaurant;
    // Store list of ratings of reviews for that restaurant
    ArrayList<Float> ratings;
    // To check whether ratings^^ data has been retrieved
    boolean retrievedRatings = false;
    // Store new review id to attach to review
    int new_review_id;
    // To check if this new id has been retrieved.
    boolean retrievedReviewID = false;
    // Store image for review.
    byte[] image = null;

    // Constructor for class
    public AddReview(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    // Called on create view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.review_add_popup, container, false);
        // To retrieve necessary data from database
        retrieveReviewID();
        retrieveReviewRatings();
        // Setting up UIs
        ratingBar = view.findViewById(R.id.rating_review_bar);
        captionText = view.findViewById(R.id.caption_edit_text);
        commentText = view.findViewById(R.id.text_edit_text);
        reviewImage = view.findViewById(R.id.review_image);
        addPhoto = view.findViewById(R.id.add_photo_button);
        submitButton = view.findViewById(R.id.submit_button);
        exit = view.findViewById(R.id.add_review_popup_exit);
        // Setting add photo button as clickable
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, UPLOAD_IMAGE);
            }
        });
        // Setting submit review button as clickable
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if data has been retrieved
                if (retrievedReviewID && retrievedRatings) {
                    float rating = ratingBar.getRating();
                    String caption = captionText.getText().toString();
                    String text = commentText.getText().toString();

                    if (verifyReview(rating, caption, text)) {
                        addReviewData(rating, caption, text);
                        updateRestaurantData(rating);
                        // Call parent fragment to update restaurant data
                        Intent intent = new Intent();
                        getTargetFragment().onActivityResult(ShowRestaurant.SHOW_RESTAURANT, ADD_REVIEW, intent);
                        getDialog().dismiss();
                    }
                } else {
                    // If data has not been retrieved, display error
                    errorPopUp("Error", "Please wait for data to load.");
                }

            }
        });
        // Setting exit image as clickable
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        // Setting Counter to TextInputLayout (Caption and Comment)
        TextInputLayout textInputLayout;
        // For caption
        textInputLayout = view.findViewById(R.id.textInputLayout);
        textInputLayout.setCounterEnabled(true);
        textInputLayout.setCounterMaxLength(MAX_CHAR_CAPTION);
        // For caption
        textInputLayout = view.findViewById(R.id.textInputLayout2);
        textInputLayout.setCounterEnabled(true);
        textInputLayout.setCounterMaxLength(MAX_CHAR_TEXT);

        // Limit Text for TextInputEditText (Caption and Comment)
        captionText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHAR_CAPTION)});
        commentText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHAR_TEXT)});

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPLOAD_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImage);
                    image = getBytes(inputStream);
                    reviewImage.setImageURI(selectedImage);
                } catch (FileNotFoundException e) {
                    errorPopUp("Error", "Unable to upload image.");
                } catch (IOException e) {
                    errorPopUp("Error", "Unable to upload image.");
                }
            }
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // Method to add review into database
    private void addReviewData(float rating, String caption, String text) {

        HashMap<String, Object> review = new HashMap<>();

        review.put("account_id", UserAccount.getInstance().getAccount_id());
        review.put("caption", caption);
        review.put("date", TimeConverter.getSGTUnixTS());
        if (image == null) {
            review.put("image", "");
        } else {
            review.put("image", ImageConverter.convertBinaryToASCIIString(image));
        }
        review.put("likes", "");
        review.put("rating", rating);
        review.put("restaurant_id", restaurant.getRestaurant_id());
        review.put("review_id", new_review_id);
        review.put("text", text);

        // Adding Review to database
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("reviews").child(String.valueOf(new_review_id - 1));
        reference.setValue(review);

        // Updating last review id
        reference = FirebaseDatabase.getInstance().getReference().child("id_counter").child("review_id");
        reference.setValue(new_review_id);
    }

    // Method to update restaurant data based on review
    private void updateRestaurantData(float rating) {
        // Add new rating into the old list
        ratings.add(rating);
        // Calculate new overall rating
        float overallRating = 0;
        for (float i : ratings) {
            overallRating += i;
        }
        overallRating /= ratings.size();
        // Update database based on new review
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference()
                .child("restaurants")
                .child(String.valueOf(restaurant.getRestaurant_id() - 1))
                .child("rating");
        reference.setValue(overallRating);

        reference = FirebaseDatabase.getInstance().getReference()
                .child("restaurants")
                .child(String.valueOf(restaurant.getRestaurant_id() - 1))
                .child("reviews");
        reference.setValue(ratings.size());

        // Update restaurant based on new review
        restaurant.setRating(overallRating);
        restaurant.setReviews(ratings.size());
    }

    /*
        Method to verify review based on user's inputs
        Review must:
        1) have a rating i.e. more than 0 (0.5 - 5)
        2) have a caption i.e. cannot be empty
        3) have a comment i.e. cannot be empty

        Review's image can be empty. In this case, default picture is used.
     */
    private boolean verifyReview(float rating, String caption, String text) {
        // Check rating
        if (!(rating > 0)) {
            errorPopUp("No rating given", "Please enter a rating from 0.5 to 5.");
            return false;
        }
        // Check caption
        if (caption.isEmpty()) {
            errorPopUp("No caption given", "Please enter a caption.");
            return false;
        }
        // Check text/comment
        if (text.isEmpty()) {
            errorPopUp("No comment given", "Please enter a comment.");
            return false;
        }
        return true;
    }

    // Retrieving last review id from firebase. Set retrievedReviewID to true once done.
    private void retrieveReviewID() {
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("id_counter").child("review_id");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                new_review_id = dataSnapshot.getValue(Integer.class);
                // Set new review id to +1
                new_review_id += 1;
                retrievedReviewID = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Retrieve a list of ratings of the restaurant. Set retrievedRatings to true once done.
    private void retrieveReviewRatings() {

        ratings = new ArrayList<>();

        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("reviews");
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                float rate;
                int restaurant_id;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    restaurant_id = snapshot.child("restaurant_id").getValue(int.class);

                    if (restaurant_id == restaurant.getRestaurant_id()) {
                        rate = snapshot.child("rating").getValue(float.class);
                        ratings.add(rate);
                    }
                }

                retrievedRatings = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

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

    // Construct Error Pop Up
    @Override
    public void errorPopUp(String title, String error) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(error)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }
}
