package com.example.fyp1;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class MakeRecycleRequest extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSIONS_REQUEST = 100;

    private ImageView imgAddPhoto;
    private TextView tvPickupDate;
    private Calendar calendar;
    private Spinner spinnerRecyclables;
    private EditText etQuantityRemarks;
    private Button btnNext;
    private Button btnCancel;
    private String locationAddress;
    private Uri photoUri;

    private FirebaseAuth mAuth;
    private String userEmail;

    private boolean isPhotoUploaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_recycle_request);

        imgAddPhoto = findViewById(R.id.img_add_photo);
        tvPickupDate = findViewById(R.id.tv_pickup_date);
        spinnerRecyclables = findViewById(R.id.spinner_recyclables);
        etQuantityRemarks = findViewById(R.id.et_quantity_remarks);
        btnNext = findViewById(R.id.btn_next);
        btnCancel = findViewById(R.id.btn_cancel);
        calendar = Calendar.getInstance();

        mAuth = FirebaseAuth.getInstance();

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

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();

        // Retrieve the location address from the Intent extras
        if (intent != null && intent.hasExtra("location_address")) {
            locationAddress = intent.getStringExtra("location_address");
        }

        // Set up the spinner with recyclables items
        String[] recyclablesArray = {"Plastic", "Paper", "Glass", "Metal", "Electronics"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, recyclablesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecyclables.setAdapter(adapter);

        imgAddPhoto.setOnClickListener(v -> requestPermissionsAndOpenImageChooser());
        tvPickupDate.setOnClickListener(v -> showDatePickerDialog());
        btnNext.setOnClickListener(v -> validateAndProceed());
        btnCancel.setOnClickListener(v -> finish());

        fetchUserEmail();
    }

    private void fetchUserEmail() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(uid).child("email");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userEmail = snapshot.getValue(String.class);
                    } else {
                        showErrorToast("User email not found");
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showErrorToast("Failed to retrieve user email");
                    finish();
                }
            });
        } else {
            showErrorToast("User not authenticated");
            finish();
        }
    }

    private void requestPermissionsAndOpenImageChooser() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST);
        } else {
            // Permissions are already granted, open image chooser
            openImageChooser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
                openImageChooser();
            } else {
                // Permissions denied
                showWarningToast("Permission Required", "Permissions are required to use this feature");
            }
        }
    }

    private void openImageChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooser = Intent.createChooser(galleryIntent, "Select Picture");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        startActivityForResult(chooser, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            try {
                Bitmap bitmap;
                if (data != null && data.getData() != null) {
                    // Image from gallery
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                } else if (data != null && data.getExtras() != null) {
                    // Image from camera
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else {
                    showErrorToast("Failed to get image");
                    return;
                }

                Uri imageUri = saveImageToFile(bitmap);
                if (imageUri != null) {
                    imgAddPhoto.setImageBitmap(bitmap);
                    imgAddPhoto.setTag(imageUri.toString());
                    isPhotoUploaded = true;  // Set the flag to true when a photo is uploaded
                } else {
                    showErrorToast("Failed to save image");
                }
            } catch (IOException e) {
                e.printStackTrace();
                showErrorToast("Error processing image");
            }
        }
    }

    private Uri saveImageToFile(Bitmap bitmap) {
        File imagePath = new File(getCacheDir(), "images");
        imagePath.mkdirs(); // Create directories if they don't exist
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".png";
        File newFile = new File(imagePath, imageFileName);

        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return FileProvider.getUriForFile(this, "com.example.fyp1.fileprovider", newFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(MakeRecycleRequest.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);

                    // Prevent selecting dates before today
                    Calendar today = Calendar.getInstance();
                    if (calendar.before(today)) {
                        showWarningToast("Invalid Date", "Date cannot be before today");
                        return;
                    }

                    String dateString = String.format("%s, %dth %s", getDayOfWeek(calendar), selectedDay, getMonthName(selectedMonth));
                    tvPickupDate.setText(dateString);
                    tvPickupDate.setTextColor(getResources().getColor(android.R.color.black));
                }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Set minimum date to today
        datePickerDialog.show();
    }

    private boolean isDateSelected() {
        return !tvPickupDate.getText().toString().equals("Select a date");
    }

    private String getMonthName(int month) {
        return new java.text.DateFormatSymbols().getMonths()[month];
    }

    private String getDayOfWeek(Calendar calendar) {
        return new java.text.DateFormatSymbols().getWeekdays()[calendar.get(Calendar.DAY_OF_WEEK)];
    }

    private void validateAndProceed() {
        if (!isPhotoUploaded) {
            showWarningToast("Photo Required", "Please upload a photo before proceeding");
            return;
        }

        String quantityText = etQuantityRemarks.getText().toString();
        if (TextUtils.isEmpty(quantityText)) {
            showWarningToast("Quantity Required", "Please enter a quantity");
            return;
        }
        if (!isDateSelected()) {
            showWarningToast("Pick Up Required", "Please select a pickup date");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            showWarningToast("Invalid Quantity", "Please enter a valid number");
            return;
        }

        if (quantity < 6) {
            showWarningToast("Quantity Must Be 6 or Higher", "Please enter a value of 6 or above");
        } else {
            // Proceed with the next action
            String pickupDate = tvPickupDate.getText().toString();
            String recyclableItem = spinnerRecyclables.getSelectedItem().toString();
            long calendarTime = calendar.getTimeInMillis();

            Intent intent = new Intent(MakeRecycleRequest.this, SelectCollectorActivity.class);
            intent.putExtra("location_address", locationAddress);
            intent.putExtra("quantity", quantity);
            intent.putExtra("pickup_date", pickupDate);
            intent.putExtra("recyclable_item", recyclableItem);
            intent.putExtra("calendar_time", calendarTime);
            intent.putExtra("user_email", userEmail);

            if (imgAddPhoto.getDrawable() != null && imgAddPhoto.getTag() != null) {
                intent.putExtra("image_uri", imgAddPhoto.getTag().toString());
            } else {
                intent.putExtra("image_uri", "no_image");
            }

            startActivity(intent);
        }
    }

    private void showWarningToast(String title, String message) {
        MotionToast.Companion.darkToast(MakeRecycleRequest.this,
                title,
                message,
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(MakeRecycleRequest.this, www.sanju.motiontoast.R.font.helveticabold));
    }

    private void showErrorToast(String message) {
        MotionToast.Companion.darkToast(MakeRecycleRequest.this,
                "Error",
                message,
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(MakeRecycleRequest.this, www.sanju.motiontoast.R.font.helveticabold));
    }
}