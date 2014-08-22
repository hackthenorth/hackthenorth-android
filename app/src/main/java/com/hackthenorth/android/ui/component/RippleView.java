package com.hackthenorth.android.ui.component;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import com.hackthenorth.android.util.Units;

public class RippleView extends FrameLayout {

    private float mDownX;
    private float mDownY;

    private float mRadius;

    private Paint mPaint;

    public RippleView(Context context) {
        super(context);
        init();
    }

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint = new Paint();
        setAlpha(0);
        setClickable(true);
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            mDownX = event.getX();
            mDownY = event.getY();

            int endRadius = Units.dpToPx(getContext(), 128);

            // Animate the alpha in quickly
            final ObjectAnimator fadeInAnimator = ObjectAnimator.ofInt(this, "alpha", 0, 25);
            fadeInAnimator.setInterpolator(new AccelerateInterpolator());
            fadeInAnimator.setDuration(100);

            // Animate the circle to a set length
            final ObjectAnimator circleAnimator = ObjectAnimator.ofFloat(this, "radius", 0, endRadius);
            circleAnimator.setInterpolator(new DecelerateInterpolator());
            circleAnimator.setDuration(200);

            // Fade out gently
            final ObjectAnimator fadeOutAnimator = ObjectAnimator.ofInt(this, "alpha", 25, 0);
            fadeOutAnimator.setInterpolator(new LinearInterpolator());
            fadeOutAnimator.setStartDelay(150);
            fadeOutAnimator.setDuration(300);

            // Run the animations in a separate thread so that they aren't
            // made choppy by performing other actions, ex. parsing data to
            // be passed into an intent to add to calendar and then showing
            // a dialog.
            new Runnable() {
                @Override
                public void run() {
                    fadeInAnimator.start();
                    circleAnimator.start();
                    fadeOutAnimator.start();
                }
            }.run();
        }

        super.onTouchEvent(event);
        return true;
    }

    public void setRadius(final float radius) {
        mRadius = radius;
        invalidate();
    }

    public void setAlpha(final int alpha) {
        mPaint.setAlpha(alpha);
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(mDownX, mDownY, mRadius, mPaint);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
    }
}
