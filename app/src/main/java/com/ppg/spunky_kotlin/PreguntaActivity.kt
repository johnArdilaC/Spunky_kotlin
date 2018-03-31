package com.ppg.spunky_kotlin

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.firebase.database.*
import com.ppg.spunky_kotlin.cardview.CheckableCardView
import kotlinx.android.synthetic.main.activity_pregunta.*

class PreguntaActivity : AppCompatActivity(), View.OnClickListener, SensorEventListener {


    private val mRootDB: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val preguntasReference: DatabaseReference = mRootDB.reference.child("Juegos").child("PreguntasTrivia")

    private var opciones:Array<CheckableCardView> = arrayOf()
    private var correcta=""

    private var preguntas:IntArray = intArrayOf()

    private var prefs: SharedPreferences?=null

    //Acelerómetro
    private var mSensorManager : SensorManager? = null
    private var mAccelerometer : Sensor?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregunta)

        //PASAR PREGUNTAS
        preguntas = intent.getIntArrayExtra(EscogerGrupoActivity.Constants.PREGUNTAS)

        opciones = arrayOf(card_a, card_b, card_c, card_d)
        opciones.forEach { it.setOnClickListener(this) }

        prefs = applicationContext.getSharedPreferences(EscogerGrupoActivity.Constants.PREFS_FILENAME, Context.MODE_PRIVATE)

        //initPreguntas(preguntas[0]) -> ¿Vale la pena hacerlo con conexion cuando se puede facilmente desde
        //shared preferences y no está haciento peticiones a la BD en cada activity?
        initPreguntaNoConn(preguntas[0])

        // get reference of the service
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
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
        val colorYellow=applicationContext.resources.getColor(R.color.colorYellow)


        if(thecard.tag==correcta){
            acerto=true
        }

        //Pintar correcta, se recorren las cuatro opciones, si el tag (a,b,c o d) coincide con el valor de correcta
        //se le cambia el fondo a verde, de lo contrario se cambia a rojo
        opciones.forEach {
            if(it.tag==correcta){
                it.setBackgroundColor(colorGreen)
                println("ENTRAAAAAAAA a green")
            }
            else if(it==thecard){
                println()
            }
            else
                it.setBackgroundColor(colorRed)
        }

        if(!acerto){thecard.setCardBackgroundColor(colorYellow)}

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


    /**
     * Busca la pregunta en la base de datos e inicializa los textos de la pregunta y las respuestas, con conexion
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

    /**
     * Sin conexion
     */
    private fun initPreguntaNoConn(idPregunta: Int){
        val default:Set<String> = hashSetOf("No se encontró la pregunta")

        var setPregunta: Set<String> =  prefs!!.getStringSet("Pregunta$idPregunta",default)

        var id:String
        var textCard:String

        setPregunta.forEach{
            val textList=it.split("=")
            if(textList.size>1){
                id=textList[0]
                textCard=textList[1]

                when(id){
                    "a" -> card_a.text=textCard
                    "b" -> card_b.text=textCard
                    "c" -> card_c.text=textCard
                    "correcta" -> correcta=textCard
                }
            }
            else
                LPregunta.text=textList[0]
        }
    }

    /**
     * -------------------Acelerómetro--------------
     */
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        if(p0!=null){
            println(p0.toString())
        }
        Log.e("on accuracy changed ", p1.toString())
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {

            val x = event.values[0]
            val y = event.values[1]

            var g: MutableList<Double> = mutableListOf()
            event.values.forEach { g.add(it.toDouble() )}

            val normOfg = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2])

            g[0] = g[0] / normOfg
            g[1] = g[1] / normOfg
            g[2] = g[2] / normOfg

            val inclination = Math.round(Math.toDegrees(Math.acos(g[2]))).toInt()

            if (inclination < 25 || inclination > 155)
            {
                if (inclination < -5) {
                 //   println("You tilt the device right NO FLAT $inclination $x  $y")
                }
                if (inclination > 5) {
                   // println("You tilt the device left NO FLAT $inclination $x  $y")

                }
            }
            else
            {
                val r = Math.round(Math.toDegrees(Math.atan2(g[0], g[1]))).toInt()

                if (r < -5) {
                   // println("You tilt the device right NO FLAT $r $x  $y")
                }
                if (r > 5) {
                  //  println("You tilt the device left NO FLAT $r $x  $y")

                }


            }


            if (Math.abs(x) > Math.abs(y)) {
            if (x < -5) {
               // println("You tilt the device right $x  $y")
            }
            if (x > 5) {
            //    println("You tilt the device left $x  $y")

            }
        } else {
            if (y < -5) {
              //  println("You tilt the device up $x  $y")

            }
            if (y > 100) {
                //println("You tilt the device down $x  $y")

            }
        }
        }
    }

        override fun onResume() {
            super.onResume()
            mSensorManager!!.registerListener(this, mAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL)
        }

        override fun onPause() {
            super.onPause()
            mSensorManager!!.unregisterListener(this)
        }



}
