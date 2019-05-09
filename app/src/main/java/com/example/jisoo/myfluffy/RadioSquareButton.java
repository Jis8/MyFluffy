package com.example.jisoo.myfluffy;

import android.content.Context;
import android.util.AttributeSet;

public class RadioSquareButton extends android.support.v7.widget.AppCompatRadioButton {

    public RadioSquareButton(Context context) {
        super(context);
    }

    public RadioSquareButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadioSquareButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        width = Math.min(width, height);
        height = width;

        setMeasuredDimension(width, height);
    }
}
