package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for getting all required information on a newly scanned QR Code
 * Information gathered: Pictures, Location, Comment, Score, Hash
 */
public class qrScannedGetInfoActivity extends AppCompatActivity {
    String qrContent;
    String objectImagePath;
    Bitmap objectImage;
    Boolean includeImage;
    String shaString;
    int score;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

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

        //get hash and score
        ScoringHandler scoringHandler = new ScoringHandler();
        shaString = scoringHandler.sha256(qrContent);
        score = scoringHandler.hexStringReader(shaString);

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
    public void submitQRCodeButton(View v) throws FileNotFoundException {
        String commentId = "";
        String imagePath = "";
        String photoRefLocation;

        qrDatabaseAddHandler qrDatabaseHandler = new qrDatabaseAddHandler();
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

        //add location data to QR Code if needed...
        //Not yet implemented
//        CheckBox addLocationCheckbox = findViewById(R.id.addLocationOnCodeCheckbox);
//
//        //add location to QR Code entry database
//        if(addLocationCheckbox.isChecked()){
//
//        }

        qrDatabaseHandler.addQRToDB(commentId, imagePath, score, shaString, db);


        Intent backToMainIntent = new Intent(qrScannedGetInfoActivity.this, MainActivity.class);
        startActivity(backToMainIntent);

    }



}