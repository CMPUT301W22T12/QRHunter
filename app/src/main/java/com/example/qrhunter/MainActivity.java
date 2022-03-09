package com.example.qrhunter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Camera;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void switchActivity(View view) {
        Intent switchActivityIntent = new Intent(this, CameraActivity.class);
        startActivity(switchActivityIntent);
    }
}