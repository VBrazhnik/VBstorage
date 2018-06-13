package com.vbrazhnik.vbstorage.viewer;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vbrazhnik.vbstorage.database.DirectoryHelper;
import com.vbrazhnik.vbstorage.entities.Item;
import com.vbrazhnik.vbstorage.R;
import com.vbrazhnik.vbstorage.entities.Type;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Item> items;

    private OnItemClickListener clickListener;

    ItemsAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        RecyclerView.ViewHolder holder;

        if (viewType == Type.IMAGE.getCode()) {
            view = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.card_item_image, parent, false);
            holder = new ItemImage(view);
        }
        else if (viewType == Type.WEB_PAGE.getCode()) {
            view = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.card_item_webpage, parent, false);
            holder = new ItemWEBpage(view);
        }
        else if (viewType == Type.AUDIO.getCode()) {
            view = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.card_item_audio, parent, false);
            holder = new ItemAudio(view);
        }
        else {
            view = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.card_item_text, parent, false);
            holder = new ItemText(view);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Item item = items.get(position);

        if (item.getType() == Type.TEXT.getCode())
        {
            ItemText itemText = (ItemText) holder;

            if (item.getTitle().isEmpty())
                itemText.title.setVisibility(View.GONE);
            else
                itemText.title.setText(item.getTitle());

            itemText.text.setText(item.getText());
            if (item.getTitle().isEmpty() && item.getText().length() <= 10)
            {
                itemText.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, (item.getText().length() <= 5) ? 35 : 25);
                itemText.text.setTypeface(Typeface.createFromAsset(context.getAssets(), "font/Rubik/Rubik-Light.ttf"));
            }
            else
            {
                itemText.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                itemText.text.setTypeface(Typeface.createFromAsset(context.getAssets(), "font/Rubik/Rubik-Regular.ttf"));
            }
        }
        else if (item.getType() == Type.IMAGE.getCode())
        {
            ItemImage itemImage = (ItemImage) holder;
            itemImage.title.setText(item.getTitle());
            if (item.getTitle().isEmpty())
            {
                itemImage.title.setVisibility(View.GONE);
            }
            if (!item.getText().isEmpty())
            {
                itemImage.textLabel.setVisibility(View.VISIBLE);
            }
            itemImage.image.setImageBitmap(null);

            if (item.getImagePath() != null) {
                File imgFile = new File(DirectoryHelper.getImagesDirectory() + item.getImagePath());
                if (imgFile.exists())
                {
                    itemImage.image.setVisibility(View.VISIBLE);
                    Uri e = Uri.fromFile(imgFile);
                    Glide.with(context).
                            load(e)
                            .asBitmap()
                            .placeholder(R.color.transparent)
                            .error(R.color.white)
                            .into(itemImage.image);
                } else {
                    itemImage.image.setVisibility(View.GONE);
                }
            }
            else
            {
                itemImage.image.setVisibility(View.GONE);
            }
        }
        else if (item.getType() == Type.WEB_PAGE.getCode())
        {
            ItemWEBpage itemWEBpage = (ItemWEBpage) holder;
            itemWEBpage.title.setText(item.getTitle());
            if (item.getTitle().isEmpty())
            {
                itemWEBpage.title.setVisibility(View.GONE);
            }
            itemWEBpage.image.setImageBitmap(null);

            if (item.getImagePath() != null) {
                File imgFile = new File(DirectoryHelper.getImagesDirectory() + item.getImagePath());
                if (imgFile.exists())
                {
                    itemWEBpage.image.setVisibility(View.VISIBLE);
                    Uri e = Uri.fromFile(imgFile);
                    Glide.with(context).
                            load(e)
                            .asBitmap()
                            .placeholder(R.color.transparent)
                            .error(R.color.white)
                            .into(itemWEBpage.image);
                } else {
                    itemWEBpage.image.setVisibility(View.GONE);
                }
            }
            else
            {
                itemWEBpage.image.setVisibility(View.GONE);
            }
        }
        else if (item.getType() == Type.AUDIO.getCode())
        {
            ItemAudio itemAudio = (ItemAudio) holder;
            if (item.getTitle().isEmpty())
                itemAudio.title.setVisibility(View.INVISIBLE);
            else
                itemAudio.title.setText(item.getTitle());
            itemAudio.text.setText(item.getText());
        }
    }

    @Override
    public int getItemViewType(int position) {

        if(items.get(position).getType() == Type.TEXT.getCode())
            return Type.TEXT.getCode();
        else if (items.get(position).getType() == Type.IMAGE.getCode())
            return Type.IMAGE.getCode();
        else if (items.get(position).getType() == Type.WEB_PAGE.getCode())
            return Type.WEB_PAGE.getCode();
        else if (items.get(position).getType() == Type.AUDIO.getCode())
            return Type.AUDIO.getCode();
        return 0;

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    class ItemText extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.item_title)  TextView title;
        @BindView(R.id.item_text)   TextView text;

        ItemText(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class ItemImage extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.item_title)  TextView title;
        @BindView(R.id.item_image)  ImageView image;
        @BindView(R.id.text_label)  ImageView textLabel;

        ItemImage(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class ItemWEBpage extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.item_title)  TextView title;
        @BindView(R.id.item_image)  ImageView image;

        ItemWEBpage(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class ItemAudio extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.item_title)  TextView title;
        @BindView(R.id.item_text)   TextView text;

        ItemAudio(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }
}
