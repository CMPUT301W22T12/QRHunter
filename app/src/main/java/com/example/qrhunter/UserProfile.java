package com.example.qrhunter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class UserProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        final Button goToLeaderboard = (Button) findViewById(R.id.leaderboard);
        goToLeaderboard.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v) {
                startActivity(new Intent(UserProfile.this, Leaderboard.class));
            }
        });
    }
}