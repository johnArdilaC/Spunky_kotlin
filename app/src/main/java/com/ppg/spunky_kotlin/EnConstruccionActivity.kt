package com.ppg.spunky_kotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate

class EnConstruccionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_en_construccion)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    }
}
