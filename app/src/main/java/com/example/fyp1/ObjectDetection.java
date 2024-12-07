package com.example.fyp1;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ObjectDetection extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 100;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 101;
    private ProgressBar progressBar; // Add a reference to the ProgressBar
    private TextView scanningText;
    private ImageView imageView, infoImage;
    private TextView resultTextView;
    private Button captureButton;
    private String currentPhotoPath;
    private List<BoundingBox> boundingBoxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detection);

        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        captureButton = findViewById(R.id.captureButton);
        progressBar = findViewById(R.id.progressBar);
        scanningText=findViewById(R.id.scanningTextView);
        infoImage=findViewById(R.id.infomationImage);


        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });

        infoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInformationDetail();
            }
        });


        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();
                    BoundingBox touchedBox = getTouchedBox(x, y);
                    if (touchedBox != null) {
                        showInfoBox(touchedBox);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If camera permission is granted, start the camera intent
                dispatchTakePictureIntent();
            } else {
                MotionToast.Companion.darkToast(ObjectDetection.this,
                        "Camera Access Required",
                        "Camera permission is needed to use this feature",
                        MotionToastStyle.WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ObjectDetection.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                MotionToast.Companion.darkToast(ObjectDetection.this,
                        "Image Creation Error",
                        "Error creating image file",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ObjectDetection.this, www.sanju.motiontoast.R.font.helveticabold));
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.fyp1.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new File(currentPhotoPath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;

                Bitmap resizedBitmap = resizeImageToScreen(bitmap, screenWidth, screenHeight);
                imageView.setImageBitmap(resizedBitmap);
                progressBar.setVisibility(View.VISIBLE);
                scanningText.setVisibility(View.VISIBLE);
                performObjectDetection(resizedBitmap);
            }
        }
    }

    private Bitmap resizeImageToScreen(Bitmap originalImage, int screenWidth, int screenHeight) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        float ratio = Math.min((float) screenWidth / width, (float) screenHeight / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(originalImage, newWidth, newHeight, true);
    }

    private void performObjectDetection(Bitmap resizedBitmap) {
        String API_KEY = "reyezr8XkYIZ34AUHO6p"; // Your API Key
        String MODEL_ENDPOINT = "recycleitem/4"; // Model endpoint

        String base64Image = encodeImage(resizedBitmap);
        String uploadURL = "https://detect.roboflow.com/" + MODEL_ENDPOINT + "?api_key=" + API_KEY;

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(base64Image, MediaType.parse("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url(uploadURL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE); // Hide the ProgressBar
                scanningText.setVisibility(View.GONE);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE); // Hide the ProgressBar
                    scanningText.setVisibility(View.GONE);
                    MotionToast.Companion.darkToast(ObjectDetection.this,
                            "Error",
                            "No internet connection",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(ObjectDetection.this, www.sanju.motiontoast.R.font.helveticabold));
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        drawBoundingBox(responseData, resizedBitmap);
                        progressBar.setVisibility(View.GONE); // Hide the ProgressBar
                        scanningText.setVisibility(View.GONE);
                    });

                } else {
                    progressBar.setVisibility(View.GONE); // Hide the ProgressBar
                    scanningText.setVisibility(View.GONE);

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE); // Hide the ProgressBar
                        scanningText.setVisibility(View.GONE);
                        resultTextView.setText("Error: " + response.code());
                    });
                }
            }
        });
    }

    private void drawBoundingBox(String responseData, Bitmap bitmap) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray predictions = jsonObject.getJSONArray("predictions");

            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);

            Paint boxPaint = new Paint();
            boxPaint.setStrokeWidth(5);
            boxPaint.setStyle(Paint.Style.STROKE);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(20);
            textPaint.setStyle(Paint.Style.FILL);

            Paint textBackgroundPaint = new Paint();
            textBackgroundPaint.setStyle(Paint.Style.FILL);

            boundingBoxes.clear();
            int objectCount = predictions.length();

            for (int i = 0; i < objectCount; i++) {
                JSONObject prediction = predictions.getJSONObject(i);
                float x = (float) prediction.getDouble("x");
                float y = (float) prediction.getDouble("y");
                float width = (float) prediction.getDouble("width");
                float height = (float) prediction.getDouble("height");
                String detectedClass = prediction.getString("class");
                float confidence = (float) prediction.getDouble("confidence") * 100;

                float left = x - width / 2;
                float top = y - height / 2;
                float right = x + width / 2;
                float bottom = y + height / 2;

                int boxColor = getColorForClass(detectedClass);
                boxPaint.setColor(boxColor);
                textBackgroundPaint.setColor(boxColor);

                canvas.drawRect(left, top, right, bottom, boxPaint);

                String labelText = detectedClass + " " + String.format("%.0f%%", confidence);
                float textWidth = textPaint.measureText(labelText);
                float textHeight = textPaint.getTextSize();
                float textPadding = 10;
                canvas.drawRect(left, top - textHeight - textPadding, left + textWidth + textPadding, top, textBackgroundPaint);
                canvas.drawText(labelText, left + textPadding / 2, top - textPadding / 2, textPaint);

                BoundingBox box = new BoundingBox(left, top, right, bottom, detectedClass, confidence);
                boundingBoxes.add(box);
            }

            imageView.setImageBitmap(mutableBitmap);
            resultTextView.setText("Objects detected: " + objectCount);

        } catch (Exception e) {
            e.printStackTrace();
            resultTextView.setText("Error parsing response");
        }
    }

    private int getColorForClass(String detectedClass) {
        switch (detectedClass) {
            case "Plastic":
                return Color.YELLOW;
            case "Paper":
                return Color.GREEN;
            case "Cans":
                return Color.CYAN;
            default:
                return Color.WHITE;
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    private BoundingBox getTouchedBox(float x, float y) {
        for (BoundingBox box : boundingBoxes) {
            if (x >= box.left && x <= box.right && y >= box.top && y <= box.bottom) {
                return box;
            }
        }
        return null;
    }

    private void showInformationDetail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_processing_information, null);
        builder.setView(dialogView);

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();
    }


    private void showInfoBox(BoundingBox box) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_info, null);
        builder.setView(dialogView);

        // Customize the dialog content
        TextView titleTextView = dialogView.findViewById(R.id.titleTextView);
        TextView statusTextView = dialogView.findViewById(R.id.statusTextView);
        ImageView arrowView = dialogView.findViewById(R.id.arrowView);

        // Set titleTextView to the detected class
        String detectedClass = box.getDetectedClass();
        titleTextView.setText(detectedClass);

        // Determine if the detected object is recyclable
        String recyclableStatus = isRecyclable(detectedClass) ? "Recyclable" : "Not Recyclable";
        statusTextView.setText(recyclableStatus);

        // Optionally set the status color
        statusTextView.setTextColor(isRecyclable(detectedClass)
                ? getResources().getColor(android.R.color.holo_green_dark)
                : getResources().getColor(android.R.color.holo_red_dark));

        // Handle arrow click
        arrowView.setOnClickListener(v -> {
            if(detectedClass.equals("Plastic")){
                Intent intent=new Intent(ObjectDetection.this, Plastic.class);
                startActivity(intent);
            }
            else if(detectedClass.equals("Paper")){
                Intent intent=new Intent(ObjectDetection.this, Paper.class);
                startActivity(intent);
            }
            else if(detectedClass.equals("Cans")){
                Intent intent=new Intent(ObjectDetection.this, Cans.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(this, "Arrow clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Set the background of the dialog's window
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);

        // Set custom width for the dialog
        dialog.setOnShowListener(dialogInterface -> {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = 480; // Set width in pixels
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        });

        dialog.show();
    }






    private boolean isRecyclable(String itemClass) {
        return itemClass.equalsIgnoreCase("Plastic") ||
                itemClass.equalsIgnoreCase("Paper") ||
                itemClass.equalsIgnoreCase("Cans");
    }

    public void toFeedback(View view){
        Intent intent =new Intent(ObjectDetection.this,Feedback.class);
        startActivity(intent);
    }

    static class BoundingBox {
        float left, top, right, bottom;
        String detectedClass;
        float confidence;

        BoundingBox(float left, float top, float right, float bottom, String detectedClass, float confidence) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.detectedClass = detectedClass;
            this.confidence = confidence;
        }

        public String getDetectedClass() {
            return detectedClass;
        }

        public void setDetectedClass(String detectedClass) {
            this.detectedClass = detectedClass;
        }
    }
}