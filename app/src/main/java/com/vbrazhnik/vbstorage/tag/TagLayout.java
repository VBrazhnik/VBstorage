package com.vbrazhnik.vbstorage.tag;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.vbrazhnik.vbstorage.R;
import com.vbrazhnik.vbstorage.entities.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagLayout extends LinearLayout {

    private Context context;
    private List<Tag> tags;
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private CompoundButton.OnCheckedChangeListener listener;

    private List<Tag> checked = null;

    public TagLayout(Context context) {
        super(context);
        this.context = context;
    }

    public TagLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void setChecked(List<Tag> checked) {
        this.checked = checked;
    }

    public boolean isChecked(long id)
    {
        if (this.checked != null)
        {
            for (Tag tag : this.checked) {
                if (id == tag.getId())
                    return true;
            }
        }
        return false;
    }

    public void update(List<Tag> tags)
    {
        this.removeAllViews();
        this.checkBoxes = new ArrayList<>();
        this.tags = tags;

        for (Tag tag: tags)
            addTag("#" + tag.getName(), isChecked(tag.getId()));

        int count = this.getChildCount();
        for (int i = 0; i < count; i++) {

            View child = this.getChildAt(i);

            LinearLayout.LayoutParams initParams = (LinearLayout.LayoutParams) child.getLayoutParams();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(initParams.width, initParams.height, initParams.weight);

            int left = 0, right = 20;

            if (i == 0)
                left = 10;
            if (i == count - 1)
                right = 10;

            params.setMargins(left, 0, right, 0);
            child.setLayoutParams(params);
        }
    }

    private void addTag(String text, boolean isChecked)
    {
        CheckBox cb = (CheckBox) LayoutInflater.from(context).inflate(R.layout.check_box_item, null);
        cb.setText(text);
        Typeface custom_font = Typeface.createFromAsset(context.getAssets(), "font/Rubik/Rubik-Medium.ttf");
        cb.setTypeface(custom_font);

        cb.setChecked(isChecked);
        cb.setOnCheckedChangeListener(listener);
        checkBoxes.add(cb);
        this.addView(cb);
    }

    public List<Tag> getCheckedTags()
    {
        List <Tag> checked = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isChecked())
                checked.add(tags.get(i));
        }
        return checked;
    }

    public void setListener(CompoundButton.OnCheckedChangeListener listener) {
        this.listener = listener;
    }
}
