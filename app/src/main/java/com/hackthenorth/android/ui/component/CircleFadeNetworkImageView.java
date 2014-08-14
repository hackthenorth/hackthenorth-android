package com.hackthenorth.android.ui.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

public class CircleFadeNetworkImageView extends NetworkImageView {

    public CircleFadeNetworkImageView(Context context) {
        this(context, null);
    }

    public CircleFadeNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleFadeNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);

        // TODO: Moez, put your circle cropping code here. Also, we can perform a fade-in
        // TODO: animation here as well.
    }
}
