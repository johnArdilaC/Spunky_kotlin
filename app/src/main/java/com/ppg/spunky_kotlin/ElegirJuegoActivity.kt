package com.ppg.spunky_kotlin

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_elegir_juego.*

class ElegirJuegoActivity : AppCompatActivity() {

    private var myViewPagerAdapter: MyViewPagerAdapter? = null
    private var dots: Array<TextView?> = arrayOf()
    private var layouts: IntArray =  intArrayOf()
    private var btnsJugar:Array<Button> = arrayOf()

    private var preguntas: IntArray = intArrayOf()

    //Atributo que indica si hay conexión
    private var isConnected: Boolean = false
    private val REQUEST_ENABLE_BT = 1
    private var changeReceiver: NetworkChangeReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elegir_juego)

        preguntas = intent.getIntArrayExtra(EscogerGrupoActivity.Constants.PREGUNTAS)

        btnsJugar = arrayOf(btn_jugarCharadas,btn_jugarTrivia,btn_jugarVerdad)

        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        layouts = intArrayOf(R.layout.charadas, R.layout.trivia, R.layout.verdad)

        addBottomDots(0)

        myViewPagerAdapter = MyViewPagerAdapter()
        view_pager.setAdapter(myViewPagerAdapter)
        view_pager.addOnPageChangeListener(viewPagerPageChangeListener)

        btnsJugar.forEach {
            it.setOnClickListener {
                // checking for last page
                // if last page home screen will be launched
                val current = getItem(+1)
                verificarConexion()
               // if (current < layouts.size && isConnected) {
                if (current < layouts.size) {
                    launchNextActivity()
                }
                else if (!isConnected) {
                    val builder = AlertDialog.Builder(this@ElegirJuegoActivity)
                    builder.setMessage(R.string.label_conexion)
                            .setTitle(R.string.label_informacion)
                            .setPositiveButton(R.string.button_bluetooth, DialogInterface.OnClickListener { dialog, id ->
                                val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                                if (mBluetoothAdapter == null) {
                                    // Device does not support Bluetooth
                                    crearMensaje(R.string.label_bluetooth)
                                } else if (!mBluetoothAdapter.isEnabled) {
                                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                                }
                            })
                            .setNeutralButton(R.string.button_volver, DialogInterface.OnClickListener { dialog, id -> })
                    val dialog = builder.create()
                    dialog.show()
                }
                else {

                }
            }

        }

    }

    private fun addBottomDots(currentPage: Int) {
        //initialize array with sizeTODO
        dots= arrayOfNulls(layouts.size)

        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)

        layoutDots.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226;")
            dots[i]?.textSize = 35f
            dots[i]?.setTextColor(colorsInactive[currentPage])
            layoutDots.addView(dots[i])
        }

        if (dots.size > 0)
            dots[currentPage]?.setTextColor(colorsActive[currentPage])
    }

    private fun getItem(i: Int): Int {
        return view_pager.currentItem + i
    }

    private fun crearMensaje(mensaje: Int) {
        val builder = AlertDialog.Builder(this@ElegirJuegoActivity)
        builder.setMessage(mensaje)
                .setTitle(R.string.label_informacion)
                .setNeutralButton(R.string.button_volver, DialogInterface.OnClickListener { dialog, id -> })
        val dialog = builder.create()
        dialog.show()
    }


    /**
     * View pager adapter
     */
    inner class MyViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val view = layoutInflater!!.inflate(layouts[position], container, false)
            container.addView(view)

            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }


        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }

    //  viewpager change listener
    internal var viewPagerPageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageSelected(position: Int) {
            addBottomDots(position)

            when (position) {
                0 -> enableVisibility(btn_jugarCharadas)
                1 -> enableVisibility(btn_jugarTrivia)
                2 -> enableVisibility(btn_jugarVerdad)
            }
        }

        fun enableVisibility(btn:Button){
            btnsJugar.forEach {
                if(it.equals(btn)) it.visibility=View.VISIBLE
                else    it.visibility=View.GONE
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {

        }

        override fun onPageScrollStateChanged(arg0: Int) {

        }
    }

    private fun launchNextActivity() {
        val intent = Intent(this, PreguntaActivity::class.java)
        intent.putExtra(EscogerGrupoActivity.Constants.PREGUNTAS, preguntas)
        startActivity(intent)
        finish()
    }

    /**
     * Conectividad
     */


    private fun irABluetoothActivity() {
        startActivity(Intent(this, JugadoresBluetoothActivity::class.java))
        finish()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            REQUEST_ENABLE_BT -> if (resultCode == Activity.RESULT_OK) {
                println("ACEPTO")
                irABluetoothActivity()
            } else {
                // User did not enable Bluetooth or an error occurred
                println("NO ACEPTO")
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun verificarConexion() {

        isConnected = changeReceiver?.getConnectivityStatusString(applicationContext) == NetworkChangeReceiver.NETWORK_STATUS_NOT_CONNECTED

    }
}
