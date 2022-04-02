package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    String username;
    double[] currentL;
    ActivityResultLauncher<Intent> openMapActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitMap();
        File userInfoFile = new File(getFilesDir(), "userInfo");
        if(userInfoFile.exists()){
            userHandler userhandle = new userHandler();
            try {
                username = userhandle.getUsername(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "username: " + username);

            return;
        }else{
            Intent registerIntent = new Intent(this, Register.class);
            startActivity(registerIntent);
            finish();
        }
    }

    public void InitMap(){
        openMapActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            currentL = data.getDoubleArrayExtra("1");
                        }
                    }
                });
    }

    public void Logout(View view) {
        File userInfo = new File(getFilesDir(), "userInfo");
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        userInfo.delete();
                        startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                        finish();                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void scanNewCodeButton(View view){
        Intent scanNewCodeIntent = new Intent(MainActivity.this, qrScanCameraActivity.class);
        startActivity(scanNewCodeIntent);
    }

    public void openMapButton(View view){
        Intent openMapIntent = new Intent(MainActivity.this, MapsActivity.class);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        if (currentL != null){
            openMapIntent.putExtra("1", currentL);
        } else {
            locationHandler locationHandler = new locationHandler(this);
            double[] coords = locationHandler.getCurrentLocation();
            openMapIntent.putExtra("1", coords);
        }

        openMapActivityResultLauncher.launch(openMapIntent);
    }

    public void openLoginQRButton(View view){
        Intent openLoginQRIntent = new Intent(MainActivity.this, loginQRCodeGeneratorActivity.class);
        startActivity(openLoginQRIntent);
    }

    public void openUserProfileButton(View view){
        Intent openProfileIntent = new Intent(MainActivity.this, userProfileViewerActivity.class);
        openProfileIntent.putExtra("userID", username);
        startActivity(openProfileIntent);
    }

    public void testQRViewButton(View view){
        String qrCodeId = "a78b4976f797ad00bca8bee0acf8b0ff7f78e1565b423bad794aa733ee265f6c";
        Intent testQRViewerIntent = new Intent(MainActivity.this, QRcodeViewerActivity.class);
        testQRViewerIntent.putExtra("QRcode", qrCodeId);
        startActivity(testQRViewerIntent);
    }

    public void exitButton(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }


}