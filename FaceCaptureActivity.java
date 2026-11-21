package com.example.spucs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// The camera screen: finds one face, waits for a blink (liveness) and returns a 112x112 face picture.
public class FaceCaptureActivity extends ComponentActivity {
    public static final String EXTRA_FACE = "face";

    private PreviewView previewView;
    private TextView statusText;
    private Button captureButton;
    private LifecycleCameraController camera;
    private FaceDetector faceDetector;
    private ExecutorService cameraThread;
    private boolean eyesWereClosed;
    private boolean blinkSeen;
    private Bitmap face;

    private final ActivityResultLauncher<String> askCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    statusText.setText(R.string.capture_no_permission);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_capture);
        previewView = findViewById(R.id.previewView);
        statusText = findViewById(R.id.captureStatusText);
        captureButton = findViewById(R.id.captureButton);
        captureButton.setEnabled(false);
        captureButton.setOnClickListener(v -> returnFace());
        findViewById(R.id.cancelCaptureButton).setOnClickListener(v -> finish());

        // Face detection that also says how open each eye is.
        faceDetector = FaceDetection.getClient(new FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build());
        cameraThread = Executors.newSingleThreadExecutor();

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            askCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        camera = new LifecycleCameraController(this);
        camera.setCameraSelector(CameraSelector.DEFAULT_FRONT_CAMERA);
        camera.setEnabledUseCases(CameraController.IMAGE_ANALYSIS);
        camera.setImageAnalysisAnalyzer(cameraThread, this::analyze);
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        previewView.setController(camera);
        // The cameras are known once CameraX has started.
        camera.getInitializationFuture().addListener(() -> {
            // A phone or emulator without a front camera uses the back camera.
            if (!camera.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                camera.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);
            }
            camera.bindToLifecycle(this);
            statusText.setText(R.string.capture_looking);
        }, getMainExecutor());
    }

    // Runs for each camera picture.
    private void analyze(ImageProxy image) {
        Bitmap picture = rotate(image.toBitmap(), image.getImageInfo().getRotationDegrees());
        try {
            faceDetector.process(InputImage.fromBitmap(picture, 0))
                    .addOnSuccessListener(faces -> checkFaces(picture, faces))
                    .addOnCompleteListener(task -> image.close());
        } catch (RuntimeException e) {
            image.close();
        }
    }

    // Runs on the screen's thread with the faces found in one picture.
    private void checkFaces(Bitmap picture, List<Face> faces) {
        if (isFinishing()) {
            return;
        }
        if (faces.size() != 1) {
            noFace(R.string.capture_one_face);
            return;
        }
        Face found = faces.get(0);
        Float leftEye = found.getLeftEyeOpenProbability();
        Float rightEye = found.getRightEyeOpenProbability();
        if (leftEye == null || rightEye == null) {
            noFace(R.string.capture_move_closer);
            return;
        }
        // Liveness: the eyes must be closed once and then open again.
        if (leftEye < 0.3f && rightEye < 0.3f) {
            eyesWereClosed = true;
        }
        if (eyesWereClosed && leftEye > 0.7f && rightEye > 0.7f) {
            blinkSeen = true;
            face = cutOutFace(picture, found.getBoundingBox());
        }
        if (blinkSeen && face != null) {
            statusText.setText(R.string.capture_ready);
            captureButton.setEnabled(true);
        } else {
            statusText.setText(R.string.capture_blink);
        }
    }

    private void noFace(int message) {
        face = null;
        captureButton.setEnabled(false);
        statusText.setText(message);
    }

    // Cuts the face out of the picture and makes it 112x112, the size the AI model needs.
    private Bitmap cutOutFace(Bitmap picture, Rect box) {
        int left = Math.max(0, box.left);
        int top = Math.max(0, box.top);
        int width = Math.min(box.right, picture.getWidth()) - left;
        int height = Math.min(box.bottom, picture.getHeight()) - top;
        if (width <= 0 || height <= 0) {
            return null;
        }
        Bitmap cut = Bitmap.createBitmap(picture, left, top, width, height);
        return Bitmap.createScaledBitmap(cut, 112, 112, true);
    }

    // Turns the camera picture upright.
    private Bitmap rotate(Bitmap picture, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), matrix, true);
    }

    private void returnFace() {
        Intent result = new Intent();
        result.putExtra(EXTRA_FACE, face);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (camera != null) {
            camera.clearImageAnalysisAnalyzer();
        }
        cameraThread.shutdown();
        faceDetector.close();
        super.onDestroy();
    }
}
