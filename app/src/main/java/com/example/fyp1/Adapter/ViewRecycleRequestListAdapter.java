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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fyp1.Model.ViewRecycleRequestFromUserModel;
import com.example.fyp1.R;
import com.example.fyp1.ViewDetailRequestFromUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.google.android.material.card.MaterialCardView; // Import MaterialCardView

import java.util.List;

public class ViewRecycleRequestListAdapter extends RecyclerView.Adapter<ViewRecycleRequestListAdapter.ViewHolder> {

    private List<ViewRecycleRequestFromUserModel> recycleRequestList;
    private Context context;

    public ViewRecycleRequestListAdapter(Context context, List<ViewRecycleRequestFromUserModel> recycleRequestList) {
        this.context = context;
        this.recycleRequestList = recycleRequestList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViewRecycleRequestFromUserModel request = recycleRequestList.get(position);
        Log.d("Adapter", "ItemType: " + request.getItemType() + ", PickUpDate: " + request.getPickUpDate() + ", ImageUri: " + request.getImageUri());
        holder.tvItemName.setText(request.getItemType());
        holder.tvDateDisplay.setText(request.getPickUpDate());
        Picasso.get().load(request.getImageUri()).into(holder.requestPickUpImage);

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
        holder.cardView.setCardBackgroundColor(backgroundColor); // Change the background color

        holder.itemView.setOnClickListener(v -> {
            fetchContactNumber(request);
        });
    }

    private void fetchContactNumber(ViewRecycleRequestFromUserModel request) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Registered Users");
        userRef.orderByChild("email").equalTo(request.getUserEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String contactNumber = userSnapshot.child("contactNumber").getValue(String.class);
                        if (contactNumber != null) {
                            Intent intent = new Intent(context, ViewDetailRequestFromUser.class);
                            intent.putExtra("requestId", request.getRequestID());
                            intent.putExtra("contactNumber", contactNumber);
                            String toastMessage = "Request ID: " + request.getRequestID() +
                                    "\nContact Number: " + contactNumber;
                            //Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
                            context.startActivity(intent);
                        }
                    }
                } else {
                    Log.e("Adapter", "No user found with email: " + request.getUserEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Adapter", "Error fetching user data", error.toException());
            }
        });
    }

    @Override
    public int getItemCount() {
        return recycleRequestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvItemName, tvDateDisplay;
        public ImageView requestPickUpImage;
        public MaterialCardView cardView; // Declare MaterialCardView

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView); // Initialize MaterialCardView
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvDateDisplay = itemView.findViewById(R.id.tvdateDisplay);
            requestPickUpImage = itemView.findViewById(R.id.requestPickUpImage);
        }
    }
}
