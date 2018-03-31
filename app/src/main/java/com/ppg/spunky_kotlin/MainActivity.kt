package com.ppg.spunky_kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Sign in via google
     */
    private var mGoogleApiClient: GoogleApiClient? = null
    private val REQUEST_CODE_SIGN_IN = 1234

    //Este se saca de la consola de firebase
    private val WEB_CLIENT_ID = "155855972528-bud5be7j2uo4lhdjh19s64higd1mvlnb.apps.googleusercontent.com"
    private var mAuth: FirebaseAuth? = null

    private val mRootDB: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val gruposReference: DatabaseReference = mRootDB.reference.child("Grupos")
    private val edadesReference: DatabaseReference = mRootDB.reference.child("Edades")
    private val preguntasReference: DatabaseReference = mRootDB.reference.child("Juegos").child("PreguntasTrivia")


    object Constants{
        val PREGUNTAS_BD = "com.ppg.spunky.prefs_DB"
    }

    private var prefsBD: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_sign_in.setOnClickListener(this)
        button1.setOnClickListener(this)
        btn_sign_out.setOnClickListener(this)
        button2.setOnClickListener(this)
        button3.setOnClickListener(this)

        prefsBD = applicationContext.getSharedPreferences(Constants.PREGUNTAS_BD, Context.MODE_PRIVATE)

        val editor = prefsBD!!.edit()

        guardarEdades(editor)
        guardarGrupos(editor)
        guardarPreguntas(editor)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClick(v: View?) {
        val i = v!!.id

        when (i) {
            R.id.btn_sign_in -> signIn()
            R.id.button1 -> escogerGrupo()
            R.id.btn_sign_out -> signOut()
            R.id.button2 -> crearJuego()
            R.id.button3 -> unirse()
        }
    }

    private fun escogerGrupo()
    {
        val intent = Intent(this, EscogerGrupoActivity::class.java)
        startActivity(intent)
    }

    private fun crearJuego()
    {
        val intent = Intent(this, EnConstruccionActivity::class.java)
        startActivity(intent)
    }

    private fun unirse()
    {
        val intent = Intent(this, UnirseActivity::class.java)
        startActivity(intent)
    }

    /**
     * Logica para hacer sign in con google
     */

    private fun signIn() {

        val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent();
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            Log.e("Sign in", result.toString())

            if (result.isSuccess) {
                // successful -> authenticate with Firebase
                val account = result.signInAccount

                firebaseAuthWithGoogle(account!!)

            } else {
                // failed -> update UI
                Log.e("Sign in", "it failed1" +  result.status)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.e("Sign in", "firebaseAuthWithGoogle():" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        val user = mAuth!!.currentUser
                        Log.e("Sign in", "it worked")
                        updateUI(user)

                    } else {
                        // Sign in fails
                        Log.e("Sign in", "it failed2 " + task.result)

                    }
                }
    }

    private fun signOut() {
        // sign out Firebase
        mAuth!!.signOut()

        // sign out Google
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback { updateUI(null) }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            btn_sign_in.visibility = View.GONE
            btn_sign_out.visibility = View.VISIBLE

        } else {

            btn_sign_in.visibility = View.VISIBLE
            btn_sign_out.visibility = View.GONE

        }
    }

    /**
     * Traer preguntas, grupos y edades y guardarlos en shared preferences
     */
    private fun guardarEdades(editor:SharedPreferences.Editor){
        //Añadir preguntas compatibles con edad
        val edadesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    //Obtener el tipo de edad
                    val edad:String =it.key
                    val preguntas = it.child("preguntas").value.toString()
                    editor.putString(edad, preguntas)
                    editor.commit()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }
        edadesReference.addValueEventListener(edadesListener)
    }

    private fun guardarGrupos(editor:SharedPreferences.Editor){
        //Añadir preguntas compatibles con grupos
        val gruposListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {



                dataSnapshot.children.forEach {
                    //Obtener el tipo de grupo
                    val grupo:String =it.key
                    val preguntas = it.child("preguntas").value.toString()
                    editor.putString(grupo, preguntas)
                    editor.commit()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }
        gruposReference.addValueEventListener(gruposListener)

    }

    private fun guardarPreguntas(editor:SharedPreferences.Editor){
        var setPregunta:Set<String> = hashSetOf()

        //Añadir preguntas compatibles con edad
        val preguntasListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for(i in 1 until 11){
                    val pregunta:DataSnapshot = dataSnapshot.child("Pregunta$i")
                    val txtPregunta = pregunta.child("txtPregunta").value.toString()

                    setPregunta += txtPregunta

                    //Guardar el resultado como hash para hacer mas facil el acceso a los datos
                    val opciones = pregunta.child("opciones").value as HashMap<String,Any>

                    for (i in opciones){
                        setPregunta+=i.toString()
                    }

                    editor.putStringSet("Pregunta$i", setPregunta)
                    editor.commit()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }
        preguntasReference.addValueEventListener(preguntasListener)
    }






}
