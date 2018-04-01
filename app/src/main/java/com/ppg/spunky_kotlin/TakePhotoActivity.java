package com.ppg.spunky_kotlin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by John on 01/04/2018.
 */

public class TakePhotoActivity extends AppCompatActivity {

    ImageView photoView;
    Button otherPhoto,exitPhoto;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dispatchTakePictureIntent();
        setContentView(R.layout.activity_takephoto);
        findViewById();

        implementListeners();
    }

    private void implementListeners() {
        otherPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTakeOtherPhotoActivity();
            }
        });
        exitPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchExit();
            }
        });
    }

    private void findViewById() {
        otherPhoto=(Button)findViewById(R.id.btnOtherPhoto);
        exitPhoto=(Button)findViewById(R.id.btnExitPhoto);
        photoView=(ImageView)findViewById(R.id.photoView);
    }


    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            photoView.setImageBitmap(imageBitmap);
        }
    }


////////
    private void launchTakeOtherPhotoActivity() {
        Intent intent = new Intent(this, RecordMemoryActivity.class);
        startActivity(intent);
    }

    private void launchExit() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
