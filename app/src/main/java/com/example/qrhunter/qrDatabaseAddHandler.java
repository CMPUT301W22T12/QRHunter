package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class qrDatabaseAddHandler {

    public qrDatabaseAddHandler(){}

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

    public String addCommentToDB(String commentText, String shaString, FirebaseFirestore db){
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("Text", commentText);
        commentData.put("QRcode", shaString);
        commentData.put("User", "INSERT USERNAME HERE");
        String uid = "GETUSERID";
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String format = s.format(new Date());
        String commentId = "C-" + uid + "-" + format;
        db.collection("Comments").document(commentId).set(commentData);
        return commentId;
    }

    public void addQRToDB(String commentId, String imagePath, int score, String shaString, FirebaseFirestore db){
        DocumentReference docRef = db.collection("QRcode").document(shaString);
        Log.i(TAG, "commentId = " + commentId);
        Log.i(TAG, "imagePath = " + imagePath);
        Log.i(TAG, "User ID = " + FirebaseAuth.getInstance().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        docRef.update("Users", FieldValue.arrayUnion("INSERT USERNAME HERE"));
                        if(commentId.length() > 0){ docRef.update("Comments", FieldValue.arrayUnion(commentId)); }
                        if(imagePath.length() > 0){ docRef.update("Images", FieldValue.arrayUnion(imagePath)); }
                    }else{
                        Map<String, Object> qrData = new HashMap<>();
                        qrData.put("Score", score);
                        qrData.put("Users", Arrays.asList("PLEASE INSERT USERNAME HERE"));
                        if(commentId.length() > 0){ qrData.put("Comments", Arrays.asList(commentId)); }
                        if(imagePath.length() > 0){ qrData.put("Images", Arrays.asList(imagePath)); }
                        db.collection("QRcode").document(shaString).set(qrData);
                    }

                }
            }
        });
    }
}