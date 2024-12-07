package com.example.fyp1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fyp1.databinding.ActivityUploadNewsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class EditNews extends AppCompatActivity {
    private ActivityUploadNewsBinding binding;
    private Uri imageUri;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private String newsId; // Field to store newsId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("news");

        // Adjust window insets for status bar or notch
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (view, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            view.setPadding(view.getPaddingLeft(), topInset, view.getPaddingRight(), view.getPaddingBottom());
            return insets;
        });

        // Retrieve newsId from Intent
        newsId = getIntent().getStringExtra("newsId");
        if (newsId != null) {
            loadNewsData(newsId); // Fetch news data from Firebase using newsId
        } else {
            showToast("News ID not found", Toast.LENGTH_SHORT);
        }

        // Set click listeners for selecting image and uploading news
        binding.eventImageView.setOnClickListener(v -> selectImage());
        binding.uploadButton.setOnClickListener(v -> upload());
    }

    private void loadNewsData(String newsId) {
        DatabaseReference newsRef = databaseReference.child(newsId);
        newsRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                String title = dataSnapshot.child("title").getValue(String.class);
                String information = dataSnapshot.child("information").getValue(String.class);

                // Populate fields with existing data
                binding.titleEditText.setText(title);
                binding.infoEditText.setText(information);
                loadImage(imageUrl);
            } else {
                showToast("News item not found", Toast.LENGTH_SHORT);
            }
        }).addOnFailureListener(e -> showToast("Failed to load news data", Toast.LENGTH_SHORT));
    }

    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl)
                    .placeholder(R.drawable.defaultimage)
                    .error(R.drawable.defaultimage)
                    .into(binding.eventImageView);
        } else {
            binding.eventImageView.setImageResource(R.drawable.defaultimage);
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    private void upload() {
        String title = binding.titleEditText.getText().toString().trim();
        String information = binding.infoEditText.getText().toString().trim();

        // Ensure to check if title and information fields are empty
        if (title.isEmpty() || information.isEmpty()) {
            MotionToast.Companion.darkToast(EditNews.this,
                    "All Fields Must Be Filled",
                    "All fields are required",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(EditNews.this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }

        // If imageUri is null, allow to update text fields without changing the image
        if (imageUri == null) {
            updateNewsDataWithoutImage(newsId, title, information);
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating...");
        progressDialog.show();

        String fileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.ENGLISH).format(new Date()) + ".jpg";
        storageReference = FirebaseStorage.getInstance().getReference("images/" + fileName);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date());
                    uploadNewsData(newsId, title, information, imageUrl, currentDate); // Use existing newsId
                    clearFields();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }))
                .addOnFailureListener(e -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    MotionToast.Companion.darkToast(EditNews.this,
                            "Error",
                            "Upload Failed",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(EditNews.this, www.sanju.motiontoast.R.font.helveticabold));
                });
    }

    // New method to update without changing the image
    private void updateNewsDataWithoutImage(String newsId, String title, String information) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date());
        // Fetch the existing news item first to keep the current image URL
        databaseReference.child(newsId).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String existingImageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                uploadNewsData(newsId, title, information, existingImageUrl, currentDate);
            } else {
                showToast("Failed to find news item", Toast.LENGTH_SHORT);
            }
        }).addOnFailureListener(e -> showToast("Error fetching news item", Toast.LENGTH_SHORT));
    }

    private void uploadNewsData(String newsId, String title, String information, String imageUrl, String date) {
        // Update the existing news item using the existing newsId
        NewsItem newsItem = new NewsItem(newsId, title, information, imageUrl, date);
        databaseReference.child(newsId).setValue(newsItem)
                .addOnSuccessListener(aVoid -> {
                    MotionToast.Companion.darkToast(EditNews.this,
                            "News Updated",
                            "News updated successfully",
                            MotionToastStyle.SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(EditNews.this, www.sanju.motiontoast.R.font.helveticabold));

                    // Navigate back to the home page
                    Intent intent = new Intent(EditNews.this, NormalUserHome.class); // Replace with your home page activity
                    startActivity(intent);
                    finish(); // Finish the current activity
                })
                .addOnFailureListener(e -> MotionToast.Companion.darkToast(EditNews.this,
                        "News Update Failed",
                        "Failed to update news",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(EditNews.this, www.sanju.motiontoast.R.font.helveticabold)));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.eventImageView.setImageURI(imageUri);
        }
    }

    private void clearFields() {
        binding.titleEditText.setText("");
        binding.infoEditText.setText("");
        binding.eventImageView.setImageResource(R.drawable.defaultimage);
    }

    private void showToast(String message, int duration) {
        Toast.makeText(this, message, duration).show();
    }
}

// NewsItem class
class NewsItem {
    private String newsId;
    private String title;
    private String information;
    private String imageUrl;
    private String date;

    public NewsItem() {
        // Default constructor required for calls to DataSnapshot.getValue(NewsItem.class)
    }

    public NewsItem(String newsId, String title, String information, String imageUrl, String date) {
        this.newsId = newsId;
        this.title = title;
        this.information = information;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    // Getters and Setters
    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
