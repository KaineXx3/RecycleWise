package com.example.fyp1;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ViewDetailRequestFromUser extends AppCompatActivity {

    private TextView tvAddress, tvItemDescription, tvDate, tvContactNumber, tvStatus, tvQuantity, Remark, statusRemark;
    private ImageView imageView,contactIcon;
    private FloatingActionButton expandedMenu, expandedEdit, expandedDelete;

    private boolean isFabExpanded = false;
    private static final String TAG = "ViewDetailRequest";
    private String requestId;
    private String contactNumber;
    private static final int REQUEST_CALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_detail_request_from_user);

        initializeViews();
        setupFabMenu();
        setupContactIcon();

        Intent intent = getIntent();
        requestId = intent.getStringExtra("requestId");
        contactNumber = intent.getStringExtra("contactNumber");

        if (requestId != null && contactNumber != null) {
            fetchRequestDetails(requestId, contactNumber);
        } else {
            //Toast.makeText(this, "Request ID or Contact Number is missing.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        tvAddress = findViewById(R.id.tvAddress);
        tvItemDescription = findViewById(R.id.tvItemDescription);
        tvDate = findViewById(R.id.tvDate);
        tvContactNumber = findViewById(R.id.tvContactNumber);
        tvStatus = findViewById(R.id.tvStatus);
        tvQuantity = findViewById(R.id.tvItemQuantity);
        imageView = findViewById(R.id.imageView);

        expandedMenu = findViewById(R.id.expanded_menu);
        expandedEdit = findViewById(R.id.expanded_edit);
        expandedDelete = findViewById(R.id.expanded_delete);

        Remark = findViewById(R.id.Remark);
        statusRemark = findViewById(R.id.statusRemark);

        contactIcon = findViewById(R.id.contact_icon);
    }

    private void setupFabMenu() {
        expandedMenu.setOnClickListener(view -> toggleFabMenu());

        expandedEdit.setOnClickListener(view -> {
            if (requestId != null && contactNumber != null) {
                Intent intent = new Intent(ViewDetailRequestFromUser.this, EditRequestFromUser.class);
                intent.putExtra("requestId", requestId);
                intent.putExtra("contactNumber", contactNumber);
                startActivity(intent);
            } else {
                //Toast.makeText(this, "Request ID or Contact Number is missing.", Toast.LENGTH_SHORT).show();
            }
        });

        expandedDelete.setOnClickListener(view -> {
            if ("Completed".equals(tvStatus.getText().toString())) {
                showDeleteConfirmationDialog();
            } else {
                MotionToast.Companion.darkToast(ViewDetailRequestFromUser.this,
                        "Deletion Restricted",
                        "Only completed requests can be deleted.",
                        MotionToastStyle.WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ViewDetailRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    private void toggleFabMenu() {
        if (!isFabExpanded) {
            expandFabMenu();
        } else {
            collapseFabMenu();
        }
    }
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Request")
                .setMessage("Are you sure you want to delete this completed request?")
                .setPositiveButton("Delete", (dialog, which) -> deleteRequest())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRequest() {
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("RecycleRequest").child(requestId);
        requestRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                MotionToast.Companion.darkToast(ViewDetailRequestFromUser.this,
                        "Request Deleted",
                        "Request deleted successfully",
                        MotionToastStyle.SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ViewDetailRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold));
                finish(); // Close the activity after deletion
            } else {
                MotionToast.Companion.darkToast(ViewDetailRequestFromUser.this,
                        "Deletion Failed",
                        "Failed to delete request",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ViewDetailRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    private void expandFabMenu() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        float buttonSpacing = screenHeight * 0.007f;

        expandedEdit.setVisibility(View.VISIBLE);
        expandedDelete.setVisibility(View.VISIBLE);

        expandedEdit.animate().translationY(-buttonSpacing).alpha(1f).setDuration(300);
        expandedDelete.animate().translationY(-buttonSpacing * 2).alpha(1f).setDuration(300);
        expandedMenu.animate().rotation(45f).setDuration(300);
        isFabExpanded = true;
    }

    private void collapseFabMenu() {
        expandedEdit.animate().translationY(0).alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isFabExpanded) {
                    expandedEdit.setVisibility(View.GONE);
                }
            }
        });
        expandedDelete.animate().translationY(0).alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isFabExpanded) {
                    expandedDelete.setVisibility(View.GONE);
                }
            }
        });
        expandedMenu.animate().rotation(0f).setDuration(300);
        isFabExpanded = false;
    }

    private void setupContactIcon() {
        contactIcon.setOnClickListener(v -> showCallConfirmationDialog());
    }

    private void showCallConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Make a call")
                .setMessage("Do you want to call " + tvContactNumber.getText() + "?")
                .setPositiveButton("Call", (dialog, which) -> makePhoneCall())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void makePhoneCall() {
        String phoneNumber = tvContactNumber.getText().toString();
        if (phoneNumber.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(ViewDetailRequestFromUser.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ViewDetailRequestFromUser.this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + phoneNumber;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        } else {
            Toast.makeText(ViewDetailRequestFromUser.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                MotionToast.Companion.darkToast(ViewDetailRequestFromUser.this,
                        "Error",
                        "Permission DENIED",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ViewDetailRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        }
    }

    private void fetchRequestDetails(String requestID, String contactNumber) {
        Log.d(TAG, "Fetching request details for requestId: " + requestID);
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("RecycleRequest").child(requestID);
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Snapshot: " + snapshot.toString());
                if (snapshot.exists()) {
                    String pickUpDate = snapshot.child("PickUpDate").getValue(String.class);
                    Integer quantity = snapshot.child("Quantity").getValue(Integer.class);
                    String requestAddress = snapshot.child("RequestAddress").getValue(String.class);
                    String imageUri = snapshot.child("imageUri").getValue(String.class);
                    String requestStatus = snapshot.child("requestStatus").getValue(String.class);
                    String itemType = snapshot.child("ItemType").getValue(String.class);

                    updateUI(pickUpDate, quantity, requestAddress, imageUri, requestStatus, itemType, contactNumber);
                } else {
                    runOnUiThread(() -> MotionToast.Companion.darkToast(ViewDetailRequestFromUser.this,
                            "Error",
                            "Request not found in database",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(ViewDetailRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage() + ", Details: " + error.getDetails());
                runOnUiThread(() -> MotionToast.Companion.darkToast(ViewDetailRequestFromUser.this,
                        "Error",
                        error.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ViewDetailRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold)));
            }
        });
    }

    private void updateUI(String pickUpDate, Integer quantity, String requestAddress, String imageUri, String requestStatus, String itemType, String contactNumber) {
        runOnUiThread(() -> {
            tvAddress.setText(requestAddress);
            tvItemDescription.setText(itemType);
            if (quantity != null) {
                tvQuantity.setText(String.valueOf(quantity) + " kg");
            } else {
                tvQuantity.setText("Unknown quantity");
            }
            tvDate.setText(pickUpDate);
            tvContactNumber.setText(contactNumber);
            tvStatus.setText(requestStatus);

            if (imageUri != null && !imageUri.isEmpty()) {
                loadImage(imageUri);
            } else {
                Log.w(TAG, "Image URI is null or empty");
                imageView.setImageResource(R.drawable.defaultimage);
            }

            if (!"Pending".equals(requestStatus)) {
                Remark.setVisibility(View.VISIBLE);
                statusRemark.setVisibility(View.VISIBLE);
                fetchRemark(requestId);
            } else {
                Remark.setVisibility(View.GONE);
                statusRemark.setVisibility(View.GONE);
            }
        });
    }

    private void fetchRemark(String requestId) {
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("RecycleRequest").child(requestId);
        requestRef.child("Remark").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String remark = snapshot.getValue(String.class);
                if (remark != null) {
                    statusRemark.setText(remark);
                } else {
                    statusRemark.setText("No remarks available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching remark: " + error.getMessage());
                statusRemark.setText("Error fetching remarks");
            }
        });
    }

    private void loadImage(String imageUri) {
        Picasso.get().load(imageUri).into(imageView);
    }
}