package com.vbrazhnik.vbstorage.tag;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import se.emilsjolander.intentbuilder.IntentBuilder;
import com.vbrazhnik.vbstorage.R;
import com.vbrazhnik.vbstorage.VBstorage;
import com.vbrazhnik.vbstorage.entities.TagDao;

@IntentBuilder
public class EditTagsActivity extends AppCompatActivity {

	@BindView(R.id.toolbar)			Toolbar toolbar;
	@BindView(R.id.recycler_view)	RecyclerView recyclerView;

	Adapter adapter;
	private TagDao tagDao;

	@Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tags);

		tagDao = ((VBstorage)getApplication()).getDaoSession().getTagDao();

		EditTagsActivityIntentBuilder.inject(getIntent(), this);
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);
		toolbar.setTitle(R.string.tags);
		toolbar.setNavigationIcon(R.drawable.toolbar_close);
		toolbar.setNavigationOnClickListener(new View.OnClickListener(){
			@Override
            public void onClick(View v){
				onBackPressed();
			}
		});
		LinearLayoutManager llm = new LinearLayoutManager(this);
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(llm);
		adapter = new Adapter(this);
		recyclerView.setAdapter(adapter);
		adapter.loadFromDatabase();
	}

	@Override
    protected void onStart(){
		super.onStart();
		adapter.registerEventBus();
	}

	@Override
    protected void onStop(){
		super.onStop();
		adapter.unregisterEventBus();
	}

	public TagDao getTagDao() {
		return tagDao;
	}
}
