package com.ppg.spunky_kotlin

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_anadir_jugadores.*
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AnadirJugadoresActivity : AppCompatActivity() {


    //Constantes
    companion object {
        val BASE_URL = "https://us-central1-spunky-ppg.cloudfunctions.net/aleatorio"
        val CODIGO = "-"
        val NO_GENERADO = "NO GENERADO"
        val NO_ESCRITO = "NO ESCRITO"
        val ESCRITO = "ESCRITO"
        val PREGUNTAS = "_PREGUNTAS_"
        val HOST = "HOST"
    }

    private var arrayAdapter: ArrayAdapter<*>? = null

    private var mRootDB: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var mDatabaseReferenceConexiones: DatabaseReference = mRootDB.reference.child("Conexiones")


    private var codigo = CODIGO
    private var preguntas: IntArray = intArrayOf()
    private var android_id: String = ""
    private var escribirBD: String = NO_GENERADO
    private var apodos: Array<String> = arrayOf()

    private var prefs: SharedPreferences?=null



    override fun onCreate(savedInstanceState: Bundle?) {

        android_id = Settings.Secure.getString(this.contentResolver,
                Settings.Secure.ANDROID_ID)

        preguntas = intent.getIntArrayExtra(EscogerGrupoActivity.Constants.PREGUNTAS)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anadir_jugadores)

        prefs = applicationContext.getSharedPreferences(EscogerGrupoActivity.Constants.PREFS_FILENAME, Context.MODE_PRIVATE)

        mDatabaseReferenceConexiones.keepSynced(true)

    }

    override fun onStart() {
        super.onStart()
        if (codigo == CODIGO) {
            AsyncTask.execute {

                getCodigo()
            }
        }
        textViewCodigo.text=codigo
    }

    /**
     * Trae un codigo aleatorio usando la funcion de Firebase
     */
    private fun getCodigo() {
        val url: URL
        var urlConnection: HttpURLConnection? = null

        try {
            url = URL(BASE_URL)
            urlConnection = url.openConnection() as HttpURLConnection
            val inbff = BufferedInputStream(urlConnection.inputStream)
            val reader = BufferedReader(InputStreamReader(inbff))
            val line: String = reader.readLine()
                println(line)
                codigo = line
                runOnUiThread { textViewCodigo.text = codigo }

        } catch (e: Exception) {
            println(e)
        } finally {
            urlConnection!!.disconnect()
            obtenerConexiones()
            escribirBD = NO_ESCRITO
        }

    }

    /**
     * Crea al host en la BD y actualiza a los jugadores que se vayan uniendo
     */
    private fun obtenerConexiones() {

        //Listener base de datos
        val listenerConexiones = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                println(codigo != CODIGO)

                if (escribirBD == NO_ESCRITO) {
                    //Escribe al host en la BD
                    mDatabaseReferenceConexiones.child(codigo).child(android_id).setValue(HOST)
                    //Escribe las preguntas en la BD
                    val preguntasString = preguntas.toMutableList().toString().replace("[","{").replace("]","}")

                    mDatabaseReferenceConexiones.child(codigo).child(PREGUNTAS).setValue(preguntasString)
                    escribirBD = ESCRITO
                } else if (escribirBD == ESCRITO) {

                    val jugadores = dataSnapshot.child(codigo).children
                    jugadores.forEach {
                        val llave = it.key
                        if (llave != PREGUNTAS && it.value.toString() != HOST) {
                            apodos+=it.value!!.toString()
                        }
                    }

                    arrayAdapter = ArrayAdapter(this@AnadirJugadoresActivity, android.R.layout.simple_list_item_1, apodos)
                    listView.adapter = arrayAdapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }

        mDatabaseReferenceConexiones.addValueEventListener(listenerConexiones)
    }

    fun jugar(view: View) {

        if(textViewCodigo.text!="-"){
            val intent = Intent(this, PreguntaActivity::class.java)

            val extras = Bundle()
            extras.putIntArray(EscogerGrupoActivity.Constants.PREGUNTAS, preguntas)
            extras.putInt(EscogerGrupoActivity.Constants.PUNTAJE, -1)
            intent.putExtras(extras)

            startActivity(intent)
        }
        else{
            val builder = AlertDialog.Builder(this@AnadirJugadoresActivity)
            builder.setMessage(R.string.label_code_missing )
                    .setTitle(R.string.label_informacion)
                    .setPositiveButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, id -> })
            val dialog = builder.create()
            dialog.show()
        }


    }

}


