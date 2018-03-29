package com.vbrazhnik.vbstorage;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class LinearDecoration  extends RecyclerView.ItemDecoration {

    private int margin;

    public LinearDecoration(int margin) {
        this.margin = margin;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int itemCount = state.getItemCount();

        final int itemPosition = parent.getChildAdapterPosition(view);

        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }

        int left = margin;
        int top = margin / 2;
        int right = margin;
        int bottom = margin / 2;

        if (itemPosition == 0)
            top = margin;
        if (itemCount > 0 && itemPosition == itemCount - 1)
            bottom = margin;

        outRect.set(left, top, right, bottom);
    }
}
