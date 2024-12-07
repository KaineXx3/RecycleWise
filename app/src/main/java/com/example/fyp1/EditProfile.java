package com.example.fyp1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class EditProfile extends AppCompatActivity {

    private TextInputEditText editTextFullName, editTextContactNo, editTextPassword;
    private Button buttonUpload;
    private ImageView profileIcon;
    private DatabaseReference userRef;
    private FirebaseAuth authProfile;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        profileIcon = findViewById(R.id.profileIcon);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextContactNo = findViewById(R.id.editTextContactNo);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonUpload = findViewById(R.id.buttonSaveChanges);

        // Initialize Firebase components
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (view, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;

            view.setPadding(
                    view.getPaddingLeft(),   // Keep original left padding
                    topInset,                // Set top padding to adjust for status bar or notch
                    view.getPaddingRight(),  // Keep original right padding
                    view.getPaddingBottom()  // Keep original bottom padding
            );

            return insets;
        });


        if (firebaseUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(firebaseUser.getUid());
            fetchProfileData();
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set click listener for profileIcon to select image from gallery
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        // Set click listener for save changes button
        buttonUpload.setOnClickListener(v -> {
            String fullName = editTextFullName.getText().toString().trim();
            String contactNo = editTextContactNo.getText().toString().trim();
            String newPassword = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(contactNo)) {
                MotionToast.Companion.darkToast(EditProfile.this,
                        "Incomplete Form",
                        "Please fill all fields",
                        MotionToastStyle.WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(EditProfile.this, www.sanju.motiontoast.R.font.helveticabold));
                return;
            }


            updateProfileAndPassword(fullName, contactNo, newPassword);

            // Optionally, clear the password field after updating
            editTextPassword.setText("");
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                profileIcon.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchProfileData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve data
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String contactNo = dataSnapshot.child("contactNumber").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                    // Update UI with retrieved data
                    editTextFullName.setText(fullName);
                    editTextContactNo.setText(contactNo);

                    // Load image using Picasso if imageUrl is not null
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).into(profileIcon);
                    }
                } else {
                    MotionToast.Companion.darkToast(EditProfile.this,
                            "User Not Found",
                            "User data not found",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(EditProfile.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(EditProfile.this,
                        "Error",
                        databaseError.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(EditProfile.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    private void updateProfileAndPassword(String fullName, String contactNo, String newPassword) {
        // Update profile information
        userRef.child("fullName").setValue(fullName);
        userRef.child("contactNumber").setValue(contactNo);

        // Only update the password in the database if newPassword is not empty
        if (!TextUtils.isEmpty(newPassword)) {
            if (newPassword.length() < 8) {
                MotionToast.Companion.darkToast(EditProfile.this,
                        "Invalid Password Length",
                        "Password must be at least 8 characters long",
                        MotionToastStyle.WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(EditProfile.this, www.sanju.motiontoast.R.font.helveticabold));
                return;
            }
            // Update password in Firebase Authentication
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(aVoid -> MotionToast.Companion.darkToast(EditProfile.this,
                                "Password Updated",
                                "Password updated successfully",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(EditProfile.this, www.sanju.motiontoast.R.font.helveticabold)))
                        .addOnFailureListener(e -> MotionToast.Companion.darkToast(EditProfile.this,
                                "Password Update Failed",
                                e.getMessage(),
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(EditProfile.this, www.sanju.motiontoast.R.font.helveticabold)));
            }
            // Update password in Firebase Realtime Database
            userRef.child("passWord").setValue(newPassword); // Assuming 'passWord' is the password field
        } else {

        }

        // Upload image if imageUri is not null
        if (imageUri != null) {
            // Example of uploading image to Firebase Storage
            // Replace "profile_pictures/" with your desired path in Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_pictures/" + authProfile.getCurrentUser().getUid());
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the uploaded image URL
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Save image URL to Firebase Database under "imageUrl"
                            userRef.child("imageUrl").setValue(uri.toString());
                            MotionToast.Companion.darkToast(EditProfile.this,
                                    "Profile Updated",
                                    "Profile updated successfully",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(EditProfile.this, www.sanju.motiontoast.R.font.helveticabold));

                            Intent intent = new Intent(EditProfile.this, NormalUserHome.class);
                            startActivity(intent);
                            finish();


                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle error uploading image
                        MotionToast.Companion.darkToast(EditProfile.this,
                                "Image Upload Failed",
                                "Failed to upload image",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(EditProfile.this, www.sanju.motiontoast.R.font.helveticabold));
                    });
        } else {
            // If no image is selected, just update profile data
            MotionToast.Companion.darkToast(EditProfile.this,
                    "Profile Updated",
                    "Profile updated successfully",
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(EditProfile.this, www.sanju.motiontoast.R.font.helveticabold));
            Intent intent = new Intent(EditProfile.this, NormalUserHome.class);
            startActivity(intent);
            finish();
        }
    }

}
