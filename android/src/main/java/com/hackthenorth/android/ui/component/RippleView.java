package com.hackthenorth.android.ui.component;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Region;
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
import com.hackthenorth.android.util.Units;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RippleView extends FrameLayout {

    private static final String TAG = "RippleView";

    // -1 to indicate that the user hasn't called setRadius or setDuration.
    public static final int RADIUS_DEFAULT = -1;
    public static final int DURATION_DEFAULT = -1;

    protected Set<Animator> animatorSet = Collections.newSetFromMap(new ConcurrentHashMap<Animator, Boolean>());

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

            // Calculate the radius and the total duration.
            int endRadius = calculateRadius(event);
            int totalDuration = calculateDuration(event, endRadius);

            // Create an Animator object to hold the information for this animation.
            final Animator animator = new Animator(this, event.getX(), event.getY(), endRadius);

            // Animate the alpha in quickly
            final ObjectAnimator fadeInAnimator = ObjectAnimator.ofInt(animator, "alpha", 0, 25);
            fadeInAnimator.setInterpolator(new AccelerateInterpolator());
            fadeInAnimator.setDuration(100);

            // Animate the circle to a set length
            final ObjectAnimator circleAnimator = ObjectAnimator.ofFloat(animator, "radius", 0, endRadius);
            circleAnimator.setInterpolator(new DecelerateInterpolator());
            circleAnimator.setDuration(totalDuration);

            // Fade out gently
            final ObjectAnimator fadeOutAnimator = ObjectAnimator.ofInt(animator, "alpha", 25, 0);
            fadeOutAnimator.setInterpolator(new LinearInterpolator());
            fadeOutAnimator.setStartDelay(totalDuration / 2);
            fadeOutAnimator.setDuration(totalDuration / 2);

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

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        super.onDraw(canvas);
        drawRipple(canvas);
    }

    protected void drawRipple(@NonNull final Canvas canvas) {

        canvas.clipRect(getPaddingLeft(), getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom(),
                Region.Op.REPLACE);

        for (Animator a : animatorSet) {
            canvas.drawCircle(a.x, a.y, a.radius, a.paint);
            canvas.drawRect(0, 0, getWidth(), getHeight(), a.paint);
        }
    }

    /**
     * Calculates the terminal radius of the ripple in pixels. Override this method to
     * provide a different radius.
     * @param ev The ACTION_UP event that causes the ripple.
     * @return The terminal radius of the ripple in pixels.
     */
    protected int calculateRadius(MotionEvent ev) {
        // Calculate the radius as a function of the length of the touch.
        long touchDuration = ev.getEventTime() - ev.getDownTime();

        // Normalize touch duration: 250ms <= dur <= 2000ms
        touchDuration = touchDuration < 250 ? 250 :
                (touchDuration > 2000 ? 2000 : touchDuration);

        long longTouch = 2000;
        float ratio = (float)Math.sqrt((float) longTouch / (float) touchDuration);
        return Units.dpToPx(getContext(), (int) (80 * ratio));
    }

    /**
     * Calculates the duration of the ripple in milliseconds.
     * @param ev The ACTION_UP event that caused the ripple.
     * @param radius The terminal radius of the ripple as calculated in calculateRadius().
     *               It is provided out of convenience since the duration is often a
     *               function of the size of the ripple.
     * @return The length in milliseconds of the ripple.
     */
    protected int calculateDuration(MotionEvent ev, int radius) {
        return radius < 500 ? 500 : radius;
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
