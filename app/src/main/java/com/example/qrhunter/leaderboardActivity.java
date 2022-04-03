package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Activity responsible for showing all leaderboards
 * Has 3 leaderboards available
 * If opened with the intent passing a "user" extra the leaderboard will open for the provided username, otherwise it opens for the logged in user
 * If opened with the intent passing a "leaderboard" extra (either "userScore", "userScans", or "qrScore") the leaderboard will automatically open the respective leaderboard
 */
public class leaderboardActivity extends AppCompatActivity {
    FirebaseFirestore db;
    CollectionReference usersRef;
    CollectionReference QRsRef;
    String username;
    LinearLayout leaderboardLinearLayout;
    int yourPos;
    int rankingIterator = 1;
    Button userScoreButton, userScansButton, qrScoreButton;
    TextView yourRankText;
    ArrayList<String> userQRs;
    boolean placed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        //Initialize all views and database variables
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("Users");
        QRsRef = db.collection("QRcode");
        leaderboardLinearLayout = findViewById(R.id.leaderboardViewerLinearLayout);
        userScoreButton = findViewById(R.id.leaderboardUserScoreButton);
        userScansButton = findViewById(R.id.leaderboardUserScansButton);
        qrScoreButton = findViewById(R.id.leaderbaordQRScoreButton);
        yourRankText = findViewById(R.id.userPositionText);
        Intent intent = getIntent();

        //Check if the leaderboard is being opened for a specific user
        if(intent.hasExtra("user")){
            username = intent.getStringExtra("user");
        }else{
            userHandler userHandle = new userHandler();
            try {
                username = userHandle.getUsername(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadUserQrs(); //load a list of all the selected users QR codes for comparison in the QR Score leaderboard
        // Check if a specific leaderboard should start open
        if(intent.hasExtra("leaderboard")){
            String leaderboardType = intent.getStringExtra("leaderboard");
            if(leaderboardType.equals("userScore")){
                userScoreButton.performClick();
            }else if(leaderboardType.equals("userScans")){
                userScansButton.performClick();
            }else if(leaderboardType.equals("qrScore")){
                qrScoreButton.performClick();
            }
        }
    }

    /**
     * This function will query the database for all users sorted by their total score descending and build the respective leaderboard
     */
    private void queryUsersByScore(){
        placed = false;
        usersRef.orderBy("totalScore", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String score = String.valueOf(document.get("totalScore"));
                        String id = document.getId();
                        addDocumentToScrollView(id, score, "user");
                    }
                }else{
                    Log.d(TAG, "error getting documents: ", task.getException());
                }
            }
        });
        if(!placed){
            yourRankText.setText(username + " has not placed on this leaderboard");
        }
    }

    /**
     * This function will query the database for all users sorted by their count of total scans descending and build the respective leaderboard
     */
    private void queryUsersByScans(){
        placed = false;
        usersRef.orderBy("totalScans", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String score = String.valueOf(document.get("totalScans"));
                        String id = document.getId();
                        addDocumentToScrollView(id, score, "user");
                    }
                }else{
                    Log.d(TAG, "error getting documents: ", task.getException());
                }
            }
        });
        if(!placed){
            yourRankText.setText(username + " has not placed on this leaderboard");
        }
    }

    /**
     * This function will query the database for all QR codes sorted by score descending and build the respective leaderboard
     */
    private void queryQRsByScore(){
        placed = false;
        QRsRef.orderBy("Score", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String score = String.valueOf(document.get("Score"));
                        String id = document.getId();
                        addDocumentToScrollView(id, score, "qr");
                    }
                }else{
                    Log.d(TAG, "error getting documents: ", task.getException());
                }
            }
        });
        if(!placed){
            yourRankText.setText(username + " has not placed on this leaderboard");
        }
    }

    /**
     * The builder function for the leaderboard. Will put a provided document into the leaderboard scroll view using the provided information
     * @param ID The ID of the document being added, either username or QR hash
     * @param score The score for the given leaderboard, either totalScore, totalScans or a QR's Score
     * @param type The type of document, either "user" or "qr"
     */
    private void addDocumentToScrollView(String ID, String score, String type){
        //Check if you are/own this item
        boolean yourDoc = false;
        //if you haven't already been placed on this leaderboard and this is a user document, check if it is the same user
        if(type.equals("user") && !placed) {
            if (ID.equals(username)) {
                yourPos = rankingIterator;
                yourRankText.setText(username + " is at rank " + yourPos + " on this leaderboard");
                yourDoc = true;
                placed = true;
            }
        //if you haven't already been placed on this leaderboard and this is a qr document, check if it is owned by the user
        }else if(type.equals("qr") && !placed){
            if(userQRs.contains(ID)){
                yourPos = rankingIterator;
                yourRankText.setText(username + " is at rank " + yourPos + " on this leaderboard");
                yourDoc = true;
                placed = true;
            }
        }
        LinearLayout documentLayout = new LinearLayout(leaderboardActivity.this);
        documentLayout.setOrientation(LinearLayout.HORIZONTAL);

        //Button for opening the document's page
        ImageButton viewButton = new ImageButton(this);
        viewButton.setId(rankingIterator);
        viewButton.setImageResource(android.R.drawable.ic_menu_search);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openViewer(ID, type);
            }
        });

        //Text to show the rank
        TextView rankingText = new TextView(this);
        rankingText.setText(String.valueOf(rankingIterator));
        rankingText.setTypeface(Typeface.DEFAULT_BOLD);
        rankingText.setPadding(0, 0 ,10, 0);
        rankingText.setTextSize(16);
        rankingText.setGravity(Gravity.CENTER_VERTICAL);

        //Text to show the documents id
        TextView IDtext = new TextView(this);
        IDtext.setText(ID);
        IDtext.setEllipsize(TextUtils.TruncateAt.END);
        IDtext.setTextSize(16);
        IDtext.setMaxLines(1);
        IDtext.setGravity(Gravity.CENTER_VERTICAL);
        //If you are/own this item, bold the ID
        if(yourDoc) { IDtext.setTypeface(Typeface.DEFAULT_BOLD); }

        //Text to show thed document's score
        TextView scoreText = new TextView(this);
        scoreText.setText("Score: " + score);
        scoreText.setTextSize(16);
        scoreText.setGravity(Gravity.CENTER_VERTICAL);
        scoreText.setPadding(10, 0, 0, 0);

        //Add the views to the horizontal layout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        documentLayout.addView(rankingText);
        documentLayout.addView(IDtext, params);
        documentLayout.addView(scoreText);
        documentLayout.addView(viewButton);

        //add the horizontal layout to the scrollview's linear layout
        leaderboardLinearLayout.addView(documentLayout);

        //increment the ranking iterator
        rankingIterator++;
    }

    /**
     * This functions loads an array of the selected user's QR codes to be used in comparison
     */
    private void loadUserQrs(){
        db.collection("Users").document(username).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.contains("QRcodes")) {
                    userQRs = (ArrayList<String>) documentSnapshot.get("QRcodes");
                }else{
                    userQRs = null;
                }
            }
        });
    }

    /**
     * This function opens the respective Activity for the selected item.
     * used by the leaderboards onClick function
     * @param ID ID of the selected item. either username or QR hash
     * @param type type of item. "user" or "qr"
     */
    private void openViewer(String ID, String type){
        if(type.equals("user")){
            Intent userViewIntent = new Intent(leaderboardActivity.this, userProfileViewerActivity.class);
            userViewIntent.putExtra("userID", ID);
            startActivity(userViewIntent);
        }else if(type.equals("qr")){
            Intent qrViewIntent = new Intent(leaderboardActivity.this, QRcodeViewerActivity.class);
            qrViewIntent.putExtra("QRcode", ID);
            startActivity(qrViewIntent);
        }
    }

    /**
     * Button function to load the User Score Leaderboard
     * @param view standard onclick function parameter
     */
    public void UsersScoreButton(View view){
        rankingIterator = 1;
        yourRankText.setText("");
        leaderboardLinearLayout.removeAllViews();
        userScoreButton.setBackgroundColor(getResources().getColor(R.color.purple_700, null));
        userScansButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        qrScoreButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        queryUsersByScore();
    }

    /**
     * Button function to load the User Scans Leaderboard
     * @param view standard onclick function parameter
     */
    public void UsersScansButton(View view){
        rankingIterator = 1;
        yourRankText.setText("");
        leaderboardLinearLayout.removeAllViews();
        userScoreButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        userScansButton.setBackgroundColor(getResources().getColor(R.color.purple_700, null));
        qrScoreButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        queryUsersByScans();
    }

    /**
     * Button function to load the QR Score Leaderboard
     * @param view standard onclick function parameter
     */
    public void QRScoreButton(View view){
        rankingIterator = 1;
        yourRankText.setText("");
        leaderboardLinearLayout.removeAllViews();
        userScoreButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        userScansButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        qrScoreButton.setBackgroundColor(getResources().getColor(R.color.purple_700, null));
        queryQRsByScore();
    }

    public void backButtonPressed(View view){
        onBackPressed();
    }

}