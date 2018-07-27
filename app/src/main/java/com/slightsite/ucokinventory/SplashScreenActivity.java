package com.slightsite.ucokinventory;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

public class SplashScreenActivity extends AppCompatActivity {

    private static final long SPLASH_TIMEOUT = 2000;
    private Button goButton;
    private boolean gone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initiateUI(savedInstanceState);
    }

    private void go() {
        gone = true;
        Intent newActivity = new Intent(SplashScreenActivity.this,
                MainActivity.class);
        startActivity(newActivity);
        SplashScreenActivity.this.finish();
    }

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    /**
     * Initiate this UI.
     * @param savedInstanceState
     */
    private void initiateUI(Bundle savedInstanceState) {
        setContentView(R.layout.layout_splash_screen);
        goButton = (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                go();
            }

        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        // Start long running operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 5;
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gone) go();
            }
        }, SPLASH_TIMEOUT);
    }
}
