package com.vbrazhnik.vbstorage.tag;

import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
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
import com.vbrazhnik.vbstorage.events.TagDeletedEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.vbrazhnik.vbstorage.R;

class EditTagViewHolder extends RecyclerView.ViewHolder implements OpenCloseable{

	private final Adapter adapter;
	private Tag tag;

	@BindView(R.id.left_button)			ImageButton leftButton;
	@BindView(R.id.tag_name_text) 		EditText tagName;
	@BindView(R.id.right_button)		ImageButton rightButton;
	@BindView(R.id.tag_name_til)		TextInputLayout tagNameTIL;

	EditTagViewHolder(final View itemView, Adapter adapter){
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
					apply();
					close();
					return true;
				}
				return false;
			}
		});
	}

	@OnClick(R.id.left_button) void clickLeftButton(View view){
		if (isOpen()){
			close();
			delete();
		}
	}

	@OnClick(R.id.right_button) void clickRightButton(View view){
		if (isOpen()){
			apply();
			close();
		}else{
			open();
		}
	}

	@Override
    public void open(){
		leftButton.setImageResource(R.drawable.tag_delete);
		rightButton.setImageResource(R.drawable.tag_done);
		if (adapter.getLastOpened() != null)
			adapter.getLastOpened().close();
		adapter.setLastOpened(this);
	}

	@Override
    public boolean isOpen(){
		return tagName.hasFocus();
	}

	@Override
    public void close(){
		Util.hideSoftKeyboard(itemView);
		leftButton.setImageResource(R.drawable.tag_tag);
		tagName.setText(tag.getName());
		tagName.clearFocus();
		rightButton.setImageResource(R.color.white);
		if (adapter.getLastOpened() == this) adapter.setLastOpened(null);
	}

	private void apply(){
		if (TextUtils.isEmpty(tagName.getText())){
			tagNameTIL.setError(adapter.getAppCompatActivity().getString(R.string.enter_tag_name));
			return;
		}
		tag.setName(tagName.getText().toString());
		tag.update();
	}

	private void delete(){
		new AlertDialog.Builder(itemView.getContext(), R.style.DialogTheme)
				.setCancelable(true)
				.setTitle(R.string.delete_tag)
				.setMessage(adapter.getAppCompatActivity().getString(R.string.tag) + " '" + tag.getName() + "' " + adapter.getAppCompatActivity().getString(R.string.items_will_be_safe))
				.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
					@Override
                    public void onClick(DialogInterface dialog, int which){
						dialog.dismiss();
						tag.delete();
						EventBus.getDefault().post(new TagDeletedEvent(tag));
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
					@Override
                    public void onClick(DialogInterface dialog, int which){
						dialog.dismiss();
					}
				})
				.show();
	}

	public void setTag(Tag tag){
		this.tag = tag;
		tagName.setText(tag.getName());
		close();
	}

}
