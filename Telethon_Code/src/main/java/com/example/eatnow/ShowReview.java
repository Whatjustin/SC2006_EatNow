package com.example.eatnow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eatnow.fragment.HomeFragment;
import com.example.eatnow.model.Account;
import com.example.eatnow.model.Restaurant;
import com.example.eatnow.model.Review;
import com.example.eatnow.model.UserAccount;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.ImageConverter;
import com.example.eatnow.utility.ImageURLDownload;
import com.example.eatnow.utility.ListConverter;
import com.example.eatnow.utility.TimeConverter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ShowReview extends DialogFragment implements ErrorPopUp {

    public static final int REVIEW_LIKE_CHANGE = 10;
    Review review;
    Account account;
    Restaurant restaurant;

    boolean liked;
    boolean change = false;

    ImageView reviewImage;
    TextView reviewCaption;
    TextView reviewText;
    TextView reviewDate;

    ImageView profileImage;
    TextView usernameText;

    ImageView restaurantImage;
    TextView restaurantText;

    RatingBar ratingBar;

    TextView numLikes;
    MaterialButton likeBtn;

    ImageView exitImage;

    UserAccount user = UserAccount.getInstance();

    public ShowReview(Review review, Account account, Restaurant restaurant) {
        this.review = review;
        this.account = account;
        this.restaurant = restaurant;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.review_popup, container, false);

        reviewImage = view.findViewById(R.id.review_image);
        reviewCaption = view.findViewById(R.id.review_caption);
        reviewText = view.findViewById(R.id.review_text);
        reviewDate = view.findViewById(R.id.reviews_date);

        if (review.getImage() != null) {
            try {
                Bitmap bmp = ImageConverter.convertASCIItoBitmap(review.getImage());
                reviewImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
            } catch (Exception e) {
            }
        }

        reviewCaption.setText(review.getCaption());

        reviewText.setText(review.getText());

        reviewDate.setText(TimeConverter.convertUnixTStoString(review.getDate()));

        ratingBar = view.findViewById(R.id.rating_review_bar);
        ratingBar.setRating(review.getRating());

        numLikes = view.findViewById(R.id.number_likes);
        likeBtn = view.findViewById(R.id.like_button);

        numLikes.setText(String.valueOf(review.getLikes().size()));

        if (user != null) {
            liked = review.getLikes().contains(user.getAccount_id());

            likeBtn.setSelected(liked);

            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                        change = true;

                        int account_id = user.getAccount_id();

                        if (liked) {
                            review.getLikes().remove(Integer.valueOf(account_id));
                            liked = false;
                        } else {
                            review.getLikes().add(account_id);
                            liked = true;
                        }

                        likeBtn.setSelected(liked);

                        updateReviewInDatabase(review);

                        numLikes.setText(String.valueOf(review.getLikes().size()));
                }
            });
        } else {
            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    errorPopUp("Login Required.",
                            "Please log in or register with us to like the review. " +
                                    "You may log in or register under the profile tab.");
                }
            });
        }

        profileImage = view.findViewById(R.id.profilePicture);
        usernameText = view.findViewById(R.id.profile_username);

        if (account.getProfile() != null) {
            try {
                Bitmap bmp = ImageConverter.convertASCIItoBitmap(account.getProfile());
                profileImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
            } catch (Exception e) {
            }
        }

        usernameText.setText(account.getUsername());

        restaurantImage = view.findViewById(R.id.restaurant_picture);
        restaurantText = view.findViewById(R.id.restaurant_name);

        if (!restaurant.getImage_link().isEmpty()) {
            try {
                new ImageURLDownload(restaurantImage).execute(restaurant.getImage_link());
            } catch (Exception exception) {
            }
        }

        restaurantText.setText(restaurant.getName());

        exitImage = view.findViewById(R.id.review_popup_exit);

        exitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (change) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("review_id", review.getReview_id());
                    bundle.putIntegerArrayList("likes", review.getLikes());
                    Intent intent = new Intent().putExtras(bundle);

                    if (getTargetFragment() instanceof HomeFragment) {
                        getTargetFragment().onActivityResult(HomeFragment.HOME_FRAGMENT, REVIEW_LIKE_CHANGE, intent);
                    } else if (getTargetFragment() instanceof ShowRestaurant) {
                        getTargetFragment().onActivityResult(ShowRestaurant.SHOW_RESTAURANT, REVIEW_LIKE_CHANGE, intent);
                    }
                }

                getDialog().dismiss();

            }
        });

        return view;
    }

    private void updateReviewInDatabase(Review review) {
        int review_id = review.getReview_id();

        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("reviews").child(String.valueOf(review_id - 1)).child("likes");

        reference.setValue(ListConverter.convertIntsToString(review.getLikes()));
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
    public void errorPopUp(String title, String error) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(error)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }
}
