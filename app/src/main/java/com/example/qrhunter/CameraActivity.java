package com.example.qrhunter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

/**
 * Activity for running and managing the camera
 * Code for checking and requesting Camera Permissions referenced from: https://stackoverflow.com/questions/22400984/how-to-comment-androids-activities-with-javadoc
 *
 */
public class CameraActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 0;

    private PreviewView cameraPreviewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private Button qrCodeFoundButton;
    private String qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreviewView = findViewById(R.id.cameraPreviewView);
        qrCodeFoundButton = findViewById(R.id.cameraActivity_qrCodeFoundButton);

        qrCodeFoundButton.setVisibility(View.INVISIBLE);
        qrCodeFoundButton.setOnClickListener(new View.OnClickListener() {
            // onClick function for the QR Code Found Button
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(), qrCode, Toast.LENGTH_SHORT).show();
                Log.i(CameraActivity.class.getSimpleName(), "QR Code Found: " + qrCode);
            }
        });

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();
    }

    /**
     * Requests permission to use the camera in the application
     */
    private void requestCamera() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            startCamera();
        }else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }

    }

    @SuppressLint("MissingSuperCall") //Do not need the super method call unless dealing with fragments
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == PERMISSION_REQUEST_CAMERA){
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ //if permissions were granted start camera
                startCamera();
            }else{
                Toast.makeText(this, "Camera Permission Denied, please allow camera use in settings", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * start the camera and bind the preview correctly
     * catch any exceptions
     */
    private void startCamera(){
        cameraProviderFuture.addListener(() -> {
            try{
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            }catch(ExecutionException | InterruptedException e) { //catch any errors when starting the camera
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * binds the created camera to the camera preview view in order to allow user to see camera view
     * set the analyzer for the camera to detect QR Codes
     * @param cameraProvider object representing the Camera
     */
    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider){
        cameraPreviewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(1280,720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeListener() {
            @Override
            public void qrCodeFound(String _qrCode) {
                qrCode = _qrCode;
                qrCodeFoundButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void qrCodeNotFound() {
                qrCodeFoundButton.setVisibility(View.INVISIBLE);
            }
        }));

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }
}

