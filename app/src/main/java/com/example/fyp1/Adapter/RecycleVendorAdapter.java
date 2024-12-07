package com.example.fyp1.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp1.ManageRegisterRequest;
import com.example.fyp1.Model.RecycleVendor;
import com.example.fyp1.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class RecycleVendorAdapter extends RecyclerView.Adapter<RecycleVendorAdapter.ViewHolder> {

    private Context context;
    private List<RecycleVendor> vendorList;
    private DatabaseReference databaseReference;

    public RecycleVendorAdapter(Context context, List<RecycleVendor> vendorList) {
        this.context = context;
        this.vendorList = vendorList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_manage_register_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecycleVendor vendor = vendorList.get(position);

        holder.tvName.setText(vendor.getFullName());
        holder.tvRegistrationNumber.setText(vendor.getRegistrationNumber());
        holder.tvEmail.setText(vendor.getEmail());

        holder.rejectButton.setOnClickListener(v -> showConfirmationDialog(vendor, "Rejected"));
        holder.approveButton.setOnClickListener(v -> showConfirmationDialog(vendor, "Approved"));

        if (position == vendorList.size() - 1) {
            holder.itemView.setBackgroundResource(R.drawable.item_border_manage_user_last);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.item_border_manage_user);
        }
    }

    private void showConfirmationDialog(RecycleVendor vendor, String newStatus) {
        new AlertDialog.Builder(context)
                .setTitle("Confirm Action")
                .setMessage("Are you sure you want to " + (newStatus.equals("Approved") ? "approve" : "reject") + " this vendor?")
                .setPositiveButton("Yes", (dialog, which) -> updateVendorStatus(vendor, newStatus))
                .setNegativeButton("No", null)
                .show();
    }

    private void updateVendorStatus(RecycleVendor vendor, String newStatus) {
        databaseReference.child(vendor.getUserId()).child("approveStatus").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (context instanceof Activity) {
                        MotionToast.Companion.darkToast(
                                (Activity) context, // Cast context to Activity
                                "Status Updated",
                                "Vendor status updated to " + newStatus,
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(context, www.sanju.motiontoast.R.font.helveticabold)
                        );
                    }
                    vendor.setApproveStatus(newStatus);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRegistrationNumber, tvEmail;
        ImageButton rejectButton, approveButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRegistrationNumber = itemView.findViewById(R.id.tv_registration_number);
            tvEmail = itemView.findViewById(R.id.tv_email);
            rejectButton = itemView.findViewById(R.id.reject);
            approveButton = itemView.findViewById(R.id.approve);
        }
    }
}