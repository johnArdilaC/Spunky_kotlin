package com.ppg.spunky_kotlin

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_unirse.*

class UnirseActivity : AppCompatActivity() {
    private var mFirebaseDatabase: FirebaseDatabase =FirebaseDatabase.getInstance()
    private var mDatabaseReferenceConexiones: DatabaseReference = mFirebaseDatabase.reference.child("Conexiones")
    private var android_id: String?=null
    private var preguntas: String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        android_id = Settings.Secure.getString(this.contentResolver,
                Settings.Secure.ANDROID_ID)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unirse)

        mDatabaseReferenceConexiones.keepSynced(true)
    }

    fun obtenerConexiones(view: View) {
        //Obtener código del juego
        val codigo = editTextUnirse
        val texto = codigo.text.toString()
        //Obtener apodo del jugador
        val apodo = editTextApodo
        val apodotxt = apodo.text.toString()

        //Listener base de datos
        val listenerConexiones = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.hasChild(texto)) {
                    if (preguntas == null) {
                        mDatabaseReferenceConexiones.child(texto).child(android_id!!).setValue(apodotxt)
                        preguntas = dataSnapshot.child(texto).child(AnadirJugadoresActivity.PREGUNTAS).value!!.toString()
                        println("PREGUNTAS " + preguntas!!)
                        procesarPreguntas()
                    }
                }
                else {
                    showAlertDialog(R.string.label_id_error,R.string.title_error)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }



        mDatabaseReferenceConexiones!!.addValueEventListener(listenerConexiones)

    }

    private fun showAlertDialog(msg:Int,title:Int){
        val builder = AlertDialog.Builder(this@UnirseActivity)
        builder.setMessage(msg)
                .setTitle(title)
                .setPositiveButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, id -> })
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Pasa de "{5,3,6}" a un array de ints
     */
    private fun procesarPreguntas() {
        val preg = preguntas!!.replace("{", "").replace("}", "")
        val preguntasSeparadas = preg.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val idsPreguntas = IntArray(preguntasSeparadas.size)

        for (j in preguntasSeparadas.indices) {
            idsPreguntas[j] = Integer.parseInt(preguntasSeparadas[j])

            val intent = Intent(this, PreguntaActivity::class.java)
            intent.putExtra(EscogerGrupoActivity.Constants.PREGUNTAS, idsPreguntas)
            startActivity(intent)
        }

    }
}
