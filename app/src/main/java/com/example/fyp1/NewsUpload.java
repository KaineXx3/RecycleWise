package com.example.fyp1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class NewsUpload extends AppCompatActivity {
    ActivityUploadNewsBinding binding;
    Uri imageUri;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseReference = FirebaseDatabase.getInstance().getReference("news");

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

        binding.eventImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        binding.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
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

        if (imageUri == null || title.isEmpty() || information.isEmpty()) {
            MotionToast.Companion.darkToast(NewsUpload.this,
                    "All Fields Must Be Filled",
                    "All fields are required",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(NewsUpload.this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.ENGLISH);
        Date now = new Date();
        String fileName = formatter.format(now) + ".jpg";

        storageReference = FirebaseStorage.getInstance().getReference("images/" + fileName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date());
                                uploadNewsData(title, information, imageUrl, currentDate);
                                clearFields();
                            }
                        });

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        binding.eventImageView.setImageURI(null);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        MotionToast.Companion.darkToast(NewsUpload.this,
                                "Error",
                                "Upload Failed",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(NewsUpload.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                });
    }

    private void uploadNewsData(String title, String information, String imageUrl, String date) {
        String newsId = databaseReference.push().getKey();

        if (newsId != null) {
            News news = new News(newsId, title, information, imageUrl, date);
            databaseReference.child(newsId).setValue(news)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            MotionToast.Companion.darkToast(NewsUpload.this,
                                    "News Uploaded",
                                    "News uploaded successfully",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(NewsUpload.this, www.sanju.motiontoast.R.font.helveticabold));

                            Intent intent = new Intent(NewsUpload.this, NormalUserHome.class); // Replace with your home page activity
                            startActivity(intent);
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MotionToast.Companion.darkToast(NewsUpload.this,
                                    "News Upload Failed",
                                    "Failed to upload news",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(NewsUpload.this, www.sanju.motiontoast.R.font.helveticabold));
                        }
                    });
        }
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


}

class News {
    String newsId;
    String title;
    String information;
    String imageUrl;
    String date;

    public News() {
    }

    public News(String newsId, String title, String information, String imageUrl, String date) {
        this.newsId = newsId;
        this.title = title;
        this.information = information;
        this.imageUrl = imageUrl;
        this.date=date;
    }

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

