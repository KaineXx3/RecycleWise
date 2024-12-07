package com.example.fyp1;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ProfileRecycleVendor extends AppCompatActivity {
    private TextView textViewName;
    private FirebaseAuth authProfile;
    private ImageView profilePicture;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_recyclevendor);

        profilePicture = findViewById(R.id.profilepic);
        textViewName = findViewById(R.id.textViewNameRecycleVendor);
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(firebaseUser.getUid());
            fetchUserData();
        }
    }

    private void fetchUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch and set user name
                    String name = dataSnapshot.child("fullName").getValue(String.class);
                    if (name != null) {
                        textViewName.setText(name);
                    }

                    // Fetch and load user profile image
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    if (imageUrl != null) {
                        Picasso.get().load(imageUrl).into(profilePicture);
                    }
                } else {
                    MotionToast.Companion.darkToast(ProfileRecycleVendor.this,
                            "Error",
                            "User data not found",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(ProfileRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(ProfileRecycleVendor.this,
                        "Error",
                        databaseError.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ProfileRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    public void toEditProfile(View view) {
        Intent intent = new Intent(ProfileRecycleVendor.this, EditProfile.class);
        startActivity(intent);
    }

    public void toLogOutEndActivity(View view) {
        // Sign out the user from Firebase
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ProfileRecycleVendor.this, Login.class); // Replace Login with your actual login activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish the current activity
        finish();
        // Show a toast message
        MotionToast.Companion.darkToast(ProfileRecycleVendor.this,
                "Logged Out",
                "Logged out successfully",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(ProfileRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
    }

    public void toEditRecycleLocation(View view) {
        Intent intent = new Intent(ProfileRecycleVendor.this, EditRecycleLocationActivity.class);
        startActivity(intent);
    }
}
