package com.shady.mylocation.splash;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.shady.mylocation.MainActivity;
import com.shady.mylocation.R;

public class SplashActivity extends AppCompatActivity {

    private int SPLASH_TIME = 3000; //3 seconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        fireSplash();
    }

    private void fireSplash(){
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_TIME);
    }

}
