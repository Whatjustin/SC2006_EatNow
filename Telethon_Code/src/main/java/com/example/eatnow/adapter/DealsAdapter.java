package com.example.eatnow.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatnow.R;
import com.example.eatnow.ShowDeal;
import com.example.eatnow.model.Deal;
import com.example.eatnow.utility.ImageConverter;

import java.util.ArrayList;

/*
    Represents the Adapter for Deals
 */
public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.DealsViewHolder> {

    // Context of activity to display deals
    Context context;
    // List of deals
    ArrayList<Deal> deals = null;

    // Constructor to set context
    public DealsAdapter(Context context) {
        this.context = context;
    }

    // Mutators and Accessors for deals
    public ArrayList<Deal> getDeals() {
        return deals;
    }

    public void setDeals(ArrayList<Deal> deals) {
        this.deals = deals;
        notifyDataSetChanged();
    }

    // Construct view holder
    @NonNull
    @Override
    public DealsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deals_item, parent, false);
        return new DealsViewHolder(view);
    }

    // Setting view holder parameters
    @Override
    public void onBindViewHolder(@NonNull DealsViewHolder holder, int position) {
        // Get Deal based on position
        Deal deal = deals.get(position);
        // If there is no deal
        if (deal == null) {
            return;
        }
        // Set ImageView based on Deal's image
        try {
            Bitmap bmp = ImageConverter.convertASCIItoBitmap(deal.getImage());
            holder.imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));
        } catch (Exception e) {
        }
        // Set TextView based on Deal's brand
        holder.textView.setText(deal.getBrand());
    }

    // Get number of items in adapter
    @Override
    public int getItemCount() {
        if (deals != null) {
            return deals.size();
        }
        return 0;
    }

    // ViewHolder class for deals
    public class DealsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // deal's image
        ImageView imageView;
        // deal's brand
        TextView textView;

        public DealsViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.deals_image);
            textView = itemView.findViewById(R.id.deals_text);

            // To set image as clickable
            imageView.setOnClickListener(this);
        }

        // If ImageView is clicked, display popup to display deal
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == imageView.getId()) {

                // Get the deal that was selected
                Deal selectedDeal = deals.get(getAdapterPosition());

                // Configuration to set dialog fragment (pop-up)
                FragmentActivity activity = (FragmentActivity) (context);
                FragmentManager fm = activity.getSupportFragmentManager();

                DialogFragment showDeal = ShowDeal.newInstance(selectedDeal);
                showDeal.show(fm, "Show Deal");

                // Toast.makeText(imageView.getContext(), "Clicked on Deal.", Toast.LENGTH_LONG).show();
            }
        }
    }

}
