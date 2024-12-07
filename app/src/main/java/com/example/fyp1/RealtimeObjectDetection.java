package com.example.fyp1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class RealtimeObjectDetection extends AppCompatActivity {
    private static final String TAG = "RealtimeObjectDetection";
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private static final String API_KEY = "reyezr8XkYIZ34AUHO6p"; // Replace with your Roboflow API key
    private static final String MODEL_ENDPOINT = "recycleitem/4"; // Replace with your model endpoint

    private PreviewView previewView;
    private ImageView overlayView;
    private ScheduledExecutorService executor;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realtime_object_detection);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);

        executor = Executors.newSingleThreadScheduledExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            startCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Log.e(TAG, "Camera permission denied");
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new android.util.Size(640, 480)) // Ensuring resolution matches what is sent to Roboflow
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyzeImage);

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void analyzeImage(@NonNull ImageProxy image) {
        if (!isProcessing) {
            isProcessing = true;
            runOnUiThread(() -> {
                Bitmap bitmap = previewView.getBitmap();
                if (bitmap != null) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);

                    executor.execute(() -> {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                        byte[] imageBytes = outputStream.toByteArray();
                        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                        sendToRoboflow(base64Image);
                        scaledBitmap.recycle();
                    });
                } else {
                    isProcessing = false;
                }
            });
        }
        image.close();
    }

    private void sendToRoboflow(String base64Image) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        String url = "https://detect.roboflow.com/" + MODEL_ENDPOINT + "?api_key=" + API_KEY;

        RequestBody requestBody = RequestBody.create(base64Image, MediaType.parse("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error sending request to Roboflow: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MotionToast.Companion.darkToast(
                                RealtimeObjectDetection.this,  // Pass the current Activity context
                                "Error",
                                "Error sending request (No internet connection) " ,
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(RealtimeObjectDetection.this, www.sanju.motiontoast.R.font.helveticabold)
                        );
                    }
                });


                isProcessing = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray predictions = jsonObject.getJSONArray("predictions");
                        drawDetections(predictions);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
                    }
                }
                isProcessing = false;
            }
        });
    }

    private void drawDetections(JSONArray predictions) {
        int viewWidth = previewView.getWidth();
        int viewHeight = previewView.getHeight();

        int inputImageWidth = 640;
        int inputImageHeight = 480;

        float scaleX = (float) viewWidth / inputImageWidth;
        float scaleY = (float) viewHeight / inputImageHeight;

        Bitmap overlay = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);

        Paint boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5);

        Paint textPaint = new Paint();
        textPaint.setTextSize(40);
        textPaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < predictions.length(); i++) {
            try {
                JSONObject prediction = predictions.getJSONObject(i);

                double x = prediction.getDouble("x");
                double y = prediction.getDouble("y");
                double width = prediction.getDouble("width");
                double height = prediction.getDouble("height");
                String className = prediction.getString("class");
                double confidence = prediction.getDouble("confidence") * 100;

                int scaledX = (int) (x * scaleX);
                int scaledY = (int) (y * scaleY);
                int scaledWidth = (int) (width * scaleX);
                int scaledHeight = (int) (height * scaleY);

                int left = scaledX - scaledWidth / 2;
                int top = scaledY - scaledHeight / 2;
                int right = scaledX + scaledWidth / 2;
                int bottom = scaledY + scaledHeight / 2;

                // Set color based on class
                int boxColor = getColorForClass(className);
                boxPaint.setColor(boxColor);
                textPaint.setColor(boxColor);

                canvas.drawRect(new Rect(left, top, right, bottom), boxPaint);

                // Draw class name and confidence percentage
                String labelText = className + " " + String.format("%.0f%%", confidence);
                canvas.drawText(labelText, left, top - 10, textPaint);

            } catch (JSONException e) {
                Log.e(TAG, "Error processing prediction: " + e.getMessage());
            }
        }

        runOnUiThread(() -> overlayView.setImageBitmap(overlay));
    }

    private int getColorForClass(String className) {
        switch (className) {
            case "Plastic":
                return Color.YELLOW;
            case "Paper":
                return Color.GREEN;
            case "Cans":
                return Color.CYAN;
            default:
                return Color.RED; // Default color for unknown classes
        }
    }
}
