package com.example.fyp1;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class Login extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button loginButton;
    private FirebaseAuth authProfile;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        authProfile = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isNetworkAvailable()) {
                    MotionToast.Companion.darkToast(Login.this,
                            "Error",
                            "No network connection",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(Login.this, www.sanju.motiontoast.R.font.helveticabold));
                    return; // Stop further execution if there's no network
                }
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    editTextEmail.setError("Email is required");
                    editTextEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Login.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                    editTextEmail.setError("Valid email is required");
                    editTextEmail.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Please enter your password", Toast.LENGTH_LONG).show();
                    editTextPassword.setError("Password is required");
                    editTextPassword.requestFocus();
                } else {
                    MotionToast.Companion.darkToast(Login.this,
                            "Authenticating...",
                            "Please wait while we verify your credentials.",
                            MotionToastStyle.INFO,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(Login.this, www.sanju.motiontoast.R.font.helveticabold));
                    loginUser(email, password);
                }
            }
        });
    }

    private void loginUser(final String email, final String password) {
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String role = userSnapshot.child("role").getValue(String.class);

                        if ("User".equals(role)) {
                            authenticateUser(email, password, role);
                        } else if ("Recycle Vendor".equals(role)) {
                            String approveStatus = userSnapshot.child("approveStatus").getValue(String.class);
                            if ("Approved".equals(approveStatus)) {
                                authenticateUser(email, password, role);
                            } else if ("Pending".equals(approveStatus)) {
                                MotionToast.Companion.darkToast(Login.this,
                                        "Pending Approval",
                                        "Your account is pending approval.",
                                        MotionToastStyle.INFO,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        ResourcesCompat.getFont(Login.this, www.sanju.motiontoast.R.font.helveticabold));
                            } else {
                                MotionToast.Companion.darkToast(Login.this,
                                        "Account Not Approved",
                                        "Your account is not approved\n",
                                        MotionToastStyle.WARNING,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        ResourcesCompat.getFont(Login.this, www.sanju.motiontoast.R.font.helveticabold));
                            }
                        } else if ("Admin".equals(role)) {
                            authenticateUser(email, password, role);
                        } else {
                            MotionToast.Companion.darkToast(Login.this,
                                    "Error ☹",
                                    "Invalid user role.",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(Login.this, www.sanju.motiontoast.R.font.helveticabold));
                        }
                        return; // Exit the loop after processing the first matching user
                    }
                } else {
                    MotionToast.Companion.darkToast(Login.this,
                            "Error ☹",
                            "User not found",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(Login.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(Login.this,
                        "Database error",
                        databaseError.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(Login.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void authenticateUser(final String email, final String password, final String role) {
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    if (firebaseUser != null) {
                        // Store the login time in Firebase Realtime Database
                        DatabaseReference userRef = databaseReference.child(firebaseUser.getUid());
                        long loginTimeMillis = System.currentTimeMillis();

                        // Format the current time to "MMM dd, yyyy"
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        String formattedDate = sdf.format(new Date(loginTimeMillis));

                        userRef.child("lastSignInTime").setValue(formattedDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Check or save user contribution
                                        checkOrSaveContribution(email);

                                        Intent intent = null;
                                        if ("Recycle Vendor".equals(role)) {
                                            intent = new Intent(Login.this, NormalUserHome.class);
                                        } else if ("User".equals(role)) {
                                            intent = new Intent(Login.this, NormalUserHome.class);
                                        } else if ("Admin".equals(role)) {
                                            intent = new Intent(Login.this, NormalUserHome.class);
                                        }

                                        if (intent != null) {
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error saving login time: " + e.getMessage());
                                        MotionToast.Companion.darkToast(Login.this,
                                                "Failed to save login time",
                                                e.getMessage(),
                                                MotionToastStyle.ERROR,
                                                MotionToast.GRAVITY_BOTTOM,
                                                MotionToast.LONG_DURATION,
                                                ResourcesCompat.getFont(Login.this, www.sanju.motiontoast.R.font.helveticabold));

                                    }
                                });
                    }
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        editTextEmail.setError("User does not exist. Please register again.");
                        editTextEmail.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        editTextPassword.setError("Invalid credentials. Kindly check and re-enter.");
                        editTextPassword.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        MotionToast.Companion.darkToast(Login.this,
                                "Error ☹",
                                e.getMessage(),
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(Login.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                }
            }
        });
    }

    private void checkOrSaveContribution(final String email) {
        DatabaseReference contributionRef = FirebaseDatabase.getInstance().getReference("Contribution");
        contributionRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Email not found in Contribution node, save new contribution data
                    Map<String, Object> contributionData = new HashMap<>();
                    contributionData.put("email", email);
                    contributionData.put("energySaving", 0);
                    contributionData.put("carbonSaving", 0);

                    String key = contributionRef.push().getKey();
                    if (key != null) {
                        contributionRef.child(key).setValue(contributionData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Contribution data saved successfully.");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Failed to save contribution data: " + e.getMessage());
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "Contribution data already exists for this email.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    public void backToRegister(View view) {
        Intent intent = new Intent(Login.this, SignUpUser.class);
        startActivity(intent);
    }

    public void backToResetPassword(View view) {
        Intent intent = new Intent(Login.this, ForgotPassword.class);
        startActivity(intent);
    }

    public void goToGuestPage(View view) {
        Intent intent = new Intent(Login.this, GuestPage.class);
        startActivity(intent);
    }
}
