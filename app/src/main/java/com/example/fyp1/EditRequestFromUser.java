package com.example.fyp1;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class EditRequestFromUser extends AppCompatActivity {

    private TextView tvAddress, tvItemDescription, tvDate, tvContactNumber, tvStatus, tvQuantity, Remark;
    private EditText statusRemark;
    private Button updateButton;
    private Spinner spinnerStatus;
    private Calendar calendar;

    private ImageView imageView;

    private String requestId;
    private String contactNumber;
    private String originalPickUpDate;
    private String firebaseStatus; // Store the status from Firebase
    private String originalRemark; // Store the original remark from Firebase
    private static final String TAG = "ViewDetailRequest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_request_from_user);
        initializeViews();
        calendar = Calendar.getInstance(); // Initialize the calendar

        Intent intent = getIntent();
        requestId = intent.getStringExtra("requestId");
        contactNumber = intent.getStringExtra("contactNumber");

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

        if (requestId != null && contactNumber != null) {
            fetchRequestData(requestId, contactNumber);
        } else {
            MotionToast.Companion.darkToast(EditRequestFromUser.this,
                    "Missing Request ID and Contact Number",
                    "Request id and contact number are null",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(EditRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold));
        }
        setupSpinner();
        tvDate.setOnClickListener(v -> showDatePickerDialog());
        updateButton.setOnClickListener(v -> updateRequest());
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(EditRequestFromUser.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);

                    // Prevent selecting dates before today
                    Calendar today = Calendar.getInstance();
                    if (calendar.before(today)) {
                        MotionToast.Companion.darkToast(EditRequestFromUser.this,
                                "Invalid Date Selection",
                                "Date cannot be before today",
                                MotionToastStyle.WARNING,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(EditRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold));
                        return;
                    }

                    String newPickUpDate = String.format("%s, %dth %s", getDayOfWeek(calendar), selectedDay, getMonthName(selectedMonth));
                    tvDate.setText(newPickUpDate);
                    tvDate.setTextColor(getResources().getColor(android.R.color.black));

                    // Compare and show Toast if the date has changed
                    if (originalPickUpDate != null && !originalPickUpDate.equals(newPickUpDate)) {
                        MotionToast.Companion.darkToast(EditRequestFromUser.this,
                                "Pickup Date Changed",
                                "The pick up date has been changed, please make confirmation with the client",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(EditRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Set minimum date to today
        datePickerDialog.show();
    }

    private boolean isDateSelected() {
        return !tvDate.getText().toString().equals("Select a date");
    }

    private String getMonthName(int month) {
        return new java.text.DateFormatSymbols().getMonths()[month];
    }

    private String getDayOfWeek(Calendar calendar) {
        return new java.text.DateFormatSymbols().getWeekdays()[calendar.get(Calendar.DAY_OF_WEEK)];
    }

    public void initializeViews() {
        tvAddress = findViewById(R.id.tvAddress);
        tvItemDescription = findViewById(R.id.tvItemDescription);
        tvDate = findViewById(R.id.tvDate);
        tvContactNumber = findViewById(R.id.tvContactNumber);
        tvStatus = findViewById(R.id.tvStatus);
        imageView = findViewById(R.id.imageView);
        tvQuantity = findViewById(R.id.tvItemQuantity);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        Remark = findViewById(R.id.Remark);
        statusRemark = findViewById(R.id.statusRemark);
        updateButton = findViewById(R.id.updateButton);
    }

    private ArrayAdapter<String> setupSpinner() {
        // Define spinner options directly in code
        String[] statuses = {"Pending", "Approved", "Rejected", "Completed"};

        // Create an ArrayAdapter using the statuses array
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Set up an item selected listener
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = (String) parent.getItemAtPosition(position);

                if (firebaseStatus != null) {
                    // Check if the selected status is the same as the original status from Firebase
                    if (selectedStatus.equals(firebaseStatus)) {
                        // Restore the original remark
                        if (originalRemark != null) {
                            statusRemark.setText(originalRemark);
                        }
                    } else {
                        // Clear statusRemark if selected status differs from Firebase status and is not "Pending"
                        if (!selectedStatus.equals("Pending")) {
                            statusRemark.setText("");
                        }
                    }
                }

                // Update visibility of statusRemark and Remark based on selected status
                if (selectedStatus.equals("Rejected") || selectedStatus.equals("Completed") || selectedStatus.equals("Approved")) {
                    statusRemark.setVisibility(View.VISIBLE);
                    Remark.setVisibility(View.VISIBLE);
                } else {
                    statusRemark.setVisibility(View.GONE);
                    Remark.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                statusRemark.setVisibility(View.GONE);
            }
        });

        return adapter;
    }

    private void fetchRequestData(String requestId, String contactNumber) {
        Log.d(TAG, "Fetching request from request Id: " + requestId);
        DatabaseReference databaserefer = FirebaseDatabase.getInstance().getReference("RecycleRequest").child(requestId);
        databaserefer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Snapshot: " + snapshot.toString());
                if (snapshot.exists()) {
                    String itemType = snapshot.child("ItemType").getValue(String.class);
                    Integer quantity = snapshot.child("Quantity").getValue(Integer.class);
                    String requestAddress = snapshot.child("RequestAddress").getValue(String.class);
                    String pickUpDate = snapshot.child("PickUpDate").getValue(String.class);
                    firebaseStatus = snapshot.child("requestStatus").getValue(String.class); // Fetch the status from Firebase
                    String imageUri = snapshot.child("imageUri").getValue(String.class);
                    originalRemark = snapshot.child("Remark").getValue(String.class); // Fetch the remark

                    // Store the original pick-up date
                    originalPickUpDate = pickUpDate;

                    // Initialize spinner and set selection based on firebaseStatus
                    ArrayAdapter<String> adapter = setupSpinner();
                    int statusPosition = adapter.getPosition(firebaseStatus);
                    spinnerStatus.setSelection(statusPosition);

                    // Update the UI with the fetched data
                    updateUI(pickUpDate, quantity, requestAddress, imageUri, firebaseStatus, itemType, contactNumber, originalRemark);
                } else {
                    Log.d(TAG, "Request not found in database");
                    runOnUiThread(() -> MotionToast.Companion.darkToast(EditRequestFromUser.this,
                            "Error",
                            "Request not found",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(EditRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold)));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Database error: " + error.getMessage() + " Details: " + error.getDetails());
                runOnUiThread(() -> MotionToast.Companion.darkToast(EditRequestFromUser.this,
                        "Error",
                        error.getMessage(),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(EditRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold)));
            }
        });
    }

    public void updateUI(String pickUpDate, Integer quantity, String requestAddress, String imageUri, String requestStatus, String itemType, String contactNumber, String remark) {
        runOnUiThread(() -> {
            if (tvAddress != null) {
                tvAddress.setText(requestAddress);
            }
            if (tvItemDescription != null) {
                tvItemDescription.setText(itemType);
            }

            if (tvQuantity != null) {
                if (quantity != null) {
                    tvQuantity.setText(String.valueOf(quantity) + " kg");
                } else {
                    tvQuantity.setText("Unknown quantity");
                }
            }
            if (tvDate != null) {
                tvDate.setText(pickUpDate);
            }
            if (tvContactNumber != null) {
                tvContactNumber.setText(contactNumber);
            }
            if (tvStatus != null) {
                tvStatus.setText(requestStatus);
            }

            if (imageUri != null && !imageUri.isEmpty()) {
                loadImage(imageUri);
            } else {
                Log.w(TAG, "Image URI is null or empty");
                if (imageView != null) {
                    imageView.setImageResource(R.drawable.defaultimage);
                }
            }

            // Display remark if available
            if (statusRemark != null && remark != null) {
                statusRemark.setText(remark);
            }
        });
    }

    private void loadImage(String imageUri) {
        Picasso.get().load(imageUri).into(imageView);
    }

    private void updateRequest() {
        String newPickUpDate = tvDate.getText().toString();
        String newStatus = spinnerStatus.getSelectedItem().toString();
        String newRemark = statusRemark.getText().toString();

        //if (newStatus.equals("Pending")) {
          //  Toast.makeText(this, "Cannot update request with 'Pending' status", Toast.LENGTH_SHORT).show();
            //return;
        //}

        new AlertDialog.Builder(this)
                .setTitle("Confirm Update")
                .setMessage("Are you sure you want to update this request?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User confirmed, proceed with update
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("RecycleRequest").child(requestId);

                        databaseReference.child("PickUpDate").setValue(newPickUpDate);
                        databaseReference.child("requestStatus").setValue(newStatus);
                        if (!newRemark.isEmpty()) {
                            databaseReference.child("Remark").setValue(newRemark);
                        }

                        MotionToast.Companion.darkToast(EditRequestFromUser.this,
                                "Request Updated",
                                "Request updated successfully",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(EditRequestFromUser.this, www.sanju.motiontoast.R.font.helveticabold));

                        Intent intent = new Intent(EditRequestFromUser.this, ViewRecycleRequestFromUser.class);
                        startActivity(intent);
                        finish(); // Close the activity
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User canceled the update, do nothing
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
