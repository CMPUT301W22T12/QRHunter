package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * qrDatabaseAddHandler is a handler class used for adding Images, Comments and QR codes into the Firestore Database
 */
public class qrDatabaseAddHandler {
    public String username;

    /**
     * Constructor function for the handler
     * @param context context from which it is called, required for retrieving username
     * @throws IOException Potential IO exception when reading userInfo file
     */
    public qrDatabaseAddHandler(Context context) throws IOException {
        userHandler userHand = new userHandler();
        username = userHand.getUsername(context);
    }

    /**
     * Adds a provided image to the Firebase Storage location with all required information
     * @param shaString String with the relevant QR Codes hash id
     * @param storage reference to the Firebase Storage location
     * @param objectImagePath Path to location of image file on device
     * @return String address of the images location in Firebase Storage
     * @throws FileNotFoundException
     */
    public String addImageToStorage(String shaString, FirebaseStorage storage, String objectImagePath) throws FileNotFoundException {
        StorageReference storageRef = storage.getReference();
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String format = s.format(new Date());
        String photoRefLocation = "images/" + shaString + "/" + format + ".jpg";
        StorageReference photoRef = storageRef.child(photoRefLocation);
        Log.i(TAG, "PhotoRef location: " + photoRefLocation);
        InputStream stream = new FileInputStream(new File(objectImagePath));
        UploadTask uploadTask = photoRef.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Unsuccessful Upload - error code: " + e);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "Successful Upload!");
            }
        });
        return photoRefLocation;
    }

    /**
     * This function adds a comment and all relevant information to the Firestore Database
     * @param commentText String representing the text of the comment
     * @param shaString String with the relevant QR Codes hash id
     * @param db Reference to the Firestore Database
     * @return String of the Comment's ID
     */
    public String addCommentToDB(String commentText, String shaString, FirebaseFirestore db){
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("Text", commentText);
        commentData.put("QRcode", shaString);
        commentData.put("User", username);
        String uid = username;
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String format = s.format(new Date());
        String commentId = "C-" + uid + "-" + format;
        db.collection("Comments").document(commentId).set(commentData);

        //add comment to user profile
        db.collection("Users").document(username).update("Comments", FieldValue.arrayUnion(commentId));

        return commentId;
    }

    /**
     * This function adds a QR to the Firestore Database along with all relevant information being included
     * Also adds a record of the scan to the User's profile
     * @param commentId ID of an included comment if there is one, use empty string if not applicable
     * @param imagePath Path of image on device to be included, use empty string if not applicable
     * @param score Score of the QR Code
     * @param latitude Current latitude to associate with QR Code, use 0 if not applicable
     * @param longitude Current longitude to associate with QR Code, use 0 if not applicable
     * @param shaString ID of QR Code (Hash of contents)
     * @param db Reference to Firestore Database
     */
    public void addQRToDB(String commentId, String imagePath, int score, double latitude, double longitude, String shaString, FirebaseFirestore db){
        DocumentReference docRef = db.collection("QRcode").document(shaString);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    //If the QR exists in the database only update the relevant fields, do not create new entry
                    if(document.exists()){
                        docRef.update("Users", FieldValue.arrayUnion(username));
                        if(commentId.length() > 0){ docRef.update("Comments", FieldValue.arrayUnion(commentId)); }
                        if(imagePath.length() > 0){ docRef.update("Images", FieldValue.arrayUnion(imagePath)); }
                    }else{ //Doesn't exist, create new entry with all required info
                        Map<String, Object> qrData = new HashMap<>();
                        qrData.put("Score", score);
                        qrData.put("Users", Arrays.asList(username));
                        if(commentId.length() > 0){ qrData.put("Comments", Arrays.asList(commentId)); }
                        if(imagePath.length() > 0){ qrData.put("Images", Arrays.asList(imagePath)); }
                        if(latitude != 0 && longitude != 0){ qrData.put("Location", new GeoPoint(latitude, longitude)); }
                        db.collection("QRcode").document(shaString).set(qrData);
                    }

                    //add record of scan to user profile
                    DocumentReference userDoc = db.collection("Users").document(username);
                    userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                userDoc.update("QRcodes", FieldValue.arrayUnion(shaString));
                                userDoc.update("totalScore", FieldValue.increment(score));
                            }
                        }
                    });
                }
            }
        });
        return;
    }

}