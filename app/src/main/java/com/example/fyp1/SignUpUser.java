package com.example.fyp1;

import static android.content.ContentValues.TAG;

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

public class SignUpUser extends AppCompatActivity {

    private EditText Useremail, Userpassword, Username, Usercontact;
    private Button signupbutton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_user);
        Useremail = findViewById(R.id.SignUpEmail);
        Userpassword = findViewById(R.id.SignUpPassword);
        Username = findViewById(R.id.SignupName);
        Usercontact = findViewById(R.id.SIgnUpContact);
        signupbutton = findViewById(R.id.buttonSignup);
        firebaseAuth = FirebaseAuth.getInstance();

        if (signupbutton == null) {
            Log.e("SignUpUser", "signupbutton is null");
        } else {
            Log.d("SignUpUser", "signupbutton found successfully");
        }

        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtain the entered data
                String email = Useremail.getText().toString();
                String password = Userpassword.getText().toString();
                String name = Username.getText().toString();
                String contact = Usercontact.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    MotionToast.Companion.darkToast(SignUpUser.this,
                            "Full Name Required",
                            "Please enter your full name",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpUser.this, www.sanju.motiontoast.R.font.helveticabold));
                    Username.setError("Full Name is required");
                    Username.requestFocus();
                } else if (TextUtils.isEmpty(email)) {
                    MotionToast.Companion.darkToast(SignUpUser.this,
                            "Email Required",
                            "Please enter your email",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpUser.this, www.sanju.motiontoast.R.font.helveticabold));
                    Useremail.setError("Email is required");
                    Useremail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    MotionToast.Companion.darkToast(SignUpUser.this,
                            "Valid Email Required",
                            "Valid email is required",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpUser.this, www.sanju.motiontoast.R.font.helveticabold));
                    Useremail.setError("Valid email is required");
                    Useremail.requestFocus();
                } else if (TextUtils.isEmpty(contact)) {
                    MotionToast.Companion.darkToast(SignUpUser.this,
                            "Phone Number Required",
                            "Please enter your phone number",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpUser.this, www.sanju.motiontoast.R.font.helveticabold));
                    Usercontact.setError("Phone No. is required");
                    Usercontact.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    MotionToast.Companion.darkToast(SignUpUser.this,
                            "Password Required",
                            "Please enter your password",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpUser.this, www.sanju.motiontoast.R.font.helveticabold));
                    Userpassword.setError("Password is required");
                    Userpassword.requestFocus();
                } else if (password.length() < 8) {
                    MotionToast.Companion.darkToast(SignUpUser.this,
                            "Password Too Short",
                            "Password should be at least 8 digits",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(SignUpUser.this, www.sanju.motiontoast.R.font.helveticabold));
                    Userpassword.setError("Password should be at least 8 digits");
                    Userpassword.requestFocus();
                } else {
                    registerUser(name, email, contact, password);
                }
            }
        });
    }

    public void Usergroupselected(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        int radioButtonId = view.getId();
        if (radioButtonId == R.id.radioButtonUser) {
            if (checked) {
                // Handle user radio button selection
            }
        } else if (radioButtonId == R.id.radioButtonRecycleVendor) {
            if (checked) {
                Intent intent = new Intent(this, SignUpRecycleVendor.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void registerUser(String username, String email, String contact, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpUser.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                            // Enter User Data into Firebase Realtime Database
                            UserDetails userDetails = new UserDetails(email, password, username, contact);

                            // Extracting User reference from Database for "Registered Users"
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance("https://fyp1-7aef5-default-rtdb.firebaseio.com/")
                                    .getReference("Registered Users");

                            referenceProfile.child(firebaseUser.getUid()).setValue(userDetails)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Save initial contribution data
                                                saveInitialContributionData(firebaseUser.getUid(), email);

                                                // Send Verification Email
                                                firebaseUser.sendEmailVerification();

                                                MotionToast.Companion.darkToast(SignUpUser.this,
                                                        "Account Registered",
                                                        "Account registered successfully.",
                                                        MotionToastStyle.SUCCESS,
                                                        MotionToast.GRAVITY_BOTTOM,
                                                        MotionToast.LONG_DURATION,
                                                        ResourcesCompat.getFont(SignUpUser.this, www.sanju.motiontoast.R.font.helveticabold));

                                                // Open User Profile after successful registration
                                                Intent intent = new Intent(SignUpUser.this, Login.class);
                                                // To Prevent User from returning back to register activity on pressing back button after registration
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish(); // to close Register Activity
                                            } else {
                                                MotionToast.Companion.darkToast(SignUpUser.this,
                                                        "Registration Failed",
                                                        "User registration failed. Please try again.",
                                                        MotionToastStyle.ERROR,
                                                        MotionToast.GRAVITY_BOTTOM,
                                                        MotionToast.LONG_DURATION,
                                                        ResourcesCompat.getFont(SignUpUser.this, www.sanju.motiontoast.R.font.helveticabold));
                                            }
                                        }
                                    });
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                Userpassword.setError("Your password is too weak. Kindly use a mix of alphabets, numbers and special characters");
                                Userpassword.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Useremail.setError("Your email is invalid or already in use. Kindly re-enter");
                                Useremail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Useremail.setError("User is already registered with this email. Please use another email.");
                                Useremail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                MotionToast.Companion.darkToast(SignUpUser.this,
                                        "Error",
                                        e.getMessage(),
                                        MotionToastStyle.ERROR,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        ResourcesCompat.getFont(SignUpUser.this, www.sanju.motiontoast.R.font.helveticabold));
                            }
                        }
                    }
                });
    }

    private void saveInitialContributionData(String userId, String email) {
        DatabaseReference referenceContribution = FirebaseDatabase.getInstance("https://fyp1-7aef5-default-rtdb.firebaseio.com/")
                .getReference("Contribution");

        ContributionDetails contributionDetails = new ContributionDetails(email, 0, 0);

        referenceContribution.child(userId).setValue(contributionDetails)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Initial contribution data saved successfully.");
                        } else {
                            Log.e(TAG, "Failed to save initial contribution data.");
                        }
                    }
                });
    }

    private class UserDetails {
        public String email;
        public String passWord;
        public String fullName;
        public String role;
        public String contactNumber;

        public UserDetails(String email, String passWord, String fullName, String contactNumber) {
            this.email = email;
            this.passWord = passWord;
            this.fullName = fullName;
            this.contactNumber = contactNumber;
            this.role = "User";
        }
    }

    private class ContributionDetails {
        public String email;
        public int carbonSaving;
        public int energySaving;

        public ContributionDetails(String email, int carbonSaving, int energySaving) {
            this.email = email;
            this.carbonSaving = carbonSaving;
            this.energySaving = energySaving;
        }
    }

    public void backToSignIn(View view) {
        Intent intent = new Intent(SignUpUser.this, Login.class);
        startActivity(intent);
        finish();
    }
}
