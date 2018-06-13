package com.vbrazhnik.vbstorage;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

public class SettingsDialog extends DialogFragment {

    @BindView(R.id.settings_tag)        ImageView tagElement;
    @BindView(R.id.settings_columns)    ImageView columnsElement;
    @BindView(R.id.settings_trash)      ImageView trashElement;
    @BindView(R.id.settings_theme)      ImageView themeElement;

    private boolean type;

    SharedPreferences sharedPreferences;

    final String TWO_COLUMNS = "two_columns";

    View.OnClickListener viewListener;
    View.OnClickListener hashtagListener;
    View.OnClickListener trashListener;

    @NonNull
    @Override
    public BasePicker onCreateDialog(Bundle savedInstanceState) {
        return new BasePicker(getContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.setting_fragment, container, false);
        ButterKnife.bind(this, view);
        columnsElement.setOnClickListener(viewListener);
        tagElement.setOnClickListener(hashtagListener);
        trashElement.setOnClickListener(trashListener);
        sharedPreferences = getActivity().getSharedPreferences("VBstorage", MODE_PRIVATE);;
        type = sharedPreferences.getBoolean(TWO_COLUMNS, true);
        if (!type)
            columnsElement.setImageDrawable(getResources().getDrawable(R.drawable.settings_columns_1));
        else
            columnsElement.setImageDrawable(getResources().getDrawable(R.drawable.settings_columns_2));

        return view;
    }

    public void setViewListener(View.OnClickListener viewListener) {
        this.viewListener = viewListener;
    }

    public void setHashtagListener(View.OnClickListener hashtagListener) {
        this.hashtagListener = hashtagListener;
    }

    public void setTrashListener(View.OnClickListener trashListener) {
        this.trashListener = trashListener;
    }

    private class BasePicker extends Dialog {

        public BasePicker(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setCancelable(true);
            setCanceledOnTouchOutside(true);
            getWindow().setGravity(Gravity.BOTTOM);
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

    }
}