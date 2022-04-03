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
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("Users");
        QRsRef = db.collection("QRcode");
        leaderboardLinearLayout = findViewById(R.id.leaderboardViewerLinearLayout);
        userScoreButton = findViewById(R.id.leaderboardUserScoreButton);
        userScansButton = findViewById(R.id.leaderboardUserScansButton);
        qrScoreButton = findViewById(R.id.leaderbaordQRScoreButton);
        yourRankText = findViewById(R.id.userPositionText);
        Intent intent = getIntent();
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
        loadUserQrs();
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

    private void queryUsersByScore(){
        placed = false;
        usersRef.orderBy("totalScore", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String score = String.valueOf(document.get("totalScore"));
                        String id = document.getId();
                        addDocumentToScrollView(id, score, "user", document);
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

    private void queryUsersByScans(){
        placed = false;
        usersRef.orderBy("totalScans", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String score = String.valueOf(document.get("totalScans"));
                        String id = document.getId();
                        addDocumentToScrollView(id, score, "user", document);
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

    private void queryQRsByScore(){
        placed = false;
        QRsRef.orderBy("Score", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String score = String.valueOf(document.get("Score"));
                        String id = document.getId();
                        addDocumentToScrollView(id, score, "qr", document);
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

    private void addDocumentToScrollView(String ID, String score, String type, QueryDocumentSnapshot document){
        //Check if you are this position
        boolean yourDoc = false;
        if(type.equals("user") && !placed) {
            if (ID.equals(username)) {
                yourPos = rankingIterator;
                yourRankText.setText(username + " is at rank " + yourPos + " on this leaderboard");
                yourDoc = true;
                placed = true;
            }
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
        ImageButton viewButton = new ImageButton(this);
        viewButton.setId(rankingIterator);
        viewButton.setImageResource(android.R.drawable.ic_menu_search);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openViewer(ID, type);
            }
        });

        TextView rankingText = new TextView(this);
        rankingText.setText(String.valueOf(rankingIterator));
        rankingText.setTypeface(Typeface.DEFAULT_BOLD);
        rankingText.setPadding(0, 0 ,10, 0);
        rankingText.setTextSize(16);
        rankingText.setGravity(Gravity.CENTER_VERTICAL);


        TextView IDtext = new TextView(this);
        IDtext.setText(ID);
        IDtext.setEllipsize(TextUtils.TruncateAt.END);
        IDtext.setTextSize(16);
        IDtext.setMaxLines(1);
        IDtext.setGravity(Gravity.CENTER_VERTICAL);
        if(yourDoc) { IDtext.setTypeface(Typeface.DEFAULT_BOLD); }

        TextView scoreText = new TextView(this);
        scoreText.setText("Score: " + score);
        scoreText.setTextSize(16);
        scoreText.setGravity(Gravity.CENTER_VERTICAL);
        scoreText.setPadding(10, 0, 0, 0);


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        documentLayout.addView(rankingText);
        documentLayout.addView(IDtext, params);
        documentLayout.addView(scoreText);
        documentLayout.addView(viewButton);

        leaderboardLinearLayout.addView(documentLayout);

        rankingIterator++;
    }

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

    public void UsersScoreButton(View view){
        rankingIterator = 1;
        yourRankText.setText("");
        leaderboardLinearLayout.removeAllViews();
        userScoreButton.setBackgroundColor(getResources().getColor(R.color.purple_700, null));
        userScansButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        qrScoreButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        queryUsersByScore();
    }

    public void UsersScansButton(View view){
        rankingIterator = 1;
        yourRankText.setText("");
        leaderboardLinearLayout.removeAllViews();
        userScoreButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        userScansButton.setBackgroundColor(getResources().getColor(R.color.purple_700, null));
        qrScoreButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        queryUsersByScans();
    }

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