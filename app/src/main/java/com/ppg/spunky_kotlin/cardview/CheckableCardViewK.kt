package com.ppg.spunky_kotlin.cardview

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Checkable
import android.widget.ImageView
import android.widget.TextView
import com.ppg.spunky_kotlin.R

/**
 * Created by mariapc on 29/03/18.
 */
class CheckableCardViewK: CardView,Checkable   {


    private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)

    private var text: String? = null

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context):super(context)
    {
        init(null)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet):super(context, attrs) {
        init(attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):super(context, attrs, defStyleAttr)
    {
        init(attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun init(attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.checkable_card_view, this, true)

        //setClickable(true)
        isClickable=true


        //setChecked(false)
        isChecked=false


        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.CheckableCardView, 0, 0)
            try {
                text = ta.getString(R.styleable.CheckableCardView_card_text)
                val itemText = findViewById<TextView>(R.id.text)

                if (text != null) {
                    itemText.setText(text)
                }

                val image = ta.getString(R.styleable.CheckableCardView_card_image)
                val itemImage = findViewById<ImageView>(R.id.image)

                if (image != null) {
                    val txtimage = image!!.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[2]
                    val txt2 = txtimage.split(".png".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]
                    val id = resources.getIdentifier(txt2, "drawable", context.packageName)

                    itemImage.setImageResource(id)
                }


                val backgroundColor = ta.getString(R.styleable.CheckableCardView_card_background)

                if (backgroundColor != null) {
                    val res = R.drawable::class.java
                    val field = res.getField(backgroundColor)

                    val myColorStateList = ColorStateList(
                            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                            intArrayOf(context.resources.getColor(R.color.colorAccent), resources.getColor(field.getInt(null)))
                    )
                    //setCardBackgroundColor(myColorStateList)
                    cardBackgroundColor=myColorStateList
                } else
                    //setCardBackgroundColor(ContextCompat.getColorStateList(getContext(), R.color.selector_card_view_colors))
                    cardBackgroundColor=ContextCompat.getColorStateList(context, R.color.selector_card_view_colors)


            } catch (e: Exception) {
                Log.e("MyTag", "Failure to get drawable id.", e)
            } finally {
                ta.recycle()
            }
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) {
            View.mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }
        return drawableState
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }

    /*fun getText(): String? {
        return text
    }

    fun setText(text: String) {
        val itemText = findViewById<TextView>(R.id.text)
        itemText.setText(text)
    }*/


    override fun setChecked(checked: Boolean) {
        this.isChecked = checked
    }

    /**
     * al implementarse causa stackovlerflow
     */
    override fun isChecked(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toggle() {
        isChecked = !this.isChecked
    }

}
