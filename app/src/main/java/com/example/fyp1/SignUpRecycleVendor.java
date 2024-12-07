package com.example.fyp1;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class SignUpRecycleVendor extends AppCompatActivity {
    private EditText Vendoremail, Vendorpassword, Vendorname, Vendorcontact, VendorRegistraionNumber;
    private Button signupVendorbutton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_recyclevendor);
        Vendoremail = findViewById(R.id.SignUpRecycleVendorEmail);
        Vendorpassword = findViewById(R.id.SignUpRecycleVendorPassword);
        Vendorname = findViewById(R.id.SignupRecycleVendorName);
        Vendorcontact = findViewById(R.id.SIgnUpRecycleVendorContact);
        signupVendorbutton = findViewById(R.id.buttonRecycleSignUp);
        VendorRegistraionNumber = findViewById(R.id.editTextText);
        firebaseAuth = FirebaseAuth.getInstance();

        signupVendorbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtain the entered data
                String email = Vendoremail.getText().toString();
                String password = Vendorpassword.getText().toString();
                String name = Vendorname.getText().toString();
                String contact = Vendorcontact.getText().toString();
                String registration_number = VendorRegistraionNumber.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    MotionToast.Companion.darkToast(SignUpRecycleVendor.this,
                            "Full Name Required",
                            "Please enter your full name",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
                    Vendorname.setError("Full Name is required");
                    Vendorname.requestFocus();
                } else if (TextUtils.isEmpty(email)) {
                    MotionToast.Companion.darkToast(SignUpRecycleVendor.this,
                            "Email Required",
                            "Please enter your email",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
                    Vendoremail.setError("Email is required");
                    Vendoremail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    MotionToast.Companion.darkToast(SignUpRecycleVendor.this,
                            "Valid Email Required",
                            "Valid email is required",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
                    Vendoremail.setError("Valid email is required");
                    Vendoremail.requestFocus();
                } else if (TextUtils.isEmpty(contact)) {
                    MotionToast.Companion.darkToast(SignUpRecycleVendor.this,
                            "Phone Number Required",
                            "Please enter your phone number",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
                    Vendorcontact.setError("Phone No. is required");
                    Vendorcontact.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    MotionToast.Companion.darkToast(SignUpRecycleVendor.this,
                            "Password Required",
                            "Please enter your password",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
                    Vendorpassword.setError("Password is required");
                    Vendorpassword.requestFocus();
                } else if (password.length() < 8) {
                    MotionToast.Companion.darkToast(SignUpRecycleVendor.this,
                            "Password Too Short",
                            "Password should be at least 8 digits",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
                    Vendorpassword.setError("Password should be at least 8 digits");
                    Vendorpassword.requestFocus();
                } else {
                    registerUser(name, email, contact, password, registration_number);
                }
            }
        });
    }

    public void Usergroupselected(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        int radioButtonId = view.getId();
        if (radioButtonId == R.id.radioButtonUser) {
            if (checked) {
                Intent intent = new Intent(this, SignUpUser.class);
                startActivity(intent);
                finish();
            }
        } else if (radioButtonId == R.id.radioButtonRecycleVendor) {
            if (checked) {
                // Handle Recycle Vendor selected
            }
        }
    }

    private void registerUser(String username, String email, String contact, String password, String registration_number) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpRecycleVendor.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                            // Enter User Data into Firebase Realtime Database
                            UserDetails userDetails = new UserDetails(email, password, username, contact, registration_number);

                            // Extracting User reference from Database for "Registered Users"
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance("https://fyp1-7aef5-default-rtdb.firebaseio.com/")
                                    .getReference("Registered Users");

                            referenceProfile.child(firebaseUser.getUid()).setValue(userDetails)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Save email, carbonSaving, and energySaving to "Contributions"
                                                saveContributionData(firebaseUser.getUid(), email);

                                                // Send Verification Email
                                                firebaseUser.sendEmailVerification();

                                                MotionToast.Companion.darkToast(SignUpRecycleVendor.this,
                                                        "Account Registered",
                                                        "Account registered successfully.",
                                                        MotionToastStyle.SUCCESS,
                                                        MotionToast.GRAVITY_BOTTOM,
                                                        MotionToast.LONG_DURATION,
                                                        ResourcesCompat.getFont(SignUpRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));

                                                // Open User Profile after successful registration
                                                Intent intent = new Intent(SignUpRecycleVendor.this, Login.class);
                                                // To Prevent User from returning back to register activity on pressing back button after registration
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish(); // to close Register Activity
                                            } else {
                                                MotionToast.Companion.darkToast(SignUpRecycleVendor.this,
                                                        "Registration Failed",
                                                        "User registration failed. Please try again.",
                                                        MotionToastStyle.ERROR,
                                                        MotionToast.GRAVITY_BOTTOM,
                                                        MotionToast.LONG_DURATION,
                                                        ResourcesCompat.getFont(SignUpRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
                                            }
                                        }
                                    });
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                Vendorpassword.setError("Your password is too weak. Kindly use a mix of alphabets, numbers and special characters");
                                Vendorpassword.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Vendoremail.setError("Your email is invalid or already in use. Kindly re-enter");
                                Vendoremail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Vendoremail.setError("User is already registered with this email. Please use another email.");
                                Vendoremail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                MotionToast.Companion.darkToast(SignUpRecycleVendor.this,
                                        "Error",
                                        e.getMessage(),
                                        MotionToastStyle.ERROR,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        ResourcesCompat.getFont(SignUpRecycleVendor.this, www.sanju.motiontoast.R.font.helveticabold));
                            }
                        }
                    }
                });
    }

    private void saveContributionData(String userId, String email) {
        // Save initial contribution data
        DatabaseReference contributionRef = FirebaseDatabase.getInstance("https://fyp1-7aef5-default-rtdb.firebaseio.com/")
                .getReference("Contributions");

        ContributionDetails contributionDetails = new ContributionDetails(email, 0.0, 0.0);

        contributionRef.child(userId).setValue(contributionDetails)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Failed to save contribution data");
                        }
                    }
                });
    }

    public void backToSignIn(View view) {
        Intent intent = new Intent(SignUpRecycleVendor.this, Login.class);
        startActivity(intent);
        finish();
    }

    // UserDetails class to store user information
    private class UserDetails {
        public String email;
        public String passWord;
        public String fullName;
        public String approveStatus;
        public String role;
        public String contactNumber;
        public String registrationNumber;

        public UserDetails(String email, String passWord, String fullName, String contactNumber, String registrationNumber) {
            this.email = email;
            this.passWord = passWord;
            this.fullName = fullName;
            this.contactNumber = contactNumber;
            this.registrationNumber = registrationNumber;
            this.approveStatus = "Pending";
            this.role = "Recycle Vendor";
        }
    }

    // ContributionDetails class to store contribution information
    private class ContributionDetails {
        public String email;
        public double carbonSaving;
        public double energySaving;

        public ContributionDetails(String email, double carbonSaving, double energySaving) {
            this.email = email;
            this.carbonSaving = carbonSaving;
            this.energySaving = energySaving;
        }
    }
}
