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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Activity for the user Log-in screen
 * If opened with the intent containing extra "QR_Content" it will check if the extra is a valid login code and allow logging in as scanned user
 */
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

        //If a QR Code has been scanned there will be a QR_Content Intent passed into the function
        //Check if QR is a valid QR and either log user in or inform them of invalid code
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

    /**
     * Function to switch to the register page on button press
     * @param view
     */
    public void SwitchToRegister(View view){
        Intent openRegisterScreen = new Intent(LoginScreenActivity.this, Register.class);
        startActivity(openRegisterScreen);
        finish();
    }

    /**
     * Function for button to open camera for QR Scanning
     * @param view
     */
    public void LoginQRScanButton(View view){
        //open camera to scan QR Code
        Intent openQRScan = new Intent(LoginScreenActivity.this, loginQRScanActivity.class);
        startActivity(openQRScan);
        finish();
    }

    /**
     * Function for button that log's user in when a QR has been scanned
     * @param view
     */
    public void LoginButton(View view){
        setLoggedInUser(loginName);
    }


    /**
     * Log in the user to the account of whose QR Code was scanned and set all relevant information/files
     * @param login String with the username of new login
     */
    public void setLoggedInUser(String login){
        //create user info file
        File userInfoFile = new File(getFilesDir(), "userInfo");
        FileOutputStream userInfoStream = null;
        try {
            userInfoStream = new FileOutputStream(userInfoFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //put username into user info file
        try {
            userInfoStream.write(login.getBytes(StandardCharsets.UTF_8));
            userInfoStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //set file to readonly
        userInfoFile.setReadOnly();
        //check if user account is an admin account
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(login).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //If the user is an administrator account, create the adminUser file and add the required password
                if(documentSnapshot.contains("Admin")){
                    if(documentSnapshot.getBoolean("Admin") == true){
                        File adminInfoFile = new File(getFilesDir(), "adminUser");
                        FileOutputStream adminInfoStream = null;
                        try{
                            adminInfoStream = new FileOutputStream(adminInfoFile);
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        }

                        try{
                            ScoringHandler hashHandle = new ScoringHandler();
                            String adminPassHash = hashHandle.sha256("admin_password");
                            adminInfoStream.write(adminPassHash.getBytes(StandardCharsets.UTF_8));
                            adminInfoStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        Intent goToMainActivityIntent = new Intent(LoginScreenActivity.this, MainActivity.class);
        startActivity(goToMainActivityIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Function that confirms whether the QR Scanned was a valid Login QR Code
     * Recognized by comparing the included Hash and the Hash of the included username
     * @param qrCode Contents of scanned QR Code
     * @return Returns username string if QR Code is valid, otherwise returns empty String
     */
    public String confirmValidLoginQR(String qrCode){
        ScoringHandler scoreHandle = new ScoringHandler();
        Log.i(TAG, "Scanned String: " + qrCode);
        if(!qrCode.contains("_")){
            return "";
        }
        //Split the string into hash and username components
        String[] splitString = qrCode.split("_", 2);
        //Hash the username component of string
        String scannedUsernameHash = scoreHandle.sha256(splitString[1]);
        //If the included hash and new username hash match then it is valid
        if(scannedUsernameHash.equals(splitString[0])){
            return splitString[1];
        }else{
            return "";
        }
    }
}