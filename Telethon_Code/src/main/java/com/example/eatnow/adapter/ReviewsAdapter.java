package com.example.eatnow.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.R;
import com.example.eatnow.ShowRestaurant;
import com.example.eatnow.ShowReview;
import com.example.eatnow.ShowUser;
import com.example.eatnow.model.Account;
import com.example.eatnow.model.Restaurant;
import com.example.eatnow.model.Review;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.ImageConverter;
import com.example.eatnow.utility.TimeConverter;

import java.util.ArrayList;
import java.util.Map;


/*
    Represents the Adapter for reviews
 */
public class ReviewsAdapter extends RecyclerView.Adapter implements ErrorPopUp {

    // Context of activity to display reviews
    Context context;
    // Layout Type 1 : Home Feed, 2 : Restaurant Review, 3 : Profile Review
    int layoutType;
    // List of reviews
    ArrayList<Review> reviews;
    // Dictionary of account ids with accounts
    Map<Integer, Account> accountMap;
    // Dictionary of restaurant ids with restaurants
    Map<Integer, Restaurant> restaurantMap = null;
    // Store selected restaurant
    Restaurant restaurant;

    // Store selected account
    Account account;

    // Constructor to set context and layout type
    public ReviewsAdapter(Context context, int layoutType) {
        this.context = context;
        this.layoutType = layoutType;
    }

    // Mutators and Accessors for reviews
    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    // Setting dictionary of account ids with usernames extracted from Fragment
    public void setAccountMap(Map<Integer, Account> accountMap) {
        this.accountMap = accountMap;
    }

    // Setting dictionary of account ids with restaurants extracted from Fragment
    public void setRestaurantMap(Map<Integer, Restaurant> restaurantMap) {
        this.restaurantMap = restaurantMap;
    }

    // Setting selected restaurant extracted from Fragment
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    // Setting selected account extracted from Fragment
    public void setAccount(Account account) {
        this.account = account;
    }

    // return layout type for view type
    @Override
    public int getItemViewType(int position) {
        return layoutType;
    }

    // Based on view type / layout type, setup layout and view holders
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            // If Home
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviews_home_item, parent, false);
            return new HomeReviewsViewHolder(view);
        } else if (viewType == 2) {
            // If Restaurant
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviews_restaurant_item, parent, false);
            return new RestaurantReviewsViewHolder(view);
        } else {
            // If Account
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviews_user_item, parent, false);
            return new AccountReviewsViewHolder(view);
        }
    }

    // Setting view holder parameters based on view holder type
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Get review based on position
        Review review = reviews.get(position);
        // If there is no review
        if (review == null) {
            return;
        }
        // Retrieving account data based on account id (if needed)
        int account_id;
        Account selectedAccount;
        // If Home
        if (holder instanceof HomeReviewsViewHolder) {
            account_id = review.getAccount_id();
            selectedAccount = accountMap.get(account_id);
            // Set ImageView based on review's image
            if (review.getImage() != null) {
                try {
                    Bitmap bmp = ImageConverter.convertASCIItoBitmap(review.getImage());
                    ((HomeReviewsViewHolder) holder).reviewImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
                } catch (Exception e) {
                }
            }
            // Set Number of Likes based on review
            ((HomeReviewsViewHolder) holder).likesText.setText(String.valueOf(review.getLikes().size()));
            // Set TextView based on account id's username
            ((HomeReviewsViewHolder) holder).usernameText.setText(selectedAccount.getUsername());
            // Set TextView based on review date
            ((HomeReviewsViewHolder) holder).dateText.setText(TimeConverter.convertUnixTStoString(review.getDate()));
            // Set ImageView based on account id's profile picture
            if (selectedAccount.getProfile() != null) {
                try {
                    Bitmap bmp = ImageConverter.convertASCIItoBitmap(selectedAccount.getProfile());
                    ((HomeReviewsViewHolder) holder).accountImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
                } catch (Exception e) {

                }
            } else {
                try {
                    int imageResource = context.getResources().getIdentifier("@drawable/default_profile", null, context.getPackageName());
                    Drawable res = context.getResources().getDrawable(imageResource);
                    ((HomeReviewsViewHolder) holder).accountImage.setImageDrawable(res);
                } catch (Exception e) {
                }
            }
        }
        // If Restaurant
        else if (holder instanceof RestaurantReviewsViewHolder) {
            account_id = review.getAccount_id();
            selectedAccount = accountMap.get(account_id);
            // Set ImageView based on review's image
            if (review.getImage() != null) {
                try {
                    Bitmap bmp = ImageConverter.convertASCIItoBitmap(review.getImage());
                    ((RestaurantReviewsViewHolder) holder).reviewImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
                } catch (Exception e) {
                }
            }
            // Set Caption of review
            ((RestaurantReviewsViewHolder) holder).reviewCaption.setText(review.getCaption());
            // Set Number of Likes based on review
            ((RestaurantReviewsViewHolder) holder).likesText.setText(String.valueOf(review.getLikes().size()));
            // Set TextView based on account id's username
            ((RestaurantReviewsViewHolder) holder).usernameText.setText(selectedAccount.getUsername());
            // Set TextView based on review date
            ((RestaurantReviewsViewHolder) holder).dateText.setText(TimeConverter.convertUnixTStoString(review.getDate()));
            // Set ImageView based on account id's profile picture
            if (selectedAccount.getProfile() != null) {
                try {
                    Bitmap bmp = ImageConverter.convertASCIItoBitmap(selectedAccount.getProfile());
                    ((RestaurantReviewsViewHolder) holder).accountImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
                } catch (Exception e) {
                }
            }
            // Set Rating based on review rating
            ((RestaurantReviewsViewHolder) holder).ratingBar.setRating(review.getRating());
        }
        // If Account
        else {
            // Set ImageView based on review's image
            if (review.getImage() != null) {
                try {
                    Bitmap bmp = ImageConverter.convertASCIItoBitmap(review.getImage());
                    ((AccountReviewsViewHolder) holder).reviewImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
                } catch (Exception e) {
                }
            }
            // Set Caption of review
            ((AccountReviewsViewHolder) holder).reviewCaption.setText(review.getCaption());
            // Set Number of Likes based on review
            ((AccountReviewsViewHolder) holder).likesText.setText(String.valueOf(review.getLikes().size()));
            // Set Rating based on review rating
            ((AccountReviewsViewHolder) holder).ratingBar.setRating(review.getRating());
        }
    }

    // Get number of items in adapter
    @Override
    public int getItemCount() {
        if (reviews != null) {
            return reviews.size();
        }
        return 0;
    }

    @Override
    public void errorPopUp(String title, String error) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(error)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }

    // ViewHolder class for reviews in Home Page
    public class HomeReviewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // UIs
        ImageView reviewImage;
        ImageView accountImage;
        TextView usernameText;
        TextView likesText;
        TextView dateText;

        public HomeReviewsViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setting up UIs
            reviewImage = itemView.findViewById(R.id.reviews_image);
            accountImage = itemView.findViewById(R.id.profilePicture);
            usernameText = itemView.findViewById(R.id.reviews_username);
            likesText = itemView.findViewById(R.id.like_text);
            dateText = itemView.findViewById(R.id.reviews_date);
            // Setting Image as Clickable
            reviewImage.setOnClickListener(this);
        }

        // If Review image is clicked
        @Override
        public void onClick(View view) {
            int id = view.getId();

            if (id == reviewImage.getId()) {
                // If data has yet to load
                if (restaurantMap == null) {
                    errorPopUp("Error", "Please wait for data to load.");
                }
                // Else show the review
                else {
                    // Get the review that was selected
                    Review selectedReview = reviews.get(getAdapterPosition());
                    // Get account that posted the review
                    Account account = accountMap.get(selectedReview.getAccount_id());
                    // Get restaurant that has the review
                    Restaurant restaurant = restaurantMap.get(selectedReview.getRestaurant_id());

                    // Configuration to set dialog fragment (pop-up)
                    FragmentActivity activity = (FragmentActivity) (context);
                    FragmentManager fm = activity.getSupportFragmentManager();

                    DialogFragment showReview = new ShowReview(selectedReview, account, restaurant);
                    showReview.show(fm, "Show Review");
                }
            }
        }
    }

    // ViewHolder class for reviews in Restaurant Page
    public class RestaurantReviewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // UIs
        ImageView reviewImage;
        ImageView accountImage;
        TextView usernameText;
        TextView likesText;
        TextView dateText;
        TextView reviewCaption;
        RatingBar ratingBar;

        public RestaurantReviewsViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setting up UIs
            reviewImage = itemView.findViewById(R.id.reviews_image_res);
            accountImage = itemView.findViewById(R.id.user_image_res);
            usernameText = itemView.findViewById(R.id.user_name_res);
            likesText = itemView.findViewById(R.id.like_text);
            dateText = itemView.findViewById(R.id.reviews_date_res);
            reviewCaption = itemView.findViewById(R.id.reviews_cap_res);
            ratingBar = itemView.findViewById(R.id.reviews_rating_res);
            // Setting View as Clickable
            itemView.setOnClickListener(this);
        }

        // If Review is clicked
        @Override
        public void onClick(View view) {
            int id = view.getId();

            if (id == itemView.getId()) {
                // Get the review that was selected
                Review selectedReview = reviews.get(getAdapterPosition());
                // Get account that posted the review
                Account account = accountMap.get(selectedReview.getAccount_id());
                // Configuration to set dialog fragment (pop-up)
                FragmentActivity activity = (FragmentActivity) (context);
                FragmentManager fm = activity.getSupportFragmentManager();

                Fragment ft = fm.findFragmentByTag("SHOW_RESTAURANT");

                DialogFragment showReview = new ShowReview(selectedReview, account, restaurant);
                // Setting target fragment to pass data from Dialog Fragment to this Fragment.
                showReview.setTargetFragment(ft, ShowRestaurant.SHOW_RESTAURANT);
                showReview.show(fm, "Show Review");
            }
        }
    }

    // ViewHolder class for reviews in Account Page
    public class AccountReviewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // UIs
        ImageView reviewImage;
        TextView likesText;
        TextView reviewCaption;
        RatingBar ratingBar;

        public AccountReviewsViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setting up UIs
            reviewImage = itemView.findViewById(R.id.reviews_image_res);
            likesText = itemView.findViewById(R.id.like_text);
            reviewCaption = itemView.findViewById(R.id.reviews_cap_res);
            ratingBar = itemView.findViewById(R.id.reviews_rating_res);
            // Setting View as Clickable
            itemView.setOnClickListener(this);
        }

        // If Review is clicked
        @Override
        public void onClick(View view) {
            int id = view.getId();

            if (id == itemView.getId()) {
                // If data has yet to load
                if (restaurantMap == null) {
                    errorPopUp("Error", "Please wait for data to load.");
                } else {
                    // Get the review that was selected
                    Review selectedReview = reviews.get(getAdapterPosition());
                    // Get restaurant that has the review
                    Restaurant restaurant = restaurantMap.get(selectedReview.getRestaurant_id());
                    // Configuration to set dialog fragment (pop-up)
                    FragmentActivity activity = (FragmentActivity) (context);
                    FragmentManager fm = activity.getSupportFragmentManager();

                    Fragment ft = fm.findFragmentByTag("SHOW_USER");

                    DialogFragment showReview = new ShowReview(selectedReview, account, restaurant);
                    // Setting target fragment to pass data from Dialog Fragment to this Fragment.
                    showReview.setTargetFragment(ft, ShowUser.SHOW_USER);
                    showReview.show(fm, "Show Review");
                }
            }
        }
    }

}
