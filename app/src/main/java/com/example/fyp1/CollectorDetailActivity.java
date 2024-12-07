package com.example.fyp1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CollectorDetailActivity extends AppCompatActivity {

    private ImageView locationImage;
    private TextView nameTextView, emailTextView, phoneTextView, locationTextView;
    private Button navigateButton;
    private String fullAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recycle_location_detail);

        // Initialize views
        locationImage = findViewById(R.id.locationImage);
        nameTextView = findViewById(R.id.nameDescriptionTextView);
        emailTextView = findViewById(R.id.emailDescriptionTextView);
        phoneTextView = findViewById(R.id.phoneDescriptionTextView);
        locationTextView = findViewById(R.id.locationDescriptionTextView);
        navigateButton = findViewById(R.id.navigateButton);

        // Retrieve data from the intent
        String locationID = getIntent().getStringExtra("locationID");

        // Fetch and display the details
        fetchCollectorDetails(locationID);

        // Set click listener for the navigate button
        navigateButton.setOnClickListener(v -> {
            if (fullAddress != null && !fullAddress.isEmpty()) {
                openGoogleMaps(fullAddress);
            }
        });
    }

    private void fetchCollectorDetails(String locationID) {
        DatabaseReference collectorRef = FirebaseDatabase.getInstance().getReference("RecycleLocation").child(locationID);

        collectorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String companyName = snapshot.child("companyName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String postcode = snapshot.child("postcode").getValue(String.class);
                    String province = snapshot.child("province").getValue(String.class);
                    String state = snapshot.child("state").getValue(String.class);

                    // Combine location, postcode, province, and state
                    fullAddress = location + ", " + postcode + ", " + province + ", " + state;

                    // Set the data to views
                    nameTextView.setText(companyName);
                    emailTextView.setText(email);
                    locationTextView.setText(fullAddress);

                    // Load image using Picasso
                    Picasso.get().load(imageUrl).into(locationImage);

                    // Fetch phone number based on email
                    fetchPhoneNumber(email);
                } else {
                    // Handle case where data does not exist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void fetchPhoneNumber(String email) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Registered Users");

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String phone = userSnapshot.child("contactNumber").getValue(String.class);
                        phoneTextView.setText(phone);
                    }
                } else {
                    // Handle case where no matching user is found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void openGoogleMaps(String address) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
