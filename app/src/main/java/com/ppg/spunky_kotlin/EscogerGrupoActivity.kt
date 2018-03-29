package com.ppg.spunky_kotlin

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IntegerRes
import android.support.v4.math.MathUtils
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View

import com.google.firebase.database.*
import com.ppg.spunky_kotlin.cardview.CheckableCardView
import kotlinx.android.synthetic.main.activity_escoger_grupo.*

class EscogerGrupoActivity : AppCompatActivity(),View.OnClickListener {

    // Write a message to the database
    private val grupos: MutableList<String> = mutableListOf()

    private val mRootDB: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val gruposReference: DatabaseReference = mRootDB.reference.child("Grupos")
    private val edadesReference: DatabaseReference = mRootDB.reference.child("Edades")

    var grupoSeleccionado:String = ""

    private var edadesSeleccionadas:Array<String> = arrayOf()
    private var preguntasTotales:MutableList<Integer> = mutableListOf()

    private var preguntasAptas:MutableList<Integer> = mutableListOf()



    private var groupView:Array<CheckableCardView> = arrayOf()
    private var ageView:Array<CheckableCardView> = arrayOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escoger_grupo)

        groupView = arrayOf(card_amigos,card_trabajo,card_desconocidos,card_familia)
        ageView = arrayOf(card_menores,card_jovenes,card_adultos,card_mayores)


        card_amigos.setOnClickListener(this)
        card_trabajo.setOnClickListener(this)
        card_familia.setOnClickListener(this)
        card_desconocidos.setOnClickListener(this)
        siguiente.setOnClickListener(this)



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
     * Cuando selecciona un grupo, deshabilita todos los demás
     */
    private fun unToggle(thecard: CheckableCardView)
    {
        if(thecard.isChecked){
        println("untoggle")
        disableElse(thecard)
        }
        else
            enableElse(thecard)

    }

    /**
     * Deshabilitar las botones que no sean el actual
     */
    private fun disableElse(thecard: CheckableCardView){
        println("Disabling all but " + thecard)
        groupView.forEach {
            if(it.id!=thecard.id){
                it.isSelected=false
                it.isChecked=false
                it.isEnabled=false
                println(" Disabled "+ it.toString())
            }

        }
    }

    /**
     * Habilitar las cards que no sean el actual
     */
    private fun enableElse(thecard: CheckableCardView){
        println("Enabling all but " + thecard)

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
            println("is checked")
            grupoSeleccionado=findCheckedGroup()

            if(ageView.any { it.isChecked }){

                edadesSeleccionadas = findCheckedAges()
                initGrupos()
            }

            //No hay edad seleccionada
            else{
                val builder = AlertDialog.Builder(this@EscogerGrupoActivity)
                builder.setMessage(R.string.label_edades_completar)
                        .setTitle(R.string.label_informacion)
                        .setPositiveButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, id -> })
                val dialog = builder.create()
                dialog.show()
            }


        }
        //Error porque no hay grupo seleccionado
        else{
            val builder = AlertDialog.Builder(this@EscogerGrupoActivity)
            builder.setMessage(R.string.label_completar)
                    .setTitle(R.string.label_informacion)
                    .setPositiveButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, id -> })
            val dialog = builder.create()
            dialog.show()
        }

    }

    private fun findCheckedGroup():String{
        var checkedGroup=""

        groupView.forEach {
            if(it.isChecked){
                checkedGroup=it.text
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



    //Get stuff from DB
    private fun initGrupos() {

        //Añadir preguntas compatibles con edad
        val edadesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                edadesSeleccionadas.forEach {


                    val hijo:DataSnapshot = dataSnapshot.child(it)
                    val idStringArray = hijo.child("preguntas").value.toString().replace("{","").replace("}","").split(",")

                    idStringArray.forEach {
                        preguntasTotales.add(Integer(Integer.valueOf(it))) }

                    println("preguntas totales"+preguntasTotales.toString())

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


                idStringArray.forEach {
                    preguntasTotales.add(Integer(Integer.valueOf(it)))
                }

                println("preguntas totales en grupo "+preguntasTotales.toString())

                preguntasAptas = preguntasTotales.distinct().toMutableList()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }

        gruposReference.addValueEventListener(gruposListener)

        println("total APTAS  "  + preguntasAptas)



    }
}
