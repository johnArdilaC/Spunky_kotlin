package com.ppg.spunky_kotlin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View

import kotlinx.android.synthetic.main.activity_takephoto.*

class TakePhotoActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dispatchTakePictureIntent()
        setContentView(R.layout.activity_takephoto)

        implementListeners()
    }

    private fun implementListeners() {
        btnOtherPhoto.setOnClickListener { launchTakeOtherPhotoActivity() }
        btnExitPhoto.setOnClickListener(View.OnClickListener { launchExit() })
    }


    fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data.extras
            val imageBitmap = extras!!.get("data") as Bitmap
            photoView.setImageBitmap(imageBitmap)
        }
    }

    private fun launchTakeOtherPhotoActivity() {
        val intent = Intent(this, RecordMemoryActivity::class.java)
        startActivity(intent)
    }

    private fun launchExit() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
