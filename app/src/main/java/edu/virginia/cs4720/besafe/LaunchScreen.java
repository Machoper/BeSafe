package edu.virginia.cs4720.besafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LaunchScreen extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent();
                setResult(Activity.RESULT_OK, homeIntent);
                //startActivity(homeIntent);
                finishAfterTransition();
            }
        }, SPLASH_TIME_OUT);

    }
}
