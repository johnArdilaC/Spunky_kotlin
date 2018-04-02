package com.ppg.spunky_kotlin


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.bluetooth.BluetoothAdapter
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.INotificationSideChannel

import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View

import com.google.firebase.database.*
import com.ppg.spunky_kotlin.cardview.CheckableCardView
import kotlinx.android.synthetic.main.activity_escoger_grupo.*

class EscogerGrupoActivity : AppCompatActivity(),View.OnClickListener {


    object Constants{
        val PREGUNTAS = "com.ppg.spunky.PREGUNTAS"
        val PUNTAJE= "com.ppg.spunky.PUNTAJE"
        val PREFS_FILENAME = "com.ppg.spunky.prefs"
        val APODO = "com.ppg.spunky.APODO"
    }

    //Firebase vars
    private val mRootDB: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val gruposReference: DatabaseReference = mRootDB.reference.child("Grupos")
    private val edadesReference: DatabaseReference = mRootDB.reference.child("Edades")
    private val preguntasReference: DatabaseReference = mRootDB.reference.child("Juegos").child("PreguntasTrivia")


    private var grupoSeleccionado:String = ""
    private var edadesSeleccionadas:Array<String> = arrayOf()
    private var preguntasTotales:MutableList<Integer> = mutableListOf()
    private var preguntasAptas:MutableList<Integer> = mutableListOf()

    private var groupView:Array<CheckableCardView> = arrayOf()
    private var ageView:Array<CheckableCardView> = arrayOf()

    private var prefs: SharedPreferences? = null
    private var prefsBD: SharedPreferences? = null

    private var isConnected: Boolean = false
    private var changeReceiver: NetworkChangeReceiver = NetworkChangeReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escoger_grupo)

        groupView = arrayOf(card_amigos,card_trabajo,card_desconocidos,card_familia)
        ageView = arrayOf(card_menores,card_jovenes,card_adultos,card_mayores)

        groupView.forEach { it.setOnClickListener(this) }

        siguiente.setOnClickListener(this)

        //Shared preferences
        prefs = applicationContext.getSharedPreferences(Constants.PREFS_FILENAME, Context.MODE_PRIVATE)
        prefsBD = applicationContext.getSharedPreferences(MainActivity.Constants.PREGUNTAS_BD, Context.MODE_PRIVATE)


    }

    /**
     * Funcion para elegir que hacer en base al boton seleccionado
     */
    override fun onClick(v: View?) {
        val i = v!!.id
        System.out.println("onclick")

        when (i) {
            R.id.card_amigos -> unToggle(card_amigos)
            R.id.card_trabajo -> unToggle(card_trabajo)
            R.id.card_desconocidos -> unToggle(card_desconocidos)
            R.id.card_familia -> unToggle(card_familia)
            R.id.siguiente -> oprimirSiguiente()
        }
    }

    /**
     * Cuando selecciona un(una card) grupo, deshabilita todos los demás
     */
    private fun unToggle(thecard: CheckableCardView)
    {
        if(thecard.isChecked){
        disableElse(thecard)
        }
        else
            enableElse(thecard)
    }

    /**
     * Deshabilitar las cards que no sean la actual
     */
    private fun disableElse(thecard: CheckableCardView){
        groupView.forEach {
            if(it.id!=thecard.id){
                it.isSelected=false
                it.isChecked=false
                it.isEnabled=false
            }
        }
    }

    /**
     * Habilitar las cards que no sean el actual
     */
    private fun enableElse(thecard: CheckableCardView){

        groupView.forEach {
            if(it.id!=thecard.id){
                it.isEnabled=true
            }
        }
    }

    //------------Logica para seleccion de grupos,edades y preguntas-------------

    /**
     * Al oprimir siguiente se validan que se haya seleccionado 1 grupo y al menos una edad
     */

    private fun oprimirSiguiente(){

        //Una card seleccionada
        if(groupView.any { it.isChecked }){
            grupoSeleccionado=findCheckedGroup()

            if(ageView.any { it.isChecked }){
                edadesSeleccionadas = findCheckedAges()

                /*
                if(activeNetwork!=null){
                    if(activeNetwork.type.equals(ConnectivityManager.TYPE_WIFI)){
                        initGrupos()
                    }
                    if(activeNetwork.type.equals(ConnectivityManager.TYPE_MOBILE)){
                        initGrupos()
                    }
                    if(activeNetwork.type.equals(ConnectivityManager.TYPE_MOBILE_DUN)){
                        initGrupos()
                    }
                }
                else{
                    launchBlueActivity()
                }
                */

                //Verificar si hay conexion para inicializar grupos o preguntas
                isConnected = !(changeReceiver.getConnectivityStatusString(applicationContext) == NetworkChangeReceiver.NETWORK_STATUS_NOT_CONNECTED)

                if(isConnected){
                    initGrupos()
                }
                else{
                    initGruposNoConn()
                }
            }

            //No hay edad seleccionada
            else{
                showAlertDialog(R.string.label_edades_completar,R.string.label_informacion)
            }
        }
        //No hay grupo seleccionado
        else{
            showAlertDialog(R.string.label_completar,R.string.label_informacion)
        }

    }
    private fun showAlertDialog(msg:Int,title:Int) {
        val builder = AlertDialog.Builder(this@EscogerGrupoActivity)
        builder.setMessage(msg)
                .setTitle(title)
                .setPositiveButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, id -> })
        val dialog = builder.create()
        dialog.show()

    }

    private fun findCheckedGroup():String{
        var checkedGroup=""

        groupView.forEach {
            if(it.isChecked){
                checkedGroup=it.text
                return checkedGroup
            }
        }
        return checkedGroup
    }

    private fun findCheckedAges():Array<String>{
        var checkedAges:Array<String> = arrayOf()

        ageView.forEach {
            if(it.isChecked){
                checkedAges+=it.text
            }
        }
        return checkedAges
    }

    /**
     *-------------------- Inicializar cosas con conexion------------------
     */
    /**
     * Inicializa las preguntas dado el grupo y las edades escogidas
     */
    private fun initGrupos() {

        //Añadir preguntas compatibles con edad
        val edadesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                edadesSeleccionadas.forEach {

                    val hijo:DataSnapshot = dataSnapshot.child(it)
                    val idStringArray = hijo.child("preguntas").value.toString().replace("{","").replace("}","").split(",")

                    idStringArray.forEach { preguntasTotales.add(Integer(Integer.valueOf(it))) }

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }

        edadesReference.addValueEventListener(edadesListener)

        //Añadir preguntas compatibles con grupo

        val gruposListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                println("el grupo" + grupoSeleccionado)
                val hijo:DataSnapshot = dataSnapshot.child(grupoSeleccionado)

                val idStringArray = hijo.child("preguntas").value.toString().replace("{","").replace("}","").split(",")

                idStringArray.forEach {preguntasTotales.add(Integer(Integer.valueOf(it)))}

                println("preguntas totales en grupo "+preguntasTotales.toString())

                preguntasAptas = preguntasTotales.distinct().toMutableList()
                preguntasAptas.shuffle()
                val preguntasFinales = IntArray(preguntasAptas.size)
                for (j in preguntasAptas.indices) {

                    preguntasFinales[j] = preguntasAptas[j].toInt()
                }
                println("total APTAS  $preguntasAptas")
                println("total Finales $preguntasFinales")



                launchNextActivity(preguntasFinales)

                //Guardar preguntas en shared preferences
                saveQuestionsSP(preguntasFinales)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }
        gruposReference.addValueEventListener(gruposListener)
    }


    /*
    fun connectivity(): String {
        var message = "not_exist"
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        //val isConnected = activeNetwork.isConnectedOrConnecting

            when (activeNetwork.type) {
                ConnectivityManager.TYPE_WIFI -> message = "exist"
                ConnectivityManager.TYPE_MOBILE -> message = "exist"
                ConnectivityManager.TYPE_MOBILE_DUN -> message = "exist"

        }


        return message
    }
    */


    //private fun launchNextActivity(preguntas: IntArray?)


    /**
     * Busca la pregunta en la base de datos e inicializa los textos de la pregunta y las respuestas
     */
    private fun initPreguntas(idPregunta:Int){

        var setPregunta:Set<String> = hashSetOf()
        val query:Query = preguntasReference.orderByChild("id").equalTo(idPregunta.toDouble())


        val editor = prefs!!.edit()

        editor.clear()
        editor.putString(Constants.APODO, "HOST")
        editor.commit()

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val valuePregunta:DataSnapshot = dataSnapshot.child("Pregunta$idPregunta")
                val txtPregunta = valuePregunta.child("txtPregunta").value.toString()

                setPregunta += txtPregunta

                //Guardar el resultado como hash para hacer mas facil el acceso a los datos
                val opciones = valuePregunta.child("opciones").value as HashMap<String,Any>

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

    /**
     *-------------------- Inicializar cosas sin conexion------------------
     */


    /**
     * Inicializa grupos o preguntas dado si hay conexion o no
     */
    private fun saveQuestionsSP(preguntas: IntArray){
        if(isConnected)     preguntas.forEach { initPreguntas(it) }
        else    preguntas.forEach { initPreguntasNoConn(it) }

        launchNextActivity(preguntas)
    }

    /**
     * Trae grupos del shared preferences
     */
    private fun initGruposNoConn(){

        val default = "No se encontró nada"

        edadesSeleccionadas.forEach {

            var test: String =  prefsBD!!.getString(it,default)
            println("test 1 anadir $test")

            val idStringArray = test.replace("{","").replace("}","").split(",")

            idStringArray.forEach { preguntasTotales.add(Integer(Integer.valueOf(it))) }

        }

        //Traer las preguntas segun grupo
        var test: String =  prefsBD!!.getString(grupoSeleccionado,default)
        println("test 1 anadir $test")

        val idStringArray = test.replace("{","").replace("}","").split(",")

        idStringArray.forEach { preguntasTotales.add(Integer(Integer.valueOf(it))) }

        //Definir preguntas aptas
        preguntasAptas = preguntasTotales.distinct().toMutableList()
        val preguntasFinales = IntArray(preguntasAptas.size)
        for (j in preguntasAptas.indices) {

            preguntasFinales[j] = preguntasAptas[j].toInt()
        }
        println("total APTAS  NO CONN $preguntasAptas")
        println("total Finales NO CON $preguntasFinales")

        //Guardar preguntas en shared preferences
        saveQuestionsSP(preguntasFinales)

    }

    private fun initPreguntasNoConn(idPregunta:Int){

        val default:Set<String> = hashSetOf("No se encontró la pregunta")

        val editor = prefs!!.edit()

        editor.clear()

        val setPregunta: Set<String> =  prefsBD!!.getStringSet("Pregunta$idPregunta",default)

        Log.e("Pregunta $idPregunta no conn", setPregunta.toString() )

        editor.putStringSet("Pregunta$idPregunta", setPregunta)
        editor.putString(Constants.APODO, "HOST")
        editor.commit()
    }


    private fun launchNextActivity(preguntas: IntArray)
    {
        val intent = Intent(this, ElegirJuegoActivity::class.java)
        intent.putExtra(Constants.PREGUNTAS,preguntas)
        startActivity(intent)
    }

    private fun launchBlueActivity()
    {
        val intent = Intent(this, AnadirJugadoresBlueActivity::class.java)
        startActivity(intent)
    }

}
