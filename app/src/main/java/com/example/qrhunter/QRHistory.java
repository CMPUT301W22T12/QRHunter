package com.example.qrhunter;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class QRHistory extends AppCompatActivity {
    ListView qrHistory;
    List history = new ArrayList();
    ArrayAdapter qrAdapter;

    String userID; //USE THIS SHIT BROO
    FirebaseFirestore db = FirebaseFirestore.getInstance(); //!!
    DocumentReference docRef; //!!

    ScoringHandler scoringHandler = new ScoringHandler(); //call scoringHandler class

    int qrAmount = history.size();
    int totalScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_history);

        qrHistory = (ListView)findViewById(R.id.qr_history);

        //history.add();

        qrAdapter = new ArrayAdapter(QRHistory.this,
                android.R.layout.simple_list_item_1,history);
        qrHistory.setAdapter(qrAdapter);

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
