package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class QRHistory extends AppCompatActivity {
    CollectionReference usersRef;
    ArrayList<String> userQRCodes;
    boolean myHistory = false;
    String userID; //USE THIS SHIT
    FirebaseFirestore db; //!!
    DocumentReference docRef; //!!
    ScoringHandler scoringHandler = new ScoringHandler(); //call scoringHandler class
    LinearLayout historyLinearLayout;

    ArrayList<String> userQRs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_history);
        db = FirebaseFirestore.getInstance();
        historyLinearLayout = findViewById(R.id.historyLinearLayout);
        historyLinearLayout.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        if (!intent.hasExtra("user")) {
            Log.i(TAG, "No Intent Passed");
            finish();
        }
        userID = intent.getStringExtra("user");
        docRef = db.collection("Users").document(userID);

        userHandler usrHandle = new userHandler();
        try {
            String myUsername = usrHandle.getUsername(this);
            if(myUsername.equals(userID)){
                myHistory = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //usersRef = db.collection("Users");
        historyUserData();
        loadUserQrs();

        if(userQRs != null) {
            for (String id : userQRs) {
                addDocumentToScrollView(id, String.valueOf(scoringHandler.hexStringReader(id)));
            }
        }

//        qrHistory = (ListView)findViewById(R.id.qr_history);
//
//        qrAdapter = new ArrayAdapter(QRHistory.this,
//                android.R.layout.simple_list_item_1,history);
//        qrHistory.setAdapter(qrAdapter);

    }

    /**
     * Fills out the user's information on the QR history page
     */
    private void historyUserData(){
        TextView historyUserName = findViewById(R.id.qr_history_user);
        TextView historyQRScanned = findViewById(R.id.history_qr_scanned);
        TextView historyTotalPoints = findViewById(R.id.history_total_score);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String totalScore = String.valueOf(documentSnapshot.get("totalScore"));
                String username = documentSnapshot.getString("Username");
                String numScans = String.valueOf(documentSnapshot.get("totalScans"));
                historyUserName.setText(username);
                historyTotalPoints.setText("Total score: " + totalScore);
                historyQRScanned.setText("Number of QR codes scanned: " + numScans);
            }
        });
    }

    public void getUserQrCodesId() {
        db.collection("Users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.contains("QRcodes")) {
                    userQRCodes = (ArrayList<String>) documentSnapshot.get("QRcodes");
                }else{
                    userQRCodes = null;
                }
            }
        });
    }

    /**
     * The builder function for the leaderboard. Will put a provided document into the leaderboard scroll view using the provided information
     * @param ID The ID of the document being added, either username or QR hash
     * @param score The score for the given leaderboard, either totalScore, totalScans or a QR's Score
     */
    private void addDocumentToScrollView(String ID, String score){

        LinearLayout documentLayout = new LinearLayout(QRHistory.this);
        documentLayout.setOrientation(LinearLayout.HORIZONTAL);

        //Button for opening the document's page
        ImageButton viewButton = new ImageButton(this);
        viewButton.setImageResource(android.R.drawable.ic_menu_delete);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Delete this code from user
                finish();
            }
        });


        //Text to show the documents id
        TextView IDtext = new TextView(this);
        IDtext.setText(ID);
        IDtext.setEllipsize(TextUtils.TruncateAt.END);
        IDtext.setTextSize(16);
        IDtext.setMaxLines(1);
        IDtext.setGravity(Gravity.CENTER_VERTICAL);

        //Text to show the document's score
        TextView scoreText = new TextView(this);
        scoreText.setText("Score: " + score);
        scoreText.setTextSize(16);
        scoreText.setGravity(Gravity.CENTER_VERTICAL);
        scoreText.setPadding(10, 0, 0, 0);

        //Add the views to the horizontal layout
        documentLayout.addView(IDtext);
        documentLayout.addView(scoreText);
        if(myHistory) {
            documentLayout.addView(viewButton);
        }

        //add the horizontal layout to the scrollview's linear layout
        historyLinearLayout.addView(documentLayout);
    }

    /**
     * This functions loads an array of the selected user's QR codes to be used in comparison
     */
    private void loadUserQrs(){
        db.collection("Users").document("jlatta2").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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

}
