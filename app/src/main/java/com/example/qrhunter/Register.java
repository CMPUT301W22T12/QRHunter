package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Activity for registering a new user
 */
public class Register extends AppCompatActivity {

    EditText Username, PlayerName, Email,Phone;
    Button register;
    TextView loginPrompt;
    FirebaseFirestore fstore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Get all relevant views from activity
        Username = findViewById(R.id.editTextUsernameText);
        PlayerName = findViewById(R.id.editTextPersonName);
        Email = findViewById(R.id.editTextTextEmailAddress);
        Phone = findViewById(R.id.editTextPhone);
        register = findViewById(R.id.register_button);
        loginPrompt = findViewById(R.id.alreadyRegisteredLoginText);

        fstore = FirebaseFirestore.getInstance();


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get values from all editText fields
                String email = Email.getText().toString().trim();
                String username = Username.getText().toString().trim();
                String playerName = PlayerName.getText().toString().trim();
                String Pno = Phone.getText().toString().trim();
                Log.i(TAG, "username: " + username);

                //Check if username is empty
                if(TextUtils.isEmpty(username)){
                    Username.setError("Username is required");
                    return;
                }
                //Check username for invalid characters
                if(username.contains("_") || username.contains(" ")){
                    Username.setError("The characters '_' and ' ' are forbidden. Please pick a new username");
                    return;
                }


                //Add username to Firestore Database
                DocumentReference docRef = fstore.collection("Users").document(username);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){
                            addUserToDatabase(username, email, playerName, Pno);
                        }else{
                            Username.setError("Username Taken");
                            return;
                        }
                    }
                });
            }
        });

        loginPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
            }
        });
    }

    /**
     * Function for adding a user to the FireStore Database
     * @param username String of the user's username (id)
     * @param email String of the user's email (can be empty String)
     * @param playerName String of the user's name (can be empty String)
     * @param phoneNum String of user's phone number (can be empty String)
     */
    public void addUserToDatabase(String username, String email, String playerName, String phoneNum){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put("Username", username);
        userInfo.put("Email", email);
        userInfo.put("Name", playerName);
        userInfo.put("PhoneNum", phoneNum);
        userInfo.put("totalScore", 0);
        userInfo.put("QRcodes", FieldValue.arrayUnion());
        db.collection("Users").document(username).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(Register.this, "Account Created!", Toast.LENGTH_SHORT).show();
                File userInfoFile = new File(getFilesDir(), "userInfo");
                FileOutputStream userInfoStream = null;
                try {
                    userInfoStream = new FileOutputStream(userInfoFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    userInfoStream.write(username.getBytes(StandardCharsets.UTF_8));
                    userInfoStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                userInfoFile.setReadOnly();
                Intent goToMainActivityIntent = new Intent(Register.this, MainActivity.class);
                startActivity(goToMainActivityIntent);
                finish();
            }
        });
    }
}