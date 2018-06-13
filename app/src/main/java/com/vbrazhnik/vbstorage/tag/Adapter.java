package com.vbrazhnik.vbstorage.tag;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.vbrazhnik.vbstorage.entities.Tag;
import com.vbrazhnik.vbstorage.events.TagCreatedEvent;
import com.vbrazhnik.vbstorage.events.TagDeletedEvent;
import com.vbrazhnik.vbstorage.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;

import java.util.List;

class Adapter extends RecyclerView.Adapter {

	private static final int VIEW_TYPE_NEW_TAG = 0;
	private static final int VIEW_TYPE_EDIT_TAG = 1;

	private List<Tag> tags;
	private OpenCloseable lastOpenedItem;

	private EditTagsActivity appCompatActivity;

	Adapter(EditTagsActivity appCompatActivity) {
		this.appCompatActivity = appCompatActivity;
	}

	EditTagsActivity getAppCompatActivity() {
		return appCompatActivity;
	}

	@NonNull
	@Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
		if (viewType == VIEW_TYPE_NEW_TAG){
			return new NewTagViewHolder(
					LayoutInflater.from(parent.getContext()).inflate(R.layout.view_new_tag, parent, false), this);
		} else if (viewType == VIEW_TYPE_EDIT_TAG) {
			return new EditTagViewHolder(
					LayoutInflater.from(parent.getContext()).inflate(R.layout.view_edit_tag, parent, false), this);
		}
		return null;
	}

	@Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position){
		if (holder instanceof EditTagViewHolder){
			position--;
			EditTagViewHolder editTagViewHolder = (EditTagViewHolder) holder;
			editTagViewHolder.setTag(tags.get(position));
		}
	}

	@Override
    public int getItemViewType(int position){
		if (position == 0) return VIEW_TYPE_NEW_TAG;
		else return VIEW_TYPE_EDIT_TAG;
	}

	@Override
    public int getItemCount(){
		return 1 + (tags == null ? 0 : tags.size());
	}

	void loadFromDatabase(){
		QueryBuilder<Tag> queryBuilder = appCompatActivity.getTagDao().queryBuilder();
		tags = queryBuilder.list();
		notifyDataSetChanged();
	}

	void registerEventBus(){
		EventBus.getDefault().register(this);
	}

	void unregisterEventBus(){
		EventBus.getDefault().unregister(this);
	}

	@Subscribe public void onTagDeletedEvent(TagDeletedEvent tagDeletedEvent){
		int index = tags.indexOf(tagDeletedEvent.getTag());
		if (index == -1) return;
		tags.remove(index);
		notifyItemRemoved(index + 1);
	}

	@Subscribe public void onTagCreatedEvent(TagCreatedEvent tagCreatedEvent){
		if (tags == null) tags = new ArrayList<>();
		tags.add(0, tagCreatedEvent.getTag());
		notifyItemInserted(1);
	}

	OpenCloseable getLastOpened(){
		return lastOpenedItem;
	}

	void setLastOpened(OpenCloseable lastOpenedItem){
		this.lastOpenedItem = lastOpenedItem;
	}
}
