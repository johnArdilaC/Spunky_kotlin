package com.ppg.spunky_kotlin

/**
 * Created by mariapc on 28/03/18.
 */
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate

class TestCheckable : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

    }
}