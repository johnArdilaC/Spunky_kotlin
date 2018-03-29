package com.ppg.spunky_kotlin

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.firebase.database.*
import com.ppg.spunky_kotlin.cardview.CheckableCardView
import kotlinx.android.synthetic.main.activity_pregunta.*

class PreguntaActivity : AppCompatActivity(), View.OnClickListener {

    private val mRootDB: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val preguntasReference: DatabaseReference = mRootDB.reference.child("Juegos").child("PreguntasTrivia")

    private var opciones:Array<CheckableCardView> = arrayOf()
    private var correcta=""

    private var preguntas:IntArray = intArrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregunta)

        //PASAR PREGUNTAS
        preguntas = intent.getIntArrayExtra(EscogerGrupoActivity.Constants.PREGUNTAS)

        opciones = arrayOf(card_a,card_b,card_c,card_d)
        opciones.forEach { it.setOnClickListener(this) }
        initPreguntas(preguntas[0])
    }

    /**
     * Busca la pregunta en la base de datos e inicializa los textos de la pregunta y las respuestas
     */
    private fun initPreguntas(idPregunta:Int){

        val query:Query = preguntasReference.orderByChild("id").equalTo(idPregunta.toDouble())

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val valuePregunta:DataSnapshot = dataSnapshot.child("Pregunta$idPregunta")
                val txtPregunta = valuePregunta.child("txtPregunta").value.toString()

                LPregunta.text=txtPregunta

                //Guardar el resultado como hash para hacer mas facil el acceso a los datos
                val opciones = valuePregunta.child("opciones").value as HashMap<String,Any>

                card_a.text=opciones["a"].toString()
                card_b.text=opciones["b"].toString()
                card_c.text=opciones["c"].toString()
                card_d.text=opciones["d"].toString()
                correcta=opciones["correcta"].toString()

                println("opciones "+opciones.values)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        })
    }

    override fun onClick(v: View?) {
        val i = v!!.id

        when (i) {
            R.id.card_a -> validar(card_a)
            R.id.card_b -> validar(card_b)
            R.id.card_c -> validar(card_c)
            R.id.card_d -> validar(card_d)
        }
    }

    /**
     * Valida la respuesta. La pinta de rojo si es incorrecta, pinta la correcta de verde
     */
    private fun validar(thecard: CheckableCardView){
        var acerto=false

        val colorGreen=applicationContext.resources.getColor(R.color.colorGreen)
        val colorRed=applicationContext.resources.getColor(R.color.colorRed)

        if(thecard.tag==correcta){
            acerto=true
        }
        //Pintar correcta, se recorren las cuatro opciones, si el tag (a,b,c o d) coincide con el valor de correcta
        //se le cambia el fondo a verde, de lo contrario se cambia a rojo
        opciones.forEach {
            if(it.tag==correcta){it.setBackgroundColor(colorGreen)}
            else it.setBackgroundColor(colorRed)
        }

        //Mensaje al jugador dependiendo si es correcta o no
        launchNextQuestion(acerto)
    }

    /**
     * Muestra el mensaje al jugador en forma de alert dialog y dependiendo si su respuesta fue correcta o no
     */
    private fun launchNextQuestion(acerto: Boolean) {

        if (acerto) {
            val builder = AlertDialog.Builder(this@PreguntaActivity)
            builder.setMessage(R.string.label_puntos)
                    .setTitle(R.string.label_correcto)
                    .setPositiveButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, id -> pasarPreguntas() })
            val dialog = builder.create()
            dialog.show()
        } else {
            val builder = AlertDialog.Builder(this@PreguntaActivity)
            builder.setMessage(R.string.label_Mpuntos)
                    .setTitle(R.string.label_incorrecto)
                    .setPositiveButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, id -> pasarPreguntas() })
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun pasarPreguntas() {
        val preguntasSiguientes = IntArray(preguntas.size - 1)
        if (preguntas.size > 1) {
            for (i in 1 until preguntas.size) {
                preguntasSiguientes[i - 1] = preguntas[i]
            }
            val intent = Intent(this, PreguntaActivity::class.java)
            intent.putExtra(EscogerGrupoActivity.Constants.PREGUNTAS, preguntasSiguientes)
            startActivity(intent)
            finish()
        }
    }

}
