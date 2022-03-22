package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoginScreenActivity extends AppCompatActivity {
    Button scanButton;
    Button loginButton;
    ImageView statusImage;
    TextView statusText;
    String qrCode;
    String loginName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        scanButton = findViewById(R.id.scanLoginQRButton);
        loginButton = findViewById(R.id.loginQRConfirmButton);
        loginButton.setVisibility(View.INVISIBLE);

        statusImage = findViewById(R.id.loginStatusImageView);
        statusImage.setImageResource(android.R.drawable.presence_offline);

        statusText = findViewById(R.id.loginQRStatusTextView);
        statusText.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        if(intent.hasExtra("QR_Content")){
            qrCode = intent.getStringExtra("QR_Content");
            String validLogin = confirmValidLoginQR(qrCode);
            if(!validLogin.equals("")){
                statusText.setVisibility(View.VISIBLE);
                statusImage.setImageResource(android.R.drawable.presence_online);
                statusText.setText("Successfully Scanned User:\n" + validLogin);
                loginName = validLogin;
                loginButton.setText("Login as: " + validLogin);
                loginButton.setVisibility(View.VISIBLE);
            }else{
                statusText.setVisibility(View.VISIBLE);
                statusText.setText("Invalid Login QR Code\nPlease Try Again");
                getIntent().removeExtra("QR_content");
                qrCode = "";
                return;
            }
        }
    }

    public void SwitchToRegister(View view){
        Intent openRegisterScreen = new Intent(LoginScreenActivity.this, Register.class);
        startActivity(openRegisterScreen);
        finish();
    }

    public void LoginQRScanButton(View view){
        //open camera to scan QR Code
        Intent openQRScan = new Intent(LoginScreenActivity.this, loginQRScanActivity.class);
        startActivity(openQRScan);
        finish();
    }

    public void LoginButton(View view){
        setScannedUser(loginName);
    }


    public void setScannedUser(String login){
        File userInfoFile = new File(getFilesDir(), "userInfo");
        FileOutputStream userInfoStream = null;
        try {
            userInfoStream = new FileOutputStream(userInfoFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            userInfoStream.write(login.getBytes(StandardCharsets.UTF_8));
            userInfoStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        userInfoFile.setReadOnly();
        Intent goToMainActivityIntent = new Intent(LoginScreenActivity.this, MainActivity.class);
        startActivity(goToMainActivityIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public String confirmValidLoginQR(String qrCode){
        ScoringHandler scoreHandle = new ScoringHandler();
        Log.i(TAG, "Scanned String: " + qrCode);
        if(!qrCode.contains("_")){
            return "";
        }
        String[] splitString = qrCode.split("_", 2);
        Log.i(TAG, "Split[0] = " + splitString[0]);
        Log.i(TAG, "Split[1] = " + splitString[1]);
        String scannedUsernameHash = scoreHandle.sha256(splitString[1]);
        Log.i(TAG, "ScannedHash = " + scannedUsernameHash);
        if(scannedUsernameHash.equals(splitString[0])){
            return splitString[1];
        }else{
            return "";
        }
    }
}