package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.io.IOException;
import java.util.ArrayList;

/**
 * This activity is used to display a selected users history of QR codes.
 * If viewing your own activity it allows the user to remove QR codes from their account
 */
public class QRHistory extends AppCompatActivity {
    boolean myHistory;
    String userID;
    FirebaseFirestore db; //!!
    DocumentReference docRef; //!!
    ScoringHandler scoringHandler = new ScoringHandler(); //call scoringHandler class
    LinearLayout historyLinearLayout;
    ArrayList<String> userQRs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_history);
        db = FirebaseFirestore.getInstance();
        historyLinearLayout = findViewById(R.id.historyViewerLinearLayout);
        myHistory = false;

        Intent intent = getIntent();
        if(intent.hasExtra("user")){
            userID = intent.getStringExtra("user");
        }else{
            userHandler userHandle = new userHandler();
            try {
                userID = userHandle.getUsername(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadUserQrs();

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


        historyUserData();

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

    /**
     * The builder function for the qr history. Will put a provided document into the qr history scroll view using the provided information
     * @param ID The ID of the document being added
     * @param score The score for the given qr code
     */
    private void addDocumentToScrollView(String ID, String score){
        LinearLayout documentLayout = new LinearLayout(QRHistory.this);
        documentLayout.setOrientation(LinearLayout.HORIZONTAL);

        //Button for opening the document's page
        ImageButton viewButton = new ImageButton(this);
        viewButton.setImageResource(android.R.drawable.ic_menu_search);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent QROpenIntent = new Intent(QRHistory.this, QRcodeViewerActivity.class);
                QROpenIntent.putExtra("QRcode", ID);
                startActivity(QROpenIntent);
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
        scoreText.setPadding(10, 0, 0, 20);

        //Add the views to the horizontal layout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        documentLayout.addView(IDtext, params);
        documentLayout.addView(scoreText);
        documentLayout.addView(viewButton);


        if(myHistory) {
            //Button for opening the document's page
            ImageButton deleteButton = new ImageButton(this);
            deleteButton.setImageResource(android.R.drawable.ic_menu_delete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Delete this code from user
                    deleteHandler deleter = new deleteHandler(db);
                    deleter.removeQRfromUser(userID, Integer.valueOf(score), ID);
                    loadUserQrs();
                }
            });
            documentLayout.addView(deleteButton);
        }

        //add the horizontal layout to the scrollview's linear layout
        historyLinearLayout.addView(documentLayout);
    }

    /**
     * This functions loads an array of the selected user's QR codes to be used in iteration
     */
    private void loadUserQrs(){
        historyLinearLayout.removeAllViewsInLayout();
        db.collection("Users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.contains("QRcodes")) {
                    userQRs = (ArrayList<String>) documentSnapshot.get("QRcodes");
                    for (String id : userQRs) {
                        addDocumentToScrollView(id, String.valueOf(scoringHandler.hexStringReader(id)));
                    }
                }else{
                    userQRs = null;
                }
            }
        });
    }

}
