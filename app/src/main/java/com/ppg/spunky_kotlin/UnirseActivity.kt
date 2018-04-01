package com.ppg.spunky_kotlin

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_unirse.*

class UnirseActivity : AppCompatActivity() {
    private var mRootDB: FirebaseDatabase =FirebaseDatabase.getInstance()
    private var conexionesReference: DatabaseReference = mRootDB.reference.child("Conexiones")
    private var android_id: String?=null
    private var preguntas: String?=null
    private val preguntasReference: DatabaseReference = mRootDB.reference.child("Juegos").child("PreguntasTrivia")

    private var isConnected: Boolean = false
    private var changeReceiver: NetworkChangeReceiver = NetworkChangeReceiver()

    private var prefs: SharedPreferences? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        android_id = Settings.Secure.getString(this.contentResolver,
                Settings.Secure.ANDROID_ID)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unirse)

        window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        prefs = applicationContext.getSharedPreferences(EscogerGrupoActivity.Constants.PREFS_FILENAME, Context.MODE_PRIVATE)


        conexionesReference.keepSynced(true)
    }

    fun obtenerConexiones(view: View) {
        //Obtener código del juego
        val codigo = editTextUnirse
        val texto = codigo.text.toString()
        //Obtener apodo del jugador
        val apodo = editTextApodo
        val apodotxt = apodo.text.toString()

        if(apodotxt.trim()==""){
            showAlertDialog(R.string.label_nickname_error,R.string.title_error)
        }
        else {

            //Listener base de datos
            val listenerConexiones = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.hasChild(texto)) {
                        if (preguntas == null) {
                            conexionesReference.child(texto).child(android_id!!).setValue(apodotxt)
                            preguntas = dataSnapshot.child(texto).child(AnadirJugadoresActivity.PREGUNTAS).value!!.toString()
                            procesarPreguntas()
                        }
                    } else {
                        showAlertDialog(R.string.label_id_error, R.string.title_error)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            }
            conexionesReference!!.addValueEventListener(listenerConexiones)
        }

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
        Log.e("preguntas", preg)
        val preguntasSeparadas = preg.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val idsPreguntas = IntArray(preguntasSeparadas.size)

        for (i in idsPreguntas.indices) {

            idsPreguntas[i] = preguntasSeparadas[i].toInt()
        }

        idsPreguntas.forEach {
            Log.e("ids preguntas", it.toString())
            initPreguntas(it) }

        for (j in preguntasSeparadas.indices) {
            idsPreguntas[j] = Integer.parseInt(preguntasSeparadas[j])

            val intent = Intent(this, PreguntaActivity::class.java)
            val extras = Bundle()
            extras.putIntArray(EscogerGrupoActivity.Constants.PREGUNTAS, idsPreguntas)
            extras.putInt(EscogerGrupoActivity.Constants.PUNTAJE, -1)
            intent.putExtras(extras)
            startActivity(intent)
        }

    }

    /**
     * Busca la pregunta en la base de datos e inicializa los textos de la pregunta y las respuestas
     */
    private fun initPreguntas(idPregunta:Int){

        var setPregunta:Set<String> = hashSetOf()
        val query:Query = preguntasReference.orderByChild("id").equalTo(idPregunta.toDouble())

        Log.e("el iddd","$idPregunta")

        val editor = prefs!!.edit()

        editor.clear()
        editor.commit()

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val valuePregunta:DataSnapshot? = dataSnapshot.child("Pregunta$idPregunta")
                val txtPregunta = valuePregunta!!.child("txtPregunta").value.toString()

                setPregunta += txtPregunta

                //Guardar el resultado como hash para hacer mas facil el acceso a los datos
                val opciones = valuePregunta!!.child("opciones").value as HashMap<String,Any>

                for (i in opciones){
                    setPregunta+=i.toString()
                }

                println("Set pregunta $idPregunta $setPregunta")

                editor.putStringSet("Pregunta$idPregunta", setPregunta)
                editor.commit()

            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        })
    }


}
