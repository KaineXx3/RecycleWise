package com.example.fyp1;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.example.fyp1.Model.ManageUserModel;
import com.example.fyp1.Adapter.UserAdapter;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ManageUser extends AppCompatActivity implements UserAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<ManageUserModel> userList;
    private DatabaseReference databaseReference;
    private String currentUserId;
    private String currentUserRole;

    private boolean isNameAsc = true;
    private boolean isRoleAsc = true;
    private boolean isLastActiveAsc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, this);
        recyclerView.setAdapter(userAdapter);

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


        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchCurrentUserRole();
        loadUserData();

        setupHeaderClicks();
    }

    private void fetchCurrentUserRole() {
        DatabaseReference currentUserRef = databaseReference.child(currentUserId);
        currentUserRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserRole = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(ManageUser.this,
                        "User Role Retrieval Failed",
                        "Failed to fetch user role.",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ManageUser.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    private void loadUserData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ManageUserModel user = snapshot.getValue(ManageUserModel.class);
                    if (user != null) {
                        user.setUserId(snapshot.getKey()); // Set userId from the database key
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                MotionToast.Companion.darkToast(ManageUser.this,
                        "Data Load Failed",
                        "Failed to load data.",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ManageUser.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    private void setupHeaderClicks() {
        TextView nameHeader = findViewById(R.id.nameHeader);
        TextView roleHeader = findViewById(R.id.roleHeader);
        TextView lastActiveHeader = findViewById(R.id.lastActiveHeader);
        ImageView nameUpArrow = findViewById(R.id.nameUpArrow);
        ImageView nameDownArrow = findViewById(R.id.nameDownArrow);
        ImageView roleUpArrow = findViewById(R.id.roleUpArrow);
        ImageView roleDownArrow = findViewById(R.id.roleDownArrow);
        ImageView lastActiveUpArrow = findViewById(R.id.lastActiveUpArrow);
        ImageView lastActiveDownArrow = findViewById(R.id.lastActiveDownArrow);

        nameHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByName(isNameAsc);
                isNameAsc = !isNameAsc;
                updateArrowColors(nameUpArrow, nameDownArrow, isNameAsc);
                resetArrowColors(roleUpArrow, roleDownArrow, lastActiveUpArrow, lastActiveDownArrow);
            }
        });

        roleHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByRole(isRoleAsc);
                isRoleAsc = !isRoleAsc;
                updateArrowColors(roleUpArrow, roleDownArrow, isRoleAsc);
                resetArrowColors(nameUpArrow, nameDownArrow, lastActiveUpArrow, lastActiveDownArrow);
            }
        });

        lastActiveHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByLastActive(isLastActiveAsc);
                isLastActiveAsc = !isLastActiveAsc;
                updateArrowColors(lastActiveUpArrow, lastActiveDownArrow, isLastActiveAsc);
                resetArrowColors(nameUpArrow, nameDownArrow, roleUpArrow, roleDownArrow);
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
        Collections.sort(userList, new Comparator<ManageUserModel>() {
            @Override
            public int compare(ManageUserModel u1, ManageUserModel u2) {
                if (ascending) {
                    return u1.getFullName().compareToIgnoreCase(u2.getFullName());
                } else {
                    return u2.getFullName().compareToIgnoreCase(u1.getFullName());
                }
            }
        });
        userAdapter.notifyDataSetChanged();
    }

    private void sortByRole(boolean ascending) {
        Collections.sort(userList, new Comparator<ManageUserModel>() {
            @Override
            public int compare(ManageUserModel u1, ManageUserModel u2) {
                if (ascending) {
                    return u1.getRole().compareToIgnoreCase(u2.getRole());
                } else {
                    return u2.getRole().compareToIgnoreCase(u1.getRole());
                }
            }
        });
        userAdapter.notifyDataSetChanged();
    }

    private void sortByLastActive(boolean ascending) {
        Collections.sort(userList, new Comparator<ManageUserModel>() {
            @Override
            public int compare(ManageUserModel u1, ManageUserModel u2) {
                if (u1.getLastSignInTime() == null) return 1;
                if (u2.getLastSignInTime() == null) return -1;
                if (ascending) {
                    return u1.getLastSignInTime().compareTo(u2.getLastSignInTime());
                } else {
                    return u2.getLastSignInTime().compareTo(u1.getLastSignInTime());
                }
            }
        });
        userAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteClick(int position) {
        ManageUserModel user = userList.get(position);

        // Check if the role of the specific user is "Admin"
        if ("Admin".equals(user.getRole())) {
            MotionToast.Companion.darkToast(ManageUser.this,
                    "Deletion Not Allowed",
                    "Admin users cannot be deleted.",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(ManageUser.this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }

        // Prompt for confirmation before deletion
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete user data from Firebase
                    deleteUserData(user);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteUserData(ManageUserModel user) {
        // Delete the user from Firebase Realtime Database
        databaseReference.child(user.getUserId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                MotionToast.Companion.darkToast(ManageUser.this,
                        "User Deleted",
                        "User Successfully Deleted",
                        MotionToastStyle.SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ManageUser.this, www.sanju.motiontoast.R.font.helveticabold));
            } else {
                MotionToast.Companion.darkToast(ManageUser.this,
                        "User Deletion Failed",
                        "Failed to delete user",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ManageUser.this, www.sanju.motiontoast.R.font.helveticabold));

            }
        });
    }


}
