package com.example.eatnow.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.R;
import com.example.eatnow.ShowUser;
import com.example.eatnow.model.Account;
import com.example.eatnow.utility.ImageConverter;

import java.util.ArrayList;

/*
    Represents the Adapter for users
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    // Context of activity to display users
    Context context;
    // List of users
    ArrayList<Account> users;

    // Constructor to set context
    public UsersAdapter(Context context) {
        this.context = context;
    }

    // Mutators and Accessors for users
    public ArrayList<Account> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<Account> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    // Construct view holder
    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_item, parent, false);
        return new UsersViewHolder(view);
    }

    // Setting view holder parameters
    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UsersViewHolder holder, int position) {
        // Get User based on position
        Account user = users.get(position);
        // If there is no user
        if (user == null) {
            return;
        }
        // Set ImageView based on User's image
        holder.imageView.setImageDrawable(null);
        if (user.getProfile() != null) {
            try {
                Bitmap bmp = ImageConverter.convertASCIItoBitmap(user.getProfile());
                holder.imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
            } catch (Exception e) {
            }
        }
        else {
            try{
                Drawable placeholder = holder.imageView.getContext().getResources().getDrawable(R.drawable.default_profile);
                holder.imageView.setImageDrawable(placeholder);
            } catch (Exception e) {
            }
        }
        // Set TextView based on User's name
        holder.textView.setText(user.getUsername());
    }

    // Get number of items in adapter
    @Override
    public int getItemCount() {
        if (users != null) {
            return users.size();
        }
        return 0;
    }

    // ViewHolder class for accounts
    public class UsersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // UIs
        ImageView imageView;
        TextView textView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setting up UIs
            imageView = itemView.findViewById(R.id.user_image);
            textView = itemView.findViewById(R.id.user_name);
            // To set view as clickable
            itemView.setOnClickListener(this);
        }

        // If view is clicked
        @Override
        public void onClick(View view) {
            int id = view.getId();

            if (id == itemView.getId()) {
                // Get selected restaurant
                Account selectedUser = users.get(getAdapterPosition());

                // Configuring to change fragment to show user
                FragmentActivity activity = (FragmentActivity) (context);
                FragmentManager fm = activity.getSupportFragmentManager();
                FrameLayout contentView = (FrameLayout) activity.findViewById(R.id.fragment_container);
                Fragment showUser = new ShowUser(selectedUser);
                fm.beginTransaction()
                        .replace(contentView.getId(), showUser, "SHOW_USER")
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
}
