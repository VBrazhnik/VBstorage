package com.vbrazhnik.vbstorage;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

public class StaggeredGridDecoration extends RecyclerView.ItemDecoration {

    private int margin;

    public StaggeredGridDecoration(int margin) {
        this.margin = margin;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int itemCount = state.getItemCount();
        final int itemPosition = parent.getChildAdapterPosition(view);
        int spanIndex = ((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();

        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }

        int left = margin;
        int top = margin / 2;
        int right = margin;
        int bottom = margin / 2;

        if (itemPosition == 0 || itemPosition == 1)
            top = margin;
        if (itemCount > 0 && itemPosition == itemCount - 1)
            bottom = margin;

        if (spanIndex == 0)
            right = margin / 2;
        else
            left = margin / 2;

        outRect.set(left, top, right, bottom);
    }
}
