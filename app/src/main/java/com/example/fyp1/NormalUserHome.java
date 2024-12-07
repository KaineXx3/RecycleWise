package com.example.fyp1;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp1.Adapter.newsInfoHorizontalAdapter;
import com.example.fyp1.Model.NewsModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class NormalUserHome extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView chatBotIcon;
    private newsInfoHorizontalAdapter adapter;
    private ArrayList<NewsModel> newsList;
    private DatabaseReference databaseReference;
    private DatabaseReference recycleRequestRef;
    private DatabaseReference contributionRef;
    private FloatingActionButton imageButtonPlus;


    private FirebaseAuth authProfile;
    private DatabaseReference userRef;

    // TextView references
    private TextView energySavingTextView;
    private TextView carbonSavingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_normaluser);

        chatBotIcon = findViewById(R.id.imageButtonChatBot);
        imageButtonPlus = findViewById(R.id.imageAddNews);
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        // Initialize TextViews
        energySavingTextView = findViewById(R.id.energySaving);
        carbonSavingTextView = findViewById(R.id.carbonSaving);

        if (firebaseUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(firebaseUser.getUid());
            recycleRequestRef = FirebaseDatabase.getInstance().getReference("RecycleRequest");
            contributionRef = FirebaseDatabase.getInstance().getReference("Contribution");
            checkUserRole();
            checkAndUpdateContribution(firebaseUser.getEmail());
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        newsList = new ArrayList<>();
        adapter = new newsInfoHorizontalAdapter(newsList, this);
        recyclerView.setAdapter(adapter);
        TextView moreNewsText = findViewById(R.id.moreNewsText);

        moreNewsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toVerticalRecycleView(v);
            }
        });

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("news");
        fetchNewsData();

        chatBotIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NormalUserHome.this, AiChatBot.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.imageButtonPickup) {
                checkUserRoleForPickup();
                return true;
            } else if (itemId == R.id.imageButtonCamera) {
                Intent intent = new Intent(NormalUserHome.this, GuestPage.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.imageButtonRecycleLocation) {
                Intent intent = new Intent(NormalUserHome.this, ViewCollectorList.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.imageButtonProfile) {
                checkUserRoleForProfile();
                return true;
            }
            return false;
        });
    }

    private void checkUserRole() {
        userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.getValue(String.class);
                    if ("Admin".equals(role)) {
                        imageButtonPlus.setVisibility(View.VISIBLE);
                        imageButtonPlus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(NormalUserHome.this, NewsUpload.class);
                                startActivity(intent);
                            }
                        });
                    } else {
                        imageButtonPlus.setVisibility(View.INVISIBLE);
                    }
                } else {
                    MotionToast.Companion.darkToast(NormalUserHome.this,
                            "Invalid role",
                            "User role not found",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(NormalUserHome.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(NormalUserHome.this,
                        "Error",
                        databaseError.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(NormalUserHome.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reset the BottomNavigationView selection when returning to this page
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Set selected item to the default or unselect any item if desired
        bottomNav.getMenu().findItem(R.id.home).setChecked(true);      // Set default selection
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    public void toGuide(View view) {

        Intent intent = null;
        if (view.getId() == R.id.imageGlass) {
            intent = new Intent(NormalUserHome.this, Glass.class);
        } else if (view.getId() == R.id.imagePlastic) {
            intent = new Intent(NormalUserHome.this, Plastic.class);
        } else if (view.getId() == R.id.imageMedicine) {
            intent = new Intent(NormalUserHome.this, Medicine.class);
        } else if (view.getId() == R.id.imagePaper) {
            intent = new Intent(NormalUserHome.this, Paper.class);
        } else if (view.getId() == R.id.imageBulkyItem) {
            intent = new Intent(NormalUserHome.this, Bulkyitem.class);
        } else if (view.getId() == R.id.imageCans) {
            intent = new Intent(NormalUserHome.this, Cans.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private void fetchNewsData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    NewsModel newsItem = snapshot.getValue(NewsModel.class);
                    if (newsItem != null) {
                        newsList.add(newsItem);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public void toVerticalRecycleView(View v) {
        Intent intent = new Intent(NormalUserHome.this, NewsActivity.class);
        startActivity(intent);
    }



    private void checkUserRoleForProfile() {
        userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.getValue(String.class);
                    Intent intent;
                    if ("Recycle Vendor".equals(role)) {
                        intent = new Intent(NormalUserHome.this, ProfileRecycleVendor.class);
                    } else if ("Admin".equals(role)) {
                        intent = new Intent(NormalUserHome.this, ProfileAdmin.class);
                    } else {
                        intent = new Intent(NormalUserHome.this, Profile.class);
                    }
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(NormalUserHome.this,
                        "Error",
                        databaseError.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(NormalUserHome.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }



    private void checkUserRoleForPickup() {
        if (!isNetworkAvailable()) {
            MotionToast.Companion.darkToast(this,
                    "No Internet Connection",
                    "Please check your internet connection.",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }
        userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.getValue(String.class);
                    Intent intent;
                    if ("Recycle Vendor".equals(role)) {
                        intent = new Intent(NormalUserHome.this, ViewRecycleRequestFromUser.class);
                    } else if ("User".equals(role)) {
                        intent = new Intent(NormalUserHome.this, MakeRecycleRequestList.class);
                    } else if("Admin".equals(role)){
                        intent = new Intent(NormalUserHome.this,MakeRecycleRequestList.class);
                    }
                    else {
                        Toast.makeText(NormalUserHome.this, "Access Denied: Only Recycle Vendors can access this feature", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(intent);
                } else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(NormalUserHome.this,
                        "Error",
                        databaseError.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(NormalUserHome.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }



    private void checkAndUpdateContribution(String email) {
        if (TextUtils.isEmpty(email)) {
            MotionToast.Companion.darkToast(NormalUserHome.this,
                    "Error",
                    "User email not found",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(NormalUserHome.this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }

        recycleRequestRef.orderByChild("userEmail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SavingsData savingsData = new SavingsData();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String itemType = snapshot.child("ItemType").getValue(String.class);
                    Double quantity = snapshot.child("Quantity").getValue(Double.class);
                    String requestStatus = snapshot.child("requestStatus").getValue(String.class);

                    if ("Approved".equals(requestStatus) && quantity != null) {
                        double carbonSaving = 0;
                        double energySaving = 0;

                        switch (itemType) {
                            case "Plastic":
                                carbonSaving = quantity * 1.3;  // Example values
                                energySaving = quantity * 0.5;
                                break;
                            case "Paper":
                                carbonSaving = quantity * 0.9;
                                energySaving = quantity * 0.4;
                                break;
                            case "Glass":
                                carbonSaving = quantity * 1.1;
                                energySaving = quantity * 0.6;
                                break;
                            case "Metal":
                                carbonSaving = quantity * 1.5;
                                energySaving = quantity * 0.7;
                                break;
                            case "Electronics":
                                carbonSaving = quantity * 2.0;
                                energySaving = quantity * 0.8;
                                break;
                        }

                        savingsData.totalCarbonSaving += carbonSaving;
                        savingsData.totalEnergySaving += energySaving;
                    }
                }

                // Save the total savings into the Contribution node
                contributionRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean emailExists = dataSnapshot.exists();
                        if (emailExists) {
                            // Update existing contribution
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().child("carbonSaving").setValue(savingsData.totalCarbonSaving);
                                snapshot.getRef().child("energySaving").setValue(savingsData.totalEnergySaving);
                            }
                        } else {
                            // Add new contribution
                            DatabaseReference newContributionRef = contributionRef.push();
                            newContributionRef.child("email").setValue(email);
                            newContributionRef.child("carbonSaving").setValue(savingsData.totalCarbonSaving);
                            newContributionRef.child("energySaving").setValue(savingsData.totalEnergySaving);
                        }

                        // Update TextViews
                        updateTextViews(savingsData.totalCarbonSaving, savingsData.totalEnergySaving);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        MotionToast.Companion.darkToast(NormalUserHome.this,
                                "Error",
                                databaseError.getMessage(),
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(NormalUserHome.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(NormalUserHome.this,
                        "Error",
                        databaseError.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(NormalUserHome.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    private void updateTextViews(double carbonSaving, double energySaving) {
        String carbonSavingText = String.format("%.2f kgCO2e", carbonSaving);
        String energySavingText = String.format("%.2f kWh", energySaving);

        carbonSavingTextView.setText(carbonSavingText);
        energySavingTextView.setText(energySavingText);
    }

    // Inner class to hold the savings data
    private static class SavingsData {
        double totalCarbonSaving = 0;
        double totalEnergySaving = 0;
    }
}
