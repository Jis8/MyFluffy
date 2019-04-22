package com.example.jisoo.myfluffy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;

public class RadioGridTableLayout extends TableLayout implements View.OnClickListener {


    private RadioButton activeRadioButton;

    public RadioGridTableLayout(Context context) {
        super(context);
    }

    public RadioGridTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onClick(View v) {
        final RadioButton rb = (RadioButton) v;
        if( activeRadioButton != null){
            activeRadioButton.setChecked(false);
        }
        rb.setChecked(true);
        activeRadioButton = rb;
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        setChildrenOnClickListener((TableRow)child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setChildrenOnClickListener((TableRow)child);
    }

    private void setChildrenOnClickListener(TableRow tr){
        final int c = tr.getChildCount();
        for(int i=0; i<3; i++){
            final View v = tr.getChildAt(i);
            if( v instanceof RadioButton){
                v.setOnClickListener(this);
            }
        }
    }

    public int getCheckedRadioButtonId(){
        if( activeRadioButton != null){
            return activeRadioButton.getId();
        }
        return -1;
    }

}


