package com.vbrazhnik.vbstorage;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Note> items;

    private OnItemClickListener clickListener;

    public NotesAdapter(Context context, List<Note> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        RecyclerView.ViewHolder holder = null;

        if (viewType == Type.TEXT.getCode())
        {
            view = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.item_text, parent, false);
            holder = new ItemText(view);
        }
        else if (viewType == Type.IMAGE.getCode()){
            view = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.item_image, parent, false);
            holder = new ItemImage(view);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Note item = items.get(position);

        if (item.getType() == Type.TEXT.getCode())
        {
            ItemText itemText = (ItemText) holder;
            itemText.title.setText(item.getTitle());
            itemText.text.setText(item.getText());
        }
        else if (item.getType() == Type.IMAGE.getCode())
        {
            ItemImage itemImage = (ItemImage) holder;
            itemImage.title.setText(item.getTitle());
            itemImage.image.setImageBitmap(null);

            if (item.getPhotoPath() != null) {
                File imgFile = new File(item.getPhotoPath());
                if (imgFile.exists())
                {
                    itemImage.image.setVisibility(View.VISIBLE);
                    Uri e = Uri.fromFile(imgFile);
                    Glide.with(context).
                            load(e)
                            .placeholder(R.color.transparent)
                            .error(R.drawable.ic_done_24dp)
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
        /*else if (item.getType() == Type.WEBPAGE.getCode())
        {

        }*/

    }

    @Override
    public int getItemViewType(int position) {
        if(items.get(position).getType() == Type.TEXT.getCode())
        {
            return Type.TEXT.getCode();
        }
        else if (items.get(position).getType() == Type.IMAGE.getCode())
        {
            return Type.IMAGE.getCode();
        }
        /*else if (items.get(position).getType() == Type.WEBPAGE.getCode())
        {
            return Type.WEBPAGE.getCode();
        }*/
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

        @BindView(R.id.item_title)
        TextView title;
        @BindView(R.id.item_text)
        TextView text;

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

        @BindView(R.id.item_title)
        TextView title;
        @BindView(R.id.item_image)
        ImageView image;

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
}
