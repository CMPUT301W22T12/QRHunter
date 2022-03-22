package com.example.qrhunter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.concurrent.ExecutionException;

/**
 * Activity for running and managing the camera used when taking a picture of an object that was scanned
 */
public class qrAddObjectPictureActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private PreviewView cameraPreviewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private ImageButton takePicButton;
    private String qrContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_add_object_picture);

        Bundle extras = getIntent().getExtras();
        qrContent = extras.getString("QR_Content");

        takePicButton = findViewById(R.id.addObject_takeImageButton);
        cameraPreviewView = findViewById(R.id.cameraAddObjectPreviewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();
    }

    /**
     * Requests permission to use the camera in the application
     */
    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(qrAddObjectPictureActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
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
     * start the camera for adding a picture of the object and bind the preview correctly
     * catch any exceptions
     */
    private void startCamera(){
        cameraProviderFuture.addListener(() -> {
            try{
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraTakePicture(cameraProvider);
            }catch(ExecutionException | InterruptedException e) { //catch any errors when starting the camera
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * binds the created camera to the camera preview view in order to allow user to see camera view
     * set the image capture and file options
     * take picture and do what is needed with the image
     * @param cameraProvider object representing the Camera
     */
    private void bindCameraTakePicture(@NonNull ProcessCameraProvider cameraProvider){
        cameraProvider.unbindAll();
        cameraPreviewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        Preview preview = new Preview.Builder().build();

        //Selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();


        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture);

        takePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturePhoto();
            }
        });

    }

    /**
     * Function for capturing the photo from camera
     * Saves photo into cache for temporary use
     * Rotates photo taken to ensure proper orientation
     */
    public void capturePhoto(){
        String imagePath = getCacheDir().getAbsolutePath() + "/objectImageFile.jpg";
        File imageFile = new File(imagePath);
        imageHandler imageHandler = new imageHandler();

            imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(ImageProxy imageProxy){
                @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();
                Bitmap bitmapImage = imageHandler.convertImageToBitmap(image);

                //check image rotation and rotate if needed
                bitmapImage = imageHandler.rotateBitmapIfNeeded(bitmapImage, imageProxy.getImageInfo().getRotationDegrees());

                //compress Bitmap and save as JPEG to file
                OutputStream fileOutStream = null;
                try {
                    fileOutStream = new FileOutputStream(imageFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 85, fileOutStream);
                try {
                    fileOutStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //close image proxy and switch activity
                imageProxy.close();
                Intent objectImageTakenIntent = new Intent(qrAddObjectPictureActivity.this, qrScannedGetInfoActivity.class);
                objectImageTakenIntent.putExtra("QR_Content", qrContent);
                objectImageTakenIntent.putExtra("entryFromScanBool", false);
                objectImageTakenIntent.putExtra("imagePath", imagePath);
                startActivity(objectImageTakenIntent);
            }
            @Override
            public void onError(ImageCaptureException e) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}