package com.example.fyp1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.List;
public class ArPaper extends AppCompatActivity {
    private static final String TAG = "ArPaper";
    private String message = "";
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 1;
    private static final String SEARCHING_PLANE_MESSAGE = "Move your device to detect surfaces";
    private final SnackbarHelper messageSnackbarHelper  = new SnackbarHelper();
    private ArFragment arFragment;
    private TextView statusTextView;
    private boolean planeDetected;

    private boolean isRecycleBinPlaced;
    private boolean isNewspaperPlaced;

    private TransformableNode newspaperNode;
    private Vector3 velocity = new Vector3();
    private static final float GRAVITY = 9.8f;
    private static final float THROW_FORCE = 5f;
    private boolean isNewspaperThrown;

    private List<Node> collidableNodes = new ArrayList<>();
    private int score;
    private TextView scoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_paper);

        initializeViews();
        checkCameraPermission();
    }

    private void initializeViews() {
        statusTextView = findViewById(R.id.status_text_view);
        scoreTextView = findViewById(R.id.score_text_view);

        if (statusTextView == null || scoreTextView == null) {
            Log.e(TAG, "TextView not found in layout");
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_LONG).show();
            finish();
        }

        updateScoreDisplay();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
        } else {
            message = SEARCHING_PLANE_MESSAGE;
            if (message != null) {
                messageSnackbarHelper.showMessage(this, SEARCHING_PLANE_MESSAGE);
            }
            initializeAR();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeAR();
            } else {
                Log.e(TAG, "Camera permission denied.");
                messageSnackbarHelper.showError(this, "Camera permission is required for AR functionality.");
            }
        }
    }

    private void initializeAR() {
        try {
            if (ArCoreApk.getInstance().requestInstall(this, true) == ArCoreApk.InstallStatus.INSTALLED) {
                ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
                if (availability.isSupported()) {
                    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
                    initializeARComponents();
                } else {
                    messageSnackbarHelper.showError(this, "AR is not supported on this device");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing AR: ", e);
            messageSnackbarHelper.showError(this, "Error initializing AR: " + e.getMessage());
        }
    }

    private void initializeARComponents() {
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);

        arFragment.setOnTapArPlaneListener(this::onPlaneTap);

    }

    private void onUpdate(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            updatePlaneDetection(frame);
            updateNewspaperPhysics(frameTime);
            checkCollisions();
        }
    }

    private void updatePlaneDetection(Frame frame) {
        boolean currentPlaneDetected = false;
        for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                currentPlaneDetected = true;
                break;
            }
        }

        if (currentPlaneDetected != planeDetected) {
            planeDetected = currentPlaneDetected;
            if (planeDetected) {
                if (!isRecycleBinPlaced) {
                    message="Tap the plane to place recycle bin";
                    statusTextView.setText(message);
                } else if (!isNewspaperPlaced&& isRecycleBinPlaced) {
                    message="Tap the plane to place NewsPaper";
                    statusTextView.setText(message);

                }
            } else {
                // Show Snackbar message when plane is not detected
                messageSnackbarHelper.showMessage(this, SEARCHING_PLANE_MESSAGE);
            }
        }
    }


    private void updateNewspaperPhysics(FrameTime frameTime) {
        if (isNewspaperThrown && newspaperNode != null) {
            float deltaTime = frameTime.getDeltaSeconds();

            // Update position
            Vector3 position = newspaperNode.getLocalPosition();
            position.x += velocity.x * deltaTime;
            position.y += velocity.y * deltaTime;
            position.z += velocity.z * deltaTime;
            newspaperNode.setLocalPosition(position);

            // Update velocity (apply gravity)
            velocity.y -= GRAVITY * deltaTime;

            // Check if the newspaper has hit the ground (assuming y=0 is ground level)
            if (position.y <= 0f) {
                position.y = 0f;
                newspaperNode.setLocalPosition(position);
                isNewspaperThrown = false;
                velocity = new Vector3();
            }
        }
    }

    private void checkCollisions() {
        if (isNewspaperThrown && newspaperNode != null) {
            for (Node node : collidableNodes) {
                if (isColliding(newspaperNode, node)) {
                    handleCollision();
                    break;
                }
            }
        }
    }

    private boolean isColliding(Node node1, Node node2) {
        Vector3 distance = Vector3.subtract(node1.getWorldPosition(), node2.getWorldPosition());
        float collisionThreshold = 0.2f; // Adjust this value based on your models' sizes
        return distance.length() < collisionThreshold;
    }

    private void handleCollision() {
        score++;
        updateScoreDisplay();


        newspaperNode.setRenderable(null);
        isNewspaperPlaced = false;
        isNewspaperThrown = false;
        statusTextView.setText("Great job! Newspaper recycled! Tap to place another newspaper");

    }

    private void updateScoreDisplay() {
        runOnUiThread(() -> scoreTextView.setText("Score: " + score));
    }

    private void onPlaneTap(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (!isRecycleBinPlaced) {
            placeRecycleBinModel("models/recyclepaper.glb", hitResult);
            isRecycleBinPlaced = true;
        } else if (!isNewspaperPlaced) {
            placeNewspaper(hitResult);
            isNewspaperPlaced = true;
        }

        if (isRecycleBinPlaced && !isNewspaperPlaced) {
            statusTextView.setText("Tap to place another newspaper");
        } else if (isRecycleBinPlaced && isNewspaperPlaced) {
            statusTextView.setText("Throw the newspaper to the recycle bin");
            messageSnackbarHelper.showMessage(this, "Throw the newspaper to the recycle bin");
        }
    }

    private void placeRecycleBinModel(String modelUri, HitResult hitResult) {
        Anchor anchor = hitResult.createAnchor();

        ModelRenderable.builder()
                .setSource(this, Uri.parse(modelUri))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(renderable -> {
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
                    modelNode.setParent(anchorNode);
                    modelNode.setRenderable(renderable);
                    modelNode.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f)); // Smaller scale for recyclepaper
                    modelNode.select();

                    collidableNodes.add(modelNode);

                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Unable to load recycle bin model", throwable);
                    messageSnackbarHelper.showError(this, "Failed to load recycle bin model: " + throwable.getMessage());
                    return null;
                });
    }

    private void placeNewspaper(HitResult hitResult) {
        Anchor anchor = hitResult.createAnchor();

        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/newspaper.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(renderable -> {
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    newspaperNode = new TransformableNode(arFragment.getTransformationSystem());
                    newspaperNode.setParent(anchorNode);
                    newspaperNode.setRenderable(renderable);
                    newspaperNode.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
                    newspaperNode.select();

                    newspaperNode.setOnTouchListener((hitTestResult, motionEvent) -> {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            throwNewspaper();
                        }
                        return true;
                    });

                    messageSnackbarHelper.showMessage(this, "Throw the newspaper to the blue recycle bin (For recycling paper)");
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Unable to load newspaper model", throwable);
                    messageSnackbarHelper.showError(this, "Failed to load newspaper model: " + throwable.getMessage());
                    return null;
                });
    }

    private void throwNewspaper() {
        if (!isNewspaperThrown) {
            isNewspaperThrown = true;
            velocity = new Vector3(0f, THROW_FORCE, 0f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arFragment != null && arFragment.getArSceneView() != null) {
            try {
                arFragment.getArSceneView().resume();
            } catch (CameraNotAvailableException e) {
                Log.e(TAG, "Camera not available", e);
                messageSnackbarHelper.showError(this, "Camera not available. Please restart the app.");
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during AR scene resume", e);
                messageSnackbarHelper.showError(this, "An unexpected error occurred. Please restart the app.");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (arFragment != null) {
            arFragment.getArSceneView().pause();
        }
    }
}