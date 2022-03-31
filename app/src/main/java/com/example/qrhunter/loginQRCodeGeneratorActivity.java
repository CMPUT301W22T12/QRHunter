package com.example.qrhunter;

import androidx.appcompat.app.AppCompatActivity;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.WriterException;

import java.io.IOException;

/**
 * Activity that shows the users login QR Code
 * code referenced from: https://www.geeksforgeeks.org/how-to-generate-qr-code-in-android/
 */
public class loginQRCodeGeneratorActivity extends AppCompatActivity {
    QRGEncoder qrgEncoder;
    Bitmap bitmap;
    ImageView LoginQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_qrcode);
        //Get username
        userHandler usrHandle = new userHandler();
        String username = null;
        LoginQR = findViewById(R.id.loginQRImageView);
        try {
            username = usrHandle.getUsername(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get hash of username
        ScoringHandler hashHandler = new ScoringHandler();
        String usernameHash = hashHandler.sha256(username);

        //String combination of hashed user and username seperated by _
        String encodeString = usernameHash + "_" + username;

        //Create a display for the QR Code
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        //Encode string as a bitmap of the QR Code, show Image
        qrgEncoder = new QRGEncoder(encodeString, null, QRGContents.Type.TEXT, dimen);
        try{
            bitmap = qrgEncoder.encodeAsBitmap();
            LoginQR.setImageBitmap(bitmap);
        }catch( WriterException e){
            Log.e("Tag", e.toString());
        }
    }

    public void returnToMainButtonFunction(View v){
        Intent returnToMainIntent = new Intent(this, MainActivity.class);
        startActivity(returnToMainIntent);
        finish();
    }
}