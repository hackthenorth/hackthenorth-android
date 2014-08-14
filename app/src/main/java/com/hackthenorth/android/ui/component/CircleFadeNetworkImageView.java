package com.hackthenorth.android.ui.component;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class CircleFadeNetworkImageView extends NetworkImageView {
    private final String TAG = "CircleFadeNetworkImageView";

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
        // TODO: animation here as well.
        if (bitmap == null) {
            super.setImageBitmap(bitmap);
            return;
        }

        Bitmap sbmp;
        int radius = getWidth();

        if (bitmap.getWidth() != radius || bitmap.getHeight() != radius) {
            float smallest = Math.min(bitmap.getWidth(), bitmap.getHeight());
            float factor = smallest / radius;
            sbmp = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() / factor), (int) (bitmap.getHeight() / factor), false);
        } else {
            sbmp = bitmap;
        }

        Bitmap output = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, radius, radius);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(radius / 2, radius / 2, radius / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        super.setImageBitmap(output);
    }
}
