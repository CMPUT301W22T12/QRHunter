package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class userSearchActivity extends AppCompatActivity {
    EditText usernameView;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        db = FirebaseFirestore.getInstance();
        usernameView = findViewById(R.id.usernameSearchEditText);

        Intent intent = getIntent();
        if(intent.hasExtra("FailedSearch")){
            Toast.makeText(this, "Invalid QR Scanned, not a user search QR.\n Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    public void searchByUsernameButton(View view){
        String username = usernameView.getText().toString().trim();
        if(username.isEmpty()){
            usernameView.setError("Please input a username to search");
            return;
        }else if(username.contains("_") || username.contains(" ")){
            usernameView.setError("Username contains invalid characters '_' or ' ', please try again");
            return;
        }else{
            DocumentReference docRef = db.collection("Users").document(username);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.getResult().exists()){
                        Intent userViewIntent = new Intent(userSearchActivity.this, userProfileViewerActivity.class);
                        userViewIntent.putExtra("userID", username);
                        startActivity(userViewIntent);
                        finish();
                    }else{
                        Toast.makeText(userSearchActivity.this, "No user with the username: " + username + " exists.\nPlease try a different username", Toast.LENGTH_LONG).show();
                        usernameView.setError("No user with the username: " + username + " exists.\nPlease try a different username");

                    }
                }
            });
        }
        return;
    }

    public void searchByQRButton(View view){
        //open camera to scan QR Code
        Intent openQRScan = new Intent(userSearchActivity.this, userSearchQRScanActivity.class);
        startActivity(openQRScan);
        finish();
    }

    public void backButtonPressed(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

}