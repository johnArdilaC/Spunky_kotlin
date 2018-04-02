package com.ppg.spunky_kotlin

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.ppg.spunky_kotlin.R.id.btntakephoto
import kotlinx.android.synthetic.main.activity_record_memory.*

class RecordMemoryActivity : AppCompatActivity() {


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_memory)
        implementListeners()
    }

    private fun implementListeners() {
        btntakephoto.setOnClickListener { launchTakePhotoActivity() }

        btntakevideo.setOnClickListener(View.OnClickListener { launchTakeVideoActivity() })

        btnexit.setOnClickListener(View.OnClickListener { launchExit() })
    }

    private fun launchTakePhotoActivity() {
        val intent = Intent(this, TakePhotoActivity::class.java)
        startActivity(intent)
    }

    private fun launchTakeVideoActivity() {
        val intent = Intent(this, TakeVideoActivity::class.java)
        startActivity(intent)
    }

    private fun launchExit() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
