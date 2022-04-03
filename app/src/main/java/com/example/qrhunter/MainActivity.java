package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    public void Logout(View view) {
        File userInfo = new File(getFilesDir(), "userInfo");
        userInfo.delete();
        startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
        finish();
    }

    public void scanNewCodeButton(View view){
        Intent scanNewCodeIntent = new Intent(MainActivity.this, qrScanCameraActivity.class);
        startActivity(scanNewCodeIntent);
    }

    public void openMapButton(View view){
        Intent openMapIntent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(openMapIntent);
    }

    public void openLoginQRButton(View view){
        Intent openLoginQRIntent = new Intent(MainActivity.this, loginQRCodeGeneratorActivity.class);
        startActivity(openLoginQRIntent);
    }

    public void testQRViewButton(View view){
        String qrCodeId = "a78b4976f797ad00bca8bee0acf8b0ff7f78e1565b423bad794aa733ee265f6c";
        Intent testQRViewerIntent = new Intent(MainActivity.this, QRcodeViewerActivity.class);
        testQRViewerIntent.putExtra("QRcode", qrCodeId);
        startActivity(testQRViewerIntent);
    }

    // Open Leaderboard
    public void openLeaderboard(View view){
        Intent openLeaderboard = new Intent(MainActivity.this, Leaderboard.class);
        startActivity(openLeaderboard);
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