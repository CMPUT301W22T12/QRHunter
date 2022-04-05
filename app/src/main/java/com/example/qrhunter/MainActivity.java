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

/**
 * Main Menu Activity, contains buttons to navigate to various sections of the app
 */
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

    /**
     * Function for initializing the Map Activity
     */
    public void InitMap(){
        openMapActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                    }
                });
    }

    /**
     * Logs current user out of the application, returns to login screen
     * @param view standard button onClick argument
     */
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

    /**
     * Opens up the camera to scan a new QR Code
     * @param view standard button onClick argument
     */
    public void scanNewCodeButton(View view){
        Intent scanNewCodeIntent = new Intent(MainActivity.this, qrScanCameraActivity.class);
        startActivity(scanNewCodeIntent);
    }

    /**
     * Opens the map activity to view nearby QR Codes
     * @param view standard button onClick argument
     */
    public void openMapButton(View view){
        Intent openMapIntent = new Intent(MainActivity.this, MapsActivity.class);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        locationHandler locationHandler = new locationHandler(this);
        double[] coords = locationHandler.getCurrentLocation();
        openMapIntent.putExtra("1", coords);
        openMapActivityResultLauncher.launch(openMapIntent);
    }

    /**
     * Opens up an activity that displays the QR code used to login
     * @param view standard button onClick argument
     */
    public void openLoginQRButton(View view){
        Intent openLoginQRIntent = new Intent(MainActivity.this, loginQRCodeGeneratorActivity.class);
        startActivity(openLoginQRIntent);
    }

    /**
     * Opens up the user profile activity
     * @param view standard button onClick argument
     */
    public void openUserProfileButton(View view){
        Intent openProfileIntent = new Intent(MainActivity.this, userProfileViewerActivity.class);
        openProfileIntent.putExtra("userID", username);
        startActivity(openProfileIntent);
    }

    /**
     * Opens up the user search menu
     * @param view standard button onClick argument
     */
    public void openUserSearchButton(View view){
        Intent openUserSearchIntent = new Intent(MainActivity.this, userSearchActivity.class);
        startActivity(openUserSearchIntent);
    }

    /**
     * Opens up an activity that displays the QR code used to search for your profile
     * @param view standard button onClick argument
     */
    public void openUserSearchQRButton(View view){
        Intent openUserSearchQRIntent = new Intent(MainActivity.this, userSearchQRGeneratorActivity.class);
        startActivity(openUserSearchQRIntent);
    }

    /**
     * Opens up the leaderboard activity
     * @param view standard button onClick argument
     */
    public void openLeaderboardButton(View view){
        Intent openLeaderboardIntent = new Intent(MainActivity.this, leaderboardActivity.class);
        startActivity(openLeaderboardIntent);
    }

    /**
     * Exits the application
     * @param view standard button onClick argument
     */
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