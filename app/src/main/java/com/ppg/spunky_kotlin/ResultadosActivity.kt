package com.ppg.spunky_kotlin

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_resultados.*

class ResultadosActivity : AppCompatActivity() {

    private var gyroscope: Sensor? = null
    private var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados)

        val puntaje=intent.getIntExtra(EscogerGrupoActivity.Constants.PUNTAJE, 0)

        val text = "1. Apodo: $puntaje puntos"
        

        text_resultado.text = text
    }

    fun enviarReto(view: View) {
            val intent = Intent(this, EnviarRetoActivity::class.java)
            startActivity(intent)


    }



}
