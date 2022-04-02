package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Document;

import java.util.List;

/**
 * Handler for all database delete functions.
 * Ability to delete QRcodes and Users, along with anything related to those documents
 */
public class deleteHandler {
FirebaseFirestore db;
FirebaseStorage storage;

    public deleteHandler(FirebaseFirestore db, FirebaseStorage storage){
        this.db = db;
        this.storage = storage;
    }

    public deleteHandler(FirebaseFirestore db){
        this.db = db;
    }

    /**
     * Delete the provided QR Code from the Database
     * Also deletes all associated images, comments and removes reference to the code from every user that has scanned it
     * @param qrID database ID of the QR Code
     */
    public void deleteQRcode(String qrID){
        DocumentReference docRef = db.collection("QRCode").document(qrID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                int score = (int) documentSnapshot.get("Score");
                List<String> commentList = (List<String>) documentSnapshot.get("Comments");
                for(String commentId : commentList){
                    deleteComment(commentId);
                }
                List<String> imageList = (List<String>) documentSnapshot.get("Images");
                for(String imageLocation : imageList){
                    deleteImage(imageLocation);
                }
                List<String> usersList = (List<String>) documentSnapshot.get("Users");
                for(String user : usersList){
                    removeQRfromUser(user, score, qrID);
                }
            }
        });
        docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "QR successfully deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error deleting QR", e);
            }
        });
    }

    public void deleteUser(String userID){
        DocumentReference docRef = db.collection("Users").document(userID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> QRList = (List<String>) documentSnapshot.get("QRcodes");
                for(String qrCode : QRList){
                    removeUserFromQR(userID, qrCode);
                }
                List<String> CommentList = (List<String>) documentSnapshot.get("Comments");
                for (String commentID : CommentList){
                    deleteComment(commentID);
                }
            }
        });
        docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "User successfully deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error deleting User", e);
            }
        });
    }

    /**
     * Deletes a comment from the database
     * @param commentId database ID of the comment
     */
    private void deleteComment(String commentId){
        db.collection("Comments").document(commentId)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Comment successfully deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting comment", e);
                    }
                });
    }

    /**
     * Deletes an image from Firebase Storage
     * @param imageLocation Firebase gs link to the image location
     */
    private void deleteImage(String imageLocation){
        StorageReference storageReference = storage.getReferenceFromUrl(imageLocation);

        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "Image successfully deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error deleting image", e);
            }
        });
    }

    /**
     * Remove reference of a QR code from a given user. Deletes the code and reduces the users score.
     * @param userId ID of the user
     * @param score Score of the QR Code
     * @param qrID ID of the QR code
     */
    private void removeQRfromUser(String userId, int score, String qrID){
        DocumentReference userDoc = db.collection("Users").document(userId);
        userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userDoc.update("Score", FieldValue.increment(-1*score));
                userDoc.update("QRcides", FieldValue.arrayRemove(qrID));
            }
        });
    }

    private void removeUserFromQR(String userID, String qrID){
        DocumentReference qrDoc = db.collection("QRcode").document(qrID);
        qrDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                qrDoc.update("Users", FieldValue.arrayRemove(userID));
            }
        });
    }
}
