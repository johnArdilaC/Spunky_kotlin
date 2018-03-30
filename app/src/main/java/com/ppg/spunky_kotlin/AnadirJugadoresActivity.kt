package com.ppg.spunky_kotlin

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_anadir_jugadores.*
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL



class AnadirJugadoresActivity : AppCompatActivity() {

    private val BASE_URL = "https://us-central1-spunky-ppg.cloudfunctions.net/aleatorio"
    private var codigo = "Esperando"
    private var preguntas: IntArray = intArrayOf()


    override fun onCreate(savedInstanceState: Bundle?) {

        preguntas = intent.getIntArrayExtra(EscogerGrupoActivity.Constants.PREGUNTAS)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anadir_jugadores)

        AsyncTask.execute { getCodigo() }
    }


    fun getCodigo() {
        val url: URL
        var urlConnection: HttpURLConnection? = null

        try {
            url = URL(BASE_URL)
            urlConnection = url.openConnection() as HttpURLConnection
            val intxt = BufferedInputStream(urlConnection.inputStream)
            val reader = BufferedReader(InputStreamReader(intxt))
            var line = reader.readLine()

            while (line != null) {

                println(line)
                codigo = line
                runOnUiThread { textViewCodigo!!.text = codigo }

                line = reader.readLine()
            }

        } catch (e: Exception) {
            println(e)
        } finally {
            urlConnection!!.disconnect()
        }

    }

    fun jugar(view: View) {
        val intent = Intent(this, PreguntaActivity::class.java)
        intent.putExtra(EscogerGrupoActivity.Constants.PREGUNTAS, preguntas)
        startActivity(intent)
    }
}


