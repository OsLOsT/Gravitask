package com.sp.gravitask;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Intent intent = new Intent(getApplicationContext(), Gravitask.class);
        startActivity(intent);

        MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.anone);
        mp.start(); //Starts your sound

        finish();
    }

}
