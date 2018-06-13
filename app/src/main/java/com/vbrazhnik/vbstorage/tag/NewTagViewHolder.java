package com.vbrazhnik.vbstorage.tag;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vbrazhnik.vbstorage.Util;
import com.vbrazhnik.vbstorage.entities.Tag;
import com.vbrazhnik.vbstorage.events.TagCreatedEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.vbrazhnik.vbstorage.R;

public class NewTagViewHolder extends RecyclerView.ViewHolder implements OpenCloseable{

	private final Adapter adapter;

	@BindView(R.id.left_button)			ImageButton leftButton;
	@BindView(R.id.tag_name_text_new)	EditText tagName;
	@BindView(R.id.done_button)			ImageButton doneButton;

	NewTagViewHolder(final View itemView, Adapter adapter){
		super(itemView);
		ButterKnife.bind(this, itemView);

		this.adapter = adapter;
		tagName.setOnFocusChangeListener(new View.OnFocusChangeListener(){
			@Override
            public void onFocusChange(View v, boolean hasFocus){
				if (hasFocus){
					open();
				}
			}
		});
		tagName.setOnEditorActionListener(new TextView.OnEditorActionListener(){
			@Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
				if (actionId == EditorInfo.IME_ACTION_DONE){
					add();
					close();
					return true;
				}
				return false;
			}
		});
	}

	public NewTagViewHolder(final View itemView){
		this(itemView, null);
	}

	@OnClick(R.id.left_button) void clickLeftButton(View view){
		if (isOpen()){
			close();
		}else{
			tagName.requestFocus();
			Util.showSoftKeyboard(tagName);
			open();
		}
	}

	@OnClick(R.id.done_button) void clickDoneButton(View view){
		add();
		close();
	}

	@Override
    public void open(){
		doneButton.setVisibility(View.VISIBLE);
		leftButton.setImageResource(R.drawable.tag_close);
		//leftButton.setAlpha(0.5f);
		if (adapter != null){
			if (adapter.getLastOpened() != null)
				adapter.getLastOpened().close();
			adapter.setLastOpened(this);
		}
	}

	@Override
    public boolean isOpen(){
		return tagName.hasFocus();
	}

	@Override
    public void close(){
		Util.hideSoftKeyboard(itemView);
		doneButton.setVisibility(View.GONE);
		tagName.setText("");
		tagName.clearFocus();
		leftButton.setAlpha(1f);
		leftButton.setImageResource(R.drawable.tag_plus);
		if (adapter!=null && adapter.getLastOpened() == this) adapter.setLastOpened(null);
	}

	private void add(){
		if (TextUtils.isEmpty(tagName.getText())) return;
		Tag tag = new Tag();
		tag.setName(tagName.getText().toString().trim());
		adapter.getAppCompatActivity().getTagDao().insert(tag);
		EventBus.getDefault().post(new TagCreatedEvent(tag));
	}
}
