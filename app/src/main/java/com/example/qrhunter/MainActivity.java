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
                    }
                });
    }

    public void Logout(View view) {
        File userInfo = new File(getFilesDir(), "userInfo");
        File adminInfo = new File(getFilesDir(), "adminUser");
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        userInfo.delete();
                        if(adminInfo.exists()){
                            adminInfo.delete();
                        }
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
        locationHandler locationHandler = new locationHandler(this);
        double[] coords = locationHandler.getCurrentLocation();
        openMapIntent.putExtra("1", coords);


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

    public void openUserSearchButton(View view){
        Intent openUserSearchIntent = new Intent(MainActivity.this, userSearchActivity.class);
        startActivity(openUserSearchIntent);
    }

    public void openUserSearchQRButton(View view){
        Intent openUserSearchQRIntent = new Intent(MainActivity.this, userSearchQRGeneratorActivity.class);
        startActivity(openUserSearchQRIntent);
    }

    public void openLeaderboardButton(View view){
        Intent openLeaderboardIntent = new Intent(MainActivity.this, leaderboardActivity.class);
        startActivity(openLeaderboardIntent);
    }

    public void testButton(View view){
        String qrCodeId = "94385f347527098352e446bdc646e112935aa8ada22c8f93844bc7be1bdc56ff";
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