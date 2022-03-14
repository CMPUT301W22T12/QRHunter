package com.example.qrhunter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button userProfileBtn = (Button) findViewById(R.id.profile_button);
        userProfileBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v) {
                startActivity(new Intent(MainActivity.this, UserProfile.class));
            }
        });
    }


}