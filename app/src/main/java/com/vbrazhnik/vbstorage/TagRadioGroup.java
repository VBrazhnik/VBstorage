package com.vbrazhnik.vbstorage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioGroup;

public class TagRadioGroup extends RadioGroup {

    public TagRadioGroup(Context context) {
        super(context);
    }

    public TagRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        updateBackground();
    }

    public void updateBackground() {
        int count = super.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            LayoutParams initParams = (LayoutParams) child.getLayoutParams();
            LayoutParams params = new LayoutParams(initParams.width, initParams.height, initParams.weight);

            params.setMargins(0, 0, 20, 0);

            child.setLayoutParams(params);
        }
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
    }

    /*@Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        //mCheckedChangeListener = listener;
    }*/

}