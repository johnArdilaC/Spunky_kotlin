package com.ppg.spunky_kotlin

import android.content.Context
import android.content.Intent

import android.hardware.Sensor
import android.hardware.SensorManager
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_resultados.*

class ResultadosActivity : AppCompatActivity() {


    private var gyroscope: Sensor? = null
    private var sensorManager: SensorManager? = null
    private var prefs: SharedPreferences? = null

    private var hayGiro: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados)

        prefs = applicationContext.getSharedPreferences(EscogerGrupoActivity.Constants.PREFS_FILENAME, Context.MODE_PRIVATE)

        val apodo = prefs!!.getString(EscogerGrupoActivity.Constants.APODO,"default")

        val puntaje=intent.getIntExtra(EscogerGrupoActivity.Constants.PUNTAJE, 0)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        gyroscope = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        verificarGiro()

        val text = "1.$apodo: $puntaje puntos"


        text_resultado.text = text
    }

    fun enviarReto(view: View) {
        if(hayGiro){
            val intent = Intent(this, EnviarRetoGiroActivity::class.java)
            startActivity(intent)
        }
        else{
            val intent = Intent(this, EnviarRetoActivity::class.java)
            startActivity(intent)
        }
    }

    fun verificarGiro()
    {
        if(gyroscope==null){
            println("NO HAY GIROSCOPIO")
        }
        else{
            hayGiro=true
        }
    }


}
