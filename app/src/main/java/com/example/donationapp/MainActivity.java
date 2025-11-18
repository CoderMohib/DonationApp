package com.example.donationapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.donationapp.view.SplashActivity;

/**
 * Main Activity - Redirects to SplashActivity
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Redirect to SplashActivity
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }
}