package com.ppg.spunky_kotlin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class RecordMemoryActivity extends AppCompatActivity{

    Button takePhoto,takeVideo,exit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_memory);
        findViewById();

        implementListeners();
    }

    private void implementListeners() {
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTakePhotoActivity();
            }
        });

        takeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTakeVideoActivity();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchExit();
            }
        });
    }

    private void findViewById() {
        takePhoto=(Button)findViewById(R.id.btntakephoto);
        takeVideo=(Button) findViewById(R.id.btntakevideo);
        exit=(Button) findViewById(R.id.btnexit);
    }



    private void launchTakePhotoActivity() {
        Intent intent = new Intent(this, TakePhotoActivity.class);
        startActivity(intent);
    }

    private void launchTakeVideoActivity() {
        Intent intent = new Intent(this, TakeVideoActivity.class);
        startActivity(intent);
    }

    private void launchExit() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}
