package com.example.fyp1;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp1.Adapter.RecycleVendorAdapter;
import com.example.fyp1.Model.RecycleVendor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ManageRegisterRequest extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecycleVendorAdapter adapter;
    private List<RecycleVendor> vendorList;
    private DatabaseReference databaseReference;
    private boolean isNameAsc = true;
    private boolean isRegistrationAsc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_register_request_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vendorList = new ArrayList<>();
        adapter = new RecycleVendorAdapter(this, vendorList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
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

        fetchVendors();
        setupHeaderClicks();
    }

    private void fetchVendors() {
        databaseReference.orderByChild("role").equalTo("Recycle Vendor")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        vendorList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            RecycleVendor vendor = snapshot.getValue(RecycleVendor.class);
                            if (vendor != null && "Pending".equals(vendor.getApproveStatus())) {
                                vendor.setUserId(snapshot.getKey()); // Set the Firebase key as userId
                                vendorList.add(vendor);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        MotionToast.Companion.darkToast(ManageRegisterRequest.this,
                                "Error",
                                databaseError.getMessage(),
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(ManageRegisterRequest.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                });
    }

    private void setupHeaderClicks() {
        TextView nameHeader = findViewById(R.id.nameHeader);
        TextView registrationHeader = findViewById(R.id.registraionNumberHeader);
        ImageView nameUpArrow = findViewById(R.id.nameUpArrow);
        ImageView nameDownArrow = findViewById(R.id.nameDownArrow);
        ImageView registrationUpArrow = findViewById(R.id.registraionNumberUpArrow);
        ImageView registrationDownArrow = findViewById(R.id.registraionNumberDownArrow);

        nameHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByName(isNameAsc);
                isNameAsc = !isNameAsc;
                updateArrowColors(nameUpArrow, nameDownArrow, isNameAsc);
                resetArrowColors(registrationUpArrow, registrationDownArrow);
            }
        });

        registrationHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByRegistration(isRegistrationAsc);
                isRegistrationAsc = !isRegistrationAsc;
                updateArrowColors(registrationUpArrow, registrationDownArrow, isRegistrationAsc);
                resetArrowColors(nameUpArrow, nameDownArrow);
            }
        });
    }

    private void updateArrowColors(ImageView upArrow, ImageView downArrow, boolean isAscending) {
        int downArrowColor = isAscending ? 0xFF000000 : 0xFFDAD8C9;
        int upArrowColor = isAscending ? 0xFFDAD8C9 : 0xFF000000;

        upArrow.setColorFilter(upArrowColor);
        downArrow.setColorFilter(downArrowColor);
    }

    private void resetArrowColors(ImageView... arrows) {
        for (ImageView arrow : arrows) {
            arrow.setColorFilter(0xFFDAD8C9);
        }
    }

    private void sortByName(boolean ascending) {
        Collections.sort(vendorList, new Comparator<RecycleVendor>() {
            @Override
            public int compare(RecycleVendor u1, RecycleVendor u2) {
                if (ascending) {
                    return u1.getFullName().compareToIgnoreCase(u2.getFullName());
                } else {
                    return u2.getFullName().compareToIgnoreCase(u1.getFullName());
                }
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void sortByRegistration(boolean ascending) {
        Collections.sort(vendorList, new Comparator<RecycleVendor>() {
            @Override
            public int compare(RecycleVendor u1, RecycleVendor u2) {
                if (ascending) {
                    return u1.getRegistrationNumber().compareToIgnoreCase(u2.getRegistrationNumber());
                } else {
                    return u2.getRegistrationNumber().compareToIgnoreCase(u1.getRegistrationNumber());
                }
            }
        });
        adapter.notifyDataSetChanged();
    }
}