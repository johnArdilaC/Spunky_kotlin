package com.ppg.spunky_kotlin

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast


class Carousel : AppCompatActivity() {

    lateinit var mPager: ViewPager
    var path: IntArray = intArrayOf(R.drawable.trivia,R.drawable.verdad, R.drawable.charada)
    //var btn = findViewById<Button>(R.id.btnjugar) as Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carousel)
        mPager=findViewById<ViewPager>(R.id.pager) as ViewPager
        var adapter: PagerAdapter = View_carousel(this,path)
        mPager.adapter=adapter

        mPager.addOnAdapterChangeListener(object : ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {


            override fun onAdapterChanged(viewPager: ViewPager, oldAdapter: PagerAdapter?, newAdapter: PagerAdapter?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                Toast.makeText(this@Carousel, path[position], Toast.LENGTH_LONG).show()
            }
        })

    }
}
