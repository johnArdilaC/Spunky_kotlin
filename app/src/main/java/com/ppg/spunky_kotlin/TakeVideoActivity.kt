package com.ppg.spunky_kotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.VideoView

    import kotlinx.android.synthetic.main.activity_takevideo.*

class TakeVideoActivity : AppCompatActivity() {

    private val START_CAMERA = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dispatchTakeVideoIntent()
        setContentView(R.layout.activity_takevideo)

        implementListeners()
    }

    private fun implementListeners() {
        btnOtherVideo.setOnClickListener { launchTakeOtherPhotoActivity() }
        btnPlay.setOnClickListener(View.OnClickListener { videoView.start() })
        btnExitVideo.setOnClickListener(View.OnClickListener { launchExit() })
    }

    private fun dispatchTakeVideoIntent() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (takeVideoIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takeVideoIntent, START_CAMERA)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        if (requestCode == START_CAMERA && resultCode == Activity.RESULT_OK) {
            val videoUri = intent.data
            videoView.setVideoURI(videoUri)
        }
        else{
            launchTakeOtherPhotoActivity()
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
