package com.example.fyp1.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp1.Model.RecycleRequest;
import com.example.fyp1.R;
import com.example.fyp1.RecycleRequestDetailsActivity;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.List;

public class RecycleRequestAdapter extends RecyclerView.Adapter<RecycleRequestAdapter.ViewHolder> {
    private List<RecycleRequest> recycleRequests;
    private Context context;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public RecycleRequestAdapter(List<RecycleRequest> recycleRequests, Context context) {
        this.recycleRequests = recycleRequests;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle_request, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecycleRequest request = recycleRequests.get(position);
        holder.date.setText(request.getDate());
        holder.itemName.setText(request.getItemName());

        // Set background color based on status
        int backgroundColor;
        switch (request.getRequestStatus()) {
            case "Pending":
                backgroundColor = ContextCompat.getColor(context, R.color.status_pending);
                break;
            case "Approved":
                backgroundColor = ContextCompat.getColor(context, R.color.status_approved);
                break;
            case "Completed":
                backgroundColor = ContextCompat.getColor(context, R.color.status_completed);
                break;
            case "Rejected":
                backgroundColor = ContextCompat.getColor(context, R.color.status_rejected);
                break;
            default:
                backgroundColor = ContextCompat.getColor(context, R.color.white);
                break;
        }

        // Set the background color of the MaterialCardView
        holder.cardView.setCardBackgroundColor(backgroundColor);

        String imageUri = request.getImageUri();
        Log.d("RecycleRequestAdapter", "Loading image URI: " + imageUri);

        Uri uri = Uri.parse(imageUri);
        if (uri.getScheme().equals("content")) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                holder.icon.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Picasso.get().load(uri).into(holder.icon);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecycleRequestDetailsActivity.class);
            intent.putExtra("REQUEST_ID", request.getId());
            intent.putExtra("ITEM_TYPE", request.getItemName());
            intent.putExtra("PICKUP_DATE", request.getDate());
            intent.putExtra("QUANTITY", request.getQuantity());
            intent.putExtra("REQUEST_ADDRESS", request.getRequestAddress());
            intent.putExtra("IMAGE_URL", request.getImageUri());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recycleRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView date, itemName;
        public ImageView icon, arrow;
        public MaterialCardView cardView; // Reference to MaterialCardView

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView); // Assuming you give an ID to MaterialCardView
            date = itemView.findViewById(R.id.tvdateDisplay);
            itemName = itemView.findViewById(R.id.tv_item_name);
            icon = itemView.findViewById(R.id.requestPickUpImage);
            arrow = itemView.findViewById(R.id.iv_arrow);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}
