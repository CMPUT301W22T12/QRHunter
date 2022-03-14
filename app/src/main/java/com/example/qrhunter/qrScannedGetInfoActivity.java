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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

        boolean scannedBefore = ifQRScannedBefore(shaString);
        //If the user took a picture to include
        if(includeImage){
            StorageReference storageRef = storage.getReference();
            SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
            String format = s.format(new Date());
            StorageReference photoRef = storageRef.child("images/" + shaString + "/" + format + ".jpg");
            InputStream stream = new FileInputStream(new File(objectImagePath));
            photoRef.putStream(stream);
        }
        //If the user made a comment
        String commentText;
        TextView commentTextBox = findViewById(R.id.commentOnNewCodeTextEdit);
        final String[] commentId = new String[0];
        Boolean isComment = !commentTextBox.toString().equals("");
        if(isComment){
            commentText = commentTextBox.toString();
            Map<String, Object> commentData = new HashMap<>();
            commentData.put("Text", commentText);
            commentData.put("QRcode", shaString);
            commentData.put("User", "INSERT USERNAME HERE");
            db.collection("Comments").add(commentData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    commentId[0] = documentReference.getId();
                }
            });
        }

        //if scanned before, just add details. Otherwise make new entry
        DocumentReference qrRef = db.collection("QRcode").document(shaString);
        if(scannedBefore){
            qrRef.update("Users", FieldValue.arrayUnion("INSERT USERNAME HERE"));
            if(isComment){ qrRef.update("Comments", "/Comments/" + commentId[0]); }
            if(includeImage){ }
        }else{
            Map<String, Object> qrData = new HashMap<>();
            qrData.put("Score", score);
            qrData.put("Users", Arrays.asList("PLEASE INSERT USERNAME HERE"));
            if(isComment){ qrData.put("Comments", Arrays.asList("/Comments/" + commentId[0])); }
            db.collection("QRcode").document(shaString).set(qrData);
        }
        CheckBox addLocationCheckbox = findViewById(R.id.addLocationOnCodeCheckbox);


        //add location to QR Code entry database
        if(addLocationCheckbox.isChecked()){

        }

        Intent backToMainIntent = new Intent(qrScannedGetInfoActivity.this, MainActivity.class);
        startActivity(backToMainIntent);

    }

    public boolean ifQRScannedBefore(String shaString){
        //Check if this QR has been scanned before
        final boolean[] scannedBefore = new boolean[1];
        DocumentReference docRef = db.collection("QRcode").document(shaString);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        scannedBefore[0] = true;
                    }else{
                        scannedBefore[0] = false;
                    }
                }
            }
        });
        return scannedBefore[0];
    }



}