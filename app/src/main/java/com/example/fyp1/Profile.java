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

public class Profile extends AppCompatActivity {
    private TextView textViewName;
    private ImageView profilePicture;
    private FirebaseAuth authProfile;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profilePicture = findViewById(R.id.profilepic);
        textViewName = findViewById(R.id.nameDisplayProfile);
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(firebaseUser.getUid());
            fetchUserName();
            fetchUserProfilePicture();
        }


    }

    private void fetchUserName() {
        userRef.child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.getValue(String.class);
                    textViewName.setText(name);
                } else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(Profile.this,
                        "Error",
                        databaseError.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(Profile.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    private void fetchUserProfilePicture() {
        userRef.child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String imageUrl = dataSnapshot.getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // Use Picasso to load the image into the ImageView
                        Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.defaultprofile) // Optional: A default image while loading
                                .error(R.drawable.defaultprofile) // Optional: An error image if there's a problem loading
                                .into(profilePicture);
                    }
                } else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(Profile.this,
                        "Error",
                        databaseError.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(Profile.this, www.sanju.motiontoast.R.font.helveticabold));

            }
        });
    }

    public void toEditProfile(View view) {
        Intent intent = new Intent(Profile.this, EditProfile.class);
        startActivity(intent);
    }

    public void toLogOutEndActivity(View view){
        // Sign out the user from Firebase
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Profile.this, Login.class); // Replace LoginActivity with your actual login activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);



        // Finish the current activity
        finish();
        //Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        MotionToast.Companion.darkToast(Profile.this,
                "Logged Out",
                "Logged out successfully",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(Profile.this, www.sanju.motiontoast.R.font.helveticabold));
        // Show a toast message

    }
}
