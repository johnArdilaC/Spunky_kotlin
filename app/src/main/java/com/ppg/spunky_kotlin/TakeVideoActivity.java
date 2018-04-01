package com.ppg.spunky_kotlin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

/**
 * Created by John on 01/04/2018.
 */

public class TakeVideoActivity extends AppCompatActivity {

    Button btnOtherVideo, btnPlay, btnExit;
    VideoView videoView;

    private int START_CAMERA=0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dispatchTakeVideoIntent();
        setContentView(R.layout.activity_takevideo);
        findViewById();


        implementListeners();
    }

    private void findViewById() {
        btnOtherVideo=(Button)findViewById(R.id.btnOtherVideo);
        btnPlay=(Button)findViewById(R.id.btnPlay);
        btnExit=(Button)findViewById(R.id.btnExitVideo);
        videoView=(VideoView) findViewById(R.id.videoView);
    }

    private void implementListeners() {
        btnOtherVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTakeOtherPhotoActivity();
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoView.start();
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchExit();
            }
        });
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, START_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == START_CAMERA && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            videoView.setVideoURI(videoUri);
        }
    }

    private void launchTakeOtherPhotoActivity() {
        Intent intent = new Intent(this, RecordMemoryActivity.class);
        startActivity(intent);
    }

    private void launchExit() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
