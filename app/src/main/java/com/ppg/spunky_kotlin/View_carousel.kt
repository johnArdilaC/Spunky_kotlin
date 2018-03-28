package com.ppg.spunky_kotlin

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast


class View_carousel : PagerAdapter {

    var con: Context
    var path: IntArray
    lateinit var inflator: LayoutInflater

    constructor(con: Context, path: IntArray) : super() {
        this.con = con
        this.path = path
    }


    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return path.size
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        var img: ImageView
        inflator = con.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var rv: View = inflator.inflate(R.layout.swipe_fragment,container,false)
        img=rv.findViewById<ImageView>(R.id.img) as ImageView
        img.setImageResource(path[position])
        container!!.addView(rv)

        img.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {

                Toast.makeText(con,"you click image:"+(position+1), Toast.LENGTH_LONG).show()
            }
        })
        return rv
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {

    }
}