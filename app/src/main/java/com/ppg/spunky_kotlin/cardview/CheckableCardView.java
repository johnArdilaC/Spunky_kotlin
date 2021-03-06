package com.ppg.spunky_kotlin.cardview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ppg.spunky_kotlin.R;

import java.lang.reflect.Field;


/**
 * Created by mariapc on 28/03/18.
 */

public class CheckableCardView  extends CardView implements Checkable {

    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked,
    };

    private boolean isChecked;
    private String text;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CheckableCardView(Context context) {
        super(context);
        init(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CheckableCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CheckableCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.checkable_card_view, this, true);

        setClickable(true);
        setChecked(false);



        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CheckableCardView, 0, 0);
            try {
                text = ta.getString(R.styleable.CheckableCardView_card_text);
                TextView itemText = findViewById(R.id.text);

                if (text != null) {
                    itemText.setText(text);
                }

                String image = ta.getString(R.styleable.CheckableCardView_card_image);
                ImageView itemImage = findViewById(R.id.image);

                if (image != null) {
                    String txtimage=image.split("/")[2];
                    String txt2 = txtimage.split(".png")[0];
                    int id = getResources().getIdentifier(txt2,"drawable", getContext().getPackageName());

                    itemImage.setImageResource(id);
                }


                String backgroundColor = ta.getString(R.styleable.CheckableCardView_card_background);

                if(backgroundColor!=null){
                    Class res = R.drawable.class;
                    Field field = res.getField(backgroundColor);

                    ColorStateList myColorStateList = new ColorStateList(
                            new int[][]{
                                    new int[]{android.R.attr.state_checked},
                                    new int[]{}
                            },
                            new int[] {
                                    getContext().getResources().getColor(R.color.colorAccent),
                                    getResources().getColor(field.getInt(null))

                            }
                    );

                    setCardBackgroundColor(myColorStateList);
                }
                else
                    setCardBackgroundColor(ContextCompat.getColorStateList(getContext(), R.color.selector_card_view_colors));


            } catch (Exception e) {
                Log.e("MyTag", "Failure to get drawable id.", e);
            }finally {
                ta.recycle();
            }
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    public String getText(){
        return  text;
    }

    public void setText(String text){
        TextView itemText = findViewById(R.id.text);
        itemText.setText(text);
    }

    @Override
    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!this.isChecked);
    }
}
