package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.List;


public class QRHistory extends AppCompatActivity {
    ListView qrHistory;
    List history = new ArrayList();
    ArrayAdapter qrAdapter;
    CollectionReference usersRef;
    String username;
    ArrayList<String> userQRs;
    boolean admin;

    String userID; //USE THIS SHIT
    FirebaseFirestore db = FirebaseFirestore.getInstance(); //!!
    DocumentReference docRef; //!!

    ScoringHandler scoringHandler = new ScoringHandler(); //call scoringHandler class

    int qrAmount = history.size();
    int totalScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_history);

        Intent intent = getIntent();
        if (!intent.hasExtra("userID")) {
            Log.i(TAG, "No Intent Passed");
            finish();
        }
        userID = intent.getStringExtra("userID");
        docRef = db.collection("Users").document(userID);

        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("Users");

        //historyUserData();
        //loadUserQrs();
/*
        qrHistory = (ListView)findViewById(R.id.qr_history);

        //history.add();

        qrAdapter = new ArrayAdapter(QRHistory.this,
                android.R.layout.simple_list_item_1,history);
        qrHistory.setAdapter(qrAdapter);
*/
    }

    /**
     * Fills out the user's information on the QR history page
     */
    private void historyUserData(){
        TextView historyUserName = findViewById(R.id.qr_history_user);
        TextView historyTotalPoints = findViewById(R.id.history_qr_scanned);
        TextView historyQRScanned = findViewById(R.id.history_total_score);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String totalScore = String.valueOf(documentSnapshot.get("totalScore"));
                String username = documentSnapshot.getString("Username");
                String numScans = String.valueOf(documentSnapshot.get("totalScans"));
                historyUserName.setText(username);
                historyTotalPoints.setText("Total score: " + totalScore);
                historyQRScanned.setText("Number of QR codes scanned" + numScans);
            }
        });
    }

    public void loadUserQrs(){
        db.collection("Users").document(username).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.contains("QRcodes")) {
                    history = (ArrayList) documentSnapshot.get("QRcodes");
                }else{
                    history = null;
                }
            }
        });
    }

    /**
     * Adding a QRCode to history with sha256 string as input
     * Also updates the amount of qr codes scanned and the total score of the user
     */
    public void addToHistory(String str) {
        history.add(str + scoringHandler.hexStringReader(str));
        qrAmount += 1;
        totalScore += getQRscore(str);
    }

    /**
     * Removing a QRCode to history with sha256 string as input
     * Also updates the amount of qr codes scanned and the total score of the user
     */
    public void removeFromHistory(String str) {
        history.remove(str + scoringHandler.hexStringReader(str));
        qrAmount -= 1;
        totalScore -= getQRscore(str);
    }

    /**
     * Returns the array list size
     */
    public void qrAmount() {
        qrAmount = history.size();
    }

    /**
     * Calls hexStringReader from the ScoringHandler class to
     */
    public int getQRscore(String str) {
        return scoringHandler.hexStringReader(str);
    }

}
