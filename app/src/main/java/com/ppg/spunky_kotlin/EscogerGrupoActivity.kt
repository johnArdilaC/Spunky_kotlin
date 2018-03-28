package com.ppg.spunky_kotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_escoger_grupo.*

class EscogerGrupoActivity : AppCompatActivity() {

    // Write a message to the database
    private val grupos: MutableList<String> = mutableListOf()

    private val mRootDB: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val gruposReference: DatabaseReference = mRootDB.reference.child("Grupos")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escoger_grupo)
        initGrupos()
        Log.d("Da reference", gruposReference.toString())

        Log.d("Da groups", grupos.toString())
        //RAmigos.text = grupos[1]
    }

    //Get stuff from DB
    private fun initGrupos() {
        val gruposListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("Da groups", dataSnapshot.toString())

                dataSnapshot.children.forEach {
                    grupos.add(it.value.toString())}
                Log.d("Da groups", grupos.toString())

            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }

        gruposReference.addListenerForSingleValueEvent(gruposListener)
    }
}
