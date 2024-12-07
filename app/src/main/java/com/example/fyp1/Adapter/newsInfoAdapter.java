package com.example.fyp1.Adapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp1.EditNews;
import com.example.fyp1.Model.NewsModel;
import com.example.fyp1.NewsDetailActivity;
import com.example.fyp1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class newsInfoAdapter extends RecyclerView.Adapter<newsInfoAdapter.ViewHolder> {

    private ArrayList<NewsModel> newsList;
    private Context context;
    private String currentUserId; // To hold current user ID

    public newsInfoAdapter(ArrayList<NewsModel> newsList, Context context) {
        this.newsList = newsList;
        this.context = context;
        // Get the current user's ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_news_layouts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsModel model = newsList.get(position);
        holder.title.setText(model.getTitle());
        String fullDate = model.getDate();
        String shortDate = fullDate.length() > 10 ? fullDate.substring(0, 10) : fullDate;
        holder.date.setText(shortDate);

        if (model.getImageUrl() != null && !model.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(model.getImageUrl())
                    .placeholder(R.drawable.defaultimage)
                    .error(R.drawable.defaultimage)
                    .into(holder.newImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("Picasso", "Image loaded successfully: " + model.getImageUrl());
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("Picasso", "Error loading image: " + model.getImageUrl(), e);
                        }
                    });
        } else {
            holder.newImageView.setImageResource(R.drawable.defaultimage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewsDetailActivity.class);
            intent.putExtra("imageUrl", model.getImageUrl());
            intent.putExtra("title", model.getTitle());
            intent.putExtra("date", model.getDate());
            intent.putExtra("news_information", model.getInformation());
            context.startActivity(intent);
        });

        // Only allow long-click actions for Admin users
        checkUserRole(holder, model);
    }

    private void checkUserRole(ViewHolder holder, NewsModel model) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(currentUserId).child("role");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && "Admin".equals(dataSnapshot.getValue(String.class))) {
                    // If user is Admin, set long-click listener
                    holder.itemView.setOnLongClickListener(v -> {
                        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Choose Action to perform")
                                .setContentText("Select an option:")
                                .setCancelText("Edit")
                                .setConfirmText("Delete")
                                .showCancelButton(true)
                                .setCancelClickListener(sDialog -> {
                                    Intent intent = new Intent(context, EditNews.class);
                                    intent.putExtra("newsId", model.getNewsId()); // Pass the newsId to the next activity
                                    context.startActivity(intent);
                                    sDialog.dismiss(); // Dismiss the dialog after action
                                })
                                .setConfirmClickListener(sDialog -> {
                                    deleteItemFromFirebase(model.getNewsId()); // Assuming you have an ID in NewsModel
                                    notifyItemRemoved(holder.getAdapterPosition()); // Notify adapter of item removed
                                    notifyItemRangeChanged(holder.getAdapterPosition(), newsList.size()); // Update range
                                    sDialog.dismiss(); // Dismiss the dialog after action
                                })
                                .show();
                        return true;
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error checking user role", databaseError.toException());
            }
        });
    }

    private void deleteItemFromFirebase(String newsId) {
        DatabaseReference newsRef = FirebaseDatabase.getInstance().getReference("news");
        newsRef.child(newsId).removeValue()
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "News item deleted successfully."))
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to delete news item.", e));
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView newImageView;
        private TextView date, title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateText);
            title = itemView.findViewById(R.id.titleText);
            newImageView = itemView.findViewById(R.id.newsImage);
        }
    }
}
