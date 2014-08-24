package com.hackthenorth.android.ui.component;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RippleView extends FrameLayout {

    private static final String TAG = "RippleView";

    protected Set<Animator> animatorSet = Collections.newSetFromMap(
            new ConcurrentHashMap<Animator, Boolean>());

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
        setClickable(true);
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {

            int endRadius = (int)getEndRadius(event.getX(), event.getY());

            final Animator animator = new Animator(this, event.getX(), event.getY(), endRadius);
            int totalDuration = endRadius / 2;

            // Animate the alpha in quickly
            final ObjectAnimator fadeInAnimator = ObjectAnimator.ofInt(animator, "alpha", 0, 25);
            fadeInAnimator.setInterpolator(new AccelerateInterpolator());
            fadeInAnimator.setDuration(100);

            // Animate the circle to a set length
            final ObjectAnimator circleAnimator = ObjectAnimator.ofFloat(animator, "radius", 0, endRadius);
            circleAnimator.setInterpolator(new DecelerateInterpolator());
            circleAnimator.setDuration(totalDuration);

            // Fade out gently
            int delay = 150;
            final ObjectAnimator fadeOutAnimator = ObjectAnimator.ofInt(animator, "alpha", 25, 0);
            fadeOutAnimator.setInterpolator(new LinearInterpolator());
            fadeOutAnimator.setStartDelay(delay);
            fadeOutAnimator.setDuration(totalDuration - delay);

            // Add the animation to the map, and remove it after the duration amount
            animatorSet.add(animator);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animatorSet.remove(animator);
                }
            }, totalDuration);

            fadeInAnimator.start();
            circleAnimator.start();
            fadeOutAnimator.start();
        }

        return true;
    }

    private float getEndRadius(float x, float y) {
        // Return length of the longest line from the given (x,y) point to a point
        // on the bounds of the view.

        float upperLeft = x * x + y * y;
        float upperRight = (x - getWidth()) * (x - getWidth()) + y * y;
        float lowerLeft = x * x + (y - getHeight()) * (y - getHeight());
        float lowerRight = upperRight + lowerLeft - upperLeft;

        float max = Math.max(Math.max(Math.max(upperLeft, upperRight), lowerLeft),
                lowerRight);

        return (float)Math.sqrt(max);
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        super.onDraw(canvas);

        for (Animator a : animatorSet) {
            canvas.drawCircle(a.x, a.y, a.radius, a.paint);
            canvas.drawRect(0, 0, getWidth(), getHeight(), a.paint);
        }
    }

    protected static class Animator {

        public float x;
        public float y;
        public float radius;
        public Paint paint;

        private WeakReference<View> mView;

        public Animator(View view, float downX, float downY, float radius) {
            mView = new WeakReference<View>(view);
            x = downX;
            y = downY;
            this.radius = radius;
            paint = new Paint();
        }

        public void setRadius(final float radius) {
            this.radius = radius;
            if (mView.get() != null) {
                mView.get().invalidate();
            }
        }

        public void setAlpha(final int alpha) {
            paint.setAlpha(alpha);
            if (mView.get() != null) {
                mView.get().invalidate();
            }
        }
    }
}
