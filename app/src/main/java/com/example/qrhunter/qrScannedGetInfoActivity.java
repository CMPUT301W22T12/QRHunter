package com.example.qrhunter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * Activity for getting all required information on a newly scanned QR Code
 * Information gathered: Pictures, Location, Comment, Score, Hash
 */
public class qrScannedGetInfoActivity extends AppCompatActivity {
    String qrContent;
    String objectImagePath;
    Bitmap objectImage;
    Boolean includeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanned_get_info);
        //retrieve variables for view entities
        TextView pointsText = findViewById(R.id.qrScanned_pointValueTextView);
        ImageView objectImageView = findViewById(R.id.getInfo_objectImageView);

        //retrieve content from Intent
        Bundle extras = getIntent().getExtras();
        qrContent = extras.getString("QR_Content");
        boolean comeFromScan = extras.getBoolean("entryFromScanBool");

        ScoringHandler scoringHandler = new ScoringHandler();
        String shaString = scoringHandler.sha256(qrContent);
        int score = scoringHandler.hexStringReader(shaString);

        if(comeFromScan){
            objectImageView.setVisibility(View.INVISIBLE);
            //When coming from a fresh scan ensure that the cache image file is empty
            File imageCacheFile = new File(getCacheDir().getAbsolutePath(), "objectImageFile.jpg");
            if(imageCacheFile.exists()){
                imageCacheFile.delete();
            }
        }
        if(extras.containsKey("imagePath")){
            includeImage = true;
            objectImagePath = extras.getString("imagePath");
            Log.i(qrScannedGetInfoActivity.class.getSimpleName(), "path received: " + objectImagePath);
            Bitmap bitmapImage = BitmapFactory.decodeFile(objectImagePath);
            objectImage = bitmapImage;
            objectImageView.setImageBitmap(bitmapImage);
            objectImageView.setVisibility(View.VISIBLE);
        }

        pointsText.setText("Your QR Code has scored you " + score + " points!");
    }

    /**
     * Button onclick function used to add a picture of the object
     * Opens camera intent
     * @param v current view
     */
    public void addImageButton(View v){
        Intent addObjectImageIntent = new Intent(qrScannedGetInfoActivity.this, qrAddObjectPictureActivity.class);
        addObjectImageIntent.putExtra("QR_Content", qrContent);
        startActivity(addObjectImageIntent);
    }

    /**
     * Button onclick function for the submission of scanned code
     * Put's all required information into the database
     * @param v current view
     */
    public void submitQRCodeButton(View v){
        CheckBox addLocationCheckbox = findViewById(R.id.addLocationOnCodeCheckbox);
        String commentText;
        Intent submitQRCodeIntent = new Intent();
        //add location to QR Code entry database
        if(addLocationCheckbox.isChecked()){

        }
        TextView commentTextBox = findViewById(R.id.commentOnNewCodeTextEdit);
        //add new comment to database linked to this QR Code
        if(commentTextBox.toString().equals("")){
            commentText = commentTextBox.toString();
            submitQRCodeIntent.putExtra("commentString", commentText);
        }
        //Add Image of object to QR Code entry in Database
        if(includeImage){

        }
    }



}