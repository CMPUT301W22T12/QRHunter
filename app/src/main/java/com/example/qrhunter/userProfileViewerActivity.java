package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

public class userProfileViewerActivity extends AppCompatActivity {
String userID;
FirebaseFirestore db = FirebaseFirestore.getInstance();
DocumentReference docRef;
boolean admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_viewer);

        Intent intent = getIntent();
        if (!intent.hasExtra("userID")) {
            Log.i(TAG, "No Intent Passed");
            finish();
        }
        userID = intent.getStringExtra("userID");
        docRef = db.collection("Users").document(userID);
        userHandler userHandle = new userHandler();
        try {
            admin = userHandle.getAdminStatus(this);
            Log.i(TAG, "Admin User");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(admin){
            ImageButton deleteButton = findViewById(R.id.qrCodeViewerDeleteButton);
            deleteButton.setVisibility(View.VISIBLE);
        }
        fillOutUserInfo();
    }

    private void fillOutUserInfo(){
        TextView userTitleText = findViewById(R.id.userProfileViewerTitleText);
        TextView userPointsText = findViewById(R.id.userProfileViewerPointsText);
        TextView numScansText = findViewById(R.id.userProfileViewerScansText);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String totalScore = String.valueOf(documentSnapshot.get("totalScore"));
                String username = documentSnapshot.getString("Username");
                String numScans = String.valueOf(documentSnapshot.get("totalScans"));
                userTitleText.setText(username);
                userPointsText.setText(username + " has a total score of: " + totalScore);
                numScansText.setText(username + " has scanned " + numScans + " unique QR Codes");
            }
        });
    }

    /**
     * Button to delete the currently open User. Only accessible by admin accounts
     * @param view current view to allow button interaction
     */
    public void deleteButton(View view){
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this QR Code?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteHandler deleter = new deleteHandler(db);
                        deleter.deleteUser(userID);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void userScoreLeaderboardButton(View view){
        Intent userScoreLeaderboardIntent = new Intent(userProfileViewerActivity.this, leaderboardActivity.class);
        userScoreLeaderboardIntent.putExtra("user", userID);
        userScoreLeaderboardIntent.putExtra("leaderboard", "userScore");
        startActivity(userScoreLeaderboardIntent);
    }

    public void userScansLeaderboardButton(View view){
        Intent userScoreLeaderboardIntent = new Intent(userProfileViewerActivity.this, leaderboardActivity.class);
        userScoreLeaderboardIntent.putExtra("user", userID);
        userScoreLeaderboardIntent.putExtra("leaderboard", "userScans");
        startActivity(userScoreLeaderboardIntent);
    }

    public void userQRScoreLeaderboardButton(View view){
        Intent userScoreLeaderboardIntent = new Intent(userProfileViewerActivity.this, leaderboardActivity.class);
        userScoreLeaderboardIntent.putExtra("user", userID);
        userScoreLeaderboardIntent.putExtra("leaderboard", "qrScore");
        startActivity(userScoreLeaderboardIntent);
    }


    public void backButtonPressed(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}