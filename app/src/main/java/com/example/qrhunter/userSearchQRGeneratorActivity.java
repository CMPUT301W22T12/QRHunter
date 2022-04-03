package com.example.qrhunter;

import androidx.appcompat.app.AppCompatActivity;

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

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class userSearchQRGeneratorActivity extends AppCompatActivity {
    QRGEncoder qrgEncoder;
    Bitmap bitmap;
    ImageView userSearchQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search_qrgenerator);

        //Get username
        userHandler usrHandle = new userHandler();
        String username = "";
        userSearchQR = findViewById(R.id.userSearchQRView);
        try {
            username = usrHandle.getUsername(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //String combination of hashed user and username seperated by _
        String encodeString =  "QRHunterGame_" + username;
        Log.i("Tag", encodeString);

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
            userSearchQR.setImageBitmap(bitmap);
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