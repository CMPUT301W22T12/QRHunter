package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Activity for getting all required information on a newly scanned QR Code
 * Information gathered: Pictures, Location, Comment, Score, Hash
 */
public class qrScannedGetInfoActivity extends AppCompatActivity {
    String qrContent;
    String objectImagePath;
    Bitmap objectImage;
    Boolean includeImage = false;
    String shaString;
    String username;
    int score;

    //Get required database references
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanned_get_info);
        //retrieve variables for view entities
        TextView pointsText = findViewById(R.id.qrScanned_pointValueTextView);
        ImageView objectImageView = findViewById(R.id.getInfo_objectImageView);

        userHandler userHand = new userHandler();
        try {
            username = userHand.getUsername(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //retrieve content from Intent
        Bundle extras = getIntent().getExtras();
        qrContent = extras.getString("QR_Content");
        boolean comeFromScan = extras.getBoolean("entryFromScanBool");

        //get hash and score
        ScoringHandler scoringHandler = new ScoringHandler();
        shaString = scoringHandler.sha256(qrContent);
        score = scoringHandler.hexStringReader(shaString);

        //check if user has scanned this code before
        DocumentReference userDocRef = db.collection("Users").document(username);
        Task<DocumentSnapshot> userDocTask = userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot userDoc = task.getResult();
                ArrayList userQRs = (ArrayList) userDoc.get("QRcodes");
                if(userQRs.contains(shaString)){
                    Intent backToMainIntent = new Intent(qrScannedGetInfoActivity.this, MainActivity.class);
                    startActivity(backToMainIntent);
                    Toast.makeText(qrScannedGetInfoActivity.this, "You have already scanned this QR Code", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        //When coming from a fresh scan ensure that the cache image file is empty
        if(comeFromScan){
            objectImageView.setVisibility(View.INVISIBLE);
            File imageCacheFile = new File(getCacheDir().getAbsolutePath(), "objectImageFile.jpg");
            if(imageCacheFile.exists()){
                imageCacheFile.delete();
            }
        }
        //If an imagePath is included, retrieve and show the image
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
    public void submitQRCodeButton(View v) throws FileNotFoundException {
        String commentId = "";
        String imagePath = "";
        String photoRefLocation;
        String qrCodeId = shaString;

        qrDatabaseAddHandler qrDatabaseHandler = null;
        try {
            qrDatabaseHandler = new qrDatabaseAddHandler(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        CheckBox addLocationCheckbox = findViewById(R.id.addLocationOnCodeCheckbox);
        //add location to QR Code entry database
        double latitude = 0.0;
        double longitude = 0.0;
        if(addLocationCheckbox.isChecked()){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);
            locationHandler locationHandler = new locationHandler(this);
            double[] coords = locationHandler.getCurrentLocation();
            latitude = coords[0];
            longitude = coords[1];
        }

        //If the user took a picture to include
        if(includeImage){
            Log.i(TAG, "Include Image");
            photoRefLocation = qrDatabaseHandler.addImageToStorage(shaString, storage, objectImagePath);
            imagePath = "gs://qrhunter-38915.appspot.com/" + photoRefLocation;
        }

        //If the user made a comment
        String commentText;
        TextView commentTextBox = findViewById(R.id.commentOnNewCodeTextEdit);
        Boolean isComment = !commentTextBox.getText().toString().equals("");
        if(isComment) {
            Log.i(TAG, "IsComment");
            commentText = commentTextBox.getText().toString();
            commentId = qrDatabaseHandler.addCommentToDB(commentText, shaString, db);
        }

        qrDatabaseHandler.addQRToDB(commentId, imagePath, score, latitude, longitude, shaString, db);


        Intent backToMainIntent = new Intent(qrScannedGetInfoActivity.this, MainActivity.class);
        startActivity(backToMainIntent);
        Toast.makeText(this, "QR Code Submitted!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}