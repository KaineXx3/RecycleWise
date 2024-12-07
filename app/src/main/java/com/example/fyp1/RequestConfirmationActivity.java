package com.example.fyp1;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class RequestConfirmationActivity extends AppCompatActivity {

    private TextView recyclableItem, quantity, pickupDate, selectedCollector, locationAddress;
    private Button buttonConfirm, buttonCancel;
    private ImageView imageViewDisplay;

    private String VendorlocationID, locationAddr, date, item, imageUri, userEmail, collectorName;
    private int quant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_request_confirmation);

        recyclableItem = findViewById(R.id.recyclable_item);
        quantity = findViewById(R.id.quantity);
        pickupDate = findViewById(R.id.pickup_date);
        selectedCollector = findViewById(R.id.selected_collector);
        locationAddress = findViewById(R.id.pickuplocation);
        imageViewDisplay = findViewById(R.id.img_display_photo);

        buttonCancel = findViewById(R.id.button_cancel);
        buttonConfirm = findViewById(R.id.button_confirm);

        // Retrieve data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            VendorlocationID = intent.getStringExtra("locationID");
            locationAddr = intent.getStringExtra("location_address");
            quant = intent.getIntExtra("quantity", -1);
            date = intent.getStringExtra("pickup_date");
            item = intent.getStringExtra("recyclable_item");
            imageUri = intent.getStringExtra("image_uri");
            userEmail = intent.getStringExtra("user_Email");
            collectorName = intent.getStringExtra("collector_Name");

            recyclableItem.setText(item);
            quantity.setText(String.valueOf(quant) + "kg");
            pickupDate.setText(date);
            selectedCollector.setText(collectorName);
            locationAddress.setText(locationAddr);

            // Load image using Picasso
            if (imageUri != null && !imageUri.isEmpty()) {
                Picasso.get()
                        .load(imageUri)
                        .placeholder(R.drawable.defaultimage) // Placeholder image
                        .into(imageViewDisplay);
            } else {
                imageViewDisplay.setImageResource(R.drawable.defaultimage); // Default image
            }
        }

        buttonCancel.setOnClickListener(v -> {
            showCancelConfirmationDialog();
        });

        buttonConfirm.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                MotionToast.Companion.darkToast(this,
                        "No Internet Connection",
                        "Please check your internet connection.",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold));
                return; // Exit if there is no internet connection
            }

            if (imageUri != null) {
                MotionToast.Companion.darkToast(RequestConfirmationActivity.this,
                        "Uploading Pickup Request...",
                        "Uploading pick up request data",
                        MotionToastStyle.INFO,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(RequestConfirmationActivity.this, www.sanju.motiontoast.R.font.helveticabold));

                uploadImageToFirebaseStorage(Uri.parse(imageUri));
            } else {
                MotionToast.Companion.darkToast(RequestConfirmationActivity.this,
                        "Image URI Unavailable",
                        "Image URI is not available",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(RequestConfirmationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Cancellation")
                .setMessage("Are you sure you want to cancel this request?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(RequestConfirmationActivity.this, NormalUserHome.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        // Define the file name for the image
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.ENGLISH);
        Date now = new Date();
        String fileName = formatter.format(now) + ".jpg";

        // Get a reference to Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to the file path in Firebase Storage
        StorageReference imageRef = storageRef.child("images/" + fileName);

        // Upload the image
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        savePickupRequestToFirebase(imageUrl);
                    }).addOnFailureListener(exception -> {
                        MotionToast.Companion.darkToast(RequestConfirmationActivity.this,
                                "Image URL Unavailable",
                                "Failed to get image URL",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(RequestConfirmationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    });
                })
                .addOnFailureListener(exception -> {
                    MotionToast.Companion.darkToast(RequestConfirmationActivity.this,
                            "Upload Failed",
                            "Failed to upload image",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(RequestConfirmationActivity.this, www.sanju.motiontoast.R.font.helveticabold));

                });
    }

    private void savePickupRequestToFirebase(String imageUrl) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("RecycleRequest");

        String requestId = ref.push().getKey(); // Generate unique ID for the request

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("VendorlocationID", VendorlocationID);
        requestData.put("RequestAddress", locationAddr);
        requestData.put("Quantity", quant);
        requestData.put("PickUpDate", date);
        requestData.put("ItemType", item);
        requestData.put("imageUri", imageUrl); // Use the image URL from Firebase Storage
        requestData.put("userEmail", userEmail);
        requestData.put("requestStatus", "Pending");
        requestData.put("requestID", requestId);

        if (requestId != null) {
            ref.child(requestId).setValue(requestData)
                    .addOnSuccessListener(aVoid -> {
                        MotionToast.Companion.darkToast(RequestConfirmationActivity.this,
                                "Pickup Request Confirmed",
                                "Pickup request confirmed",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(RequestConfirmationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                        Intent intent = new Intent(RequestConfirmationActivity.this, MakeRecycleRequestList.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        MotionToast.Companion.darkToast(RequestConfirmationActivity.this,
                                "Pickup Confirmation Failed",
                                "Failed to confirm pickup request",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(RequestConfirmationActivity.this, www.sanju.motiontoast.R.font.helveticabold));

                    });
        } else {
            MotionToast.Companion.darkToast(RequestConfirmationActivity.this,
                    "Request ID Generation Failed",
                    "Failed to generate request ID",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(RequestConfirmationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
        }
    }
}
