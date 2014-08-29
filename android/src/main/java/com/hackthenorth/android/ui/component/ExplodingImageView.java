package com.hackthenorth.android.ui.component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class ExplodingImageView extends ImageView {
    private static final String TAG = "ExplodingImageView";

    private static final int ACTION_NONE = 0;
    private static final int ACTION_APPEAR = 1;
    private static final int ACTION_DISAPPEAR = 2;

    private int currentAction = ACTION_NONE;
    private int nextAction = ACTION_NONE;

    private AnimationListener currentListener;
    private AnimationListener nextListener;

    private final int DURATION = 150;

    public ExplodingImageView(Context context) {
        this(context, null);
    }
    public ExplodingImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ExplodingImageView(Context context, AttributeSet attrs, int idk) {
        super(context, attrs, idk);
    }

    public void setExplodingVisibility(int visibility) {
        setExplodingVisibility(visibility, null);
    }

    public void setExplodingVisibility(int visibility, AnimationListener l) {
        if (visibility == View.VISIBLE && getVisibility() != View.VISIBLE) {
            if (currentAction == ACTION_NONE) {
                // Appear!
                currentAction = ACTION_APPEAR;
                currentListener = l;
                super.setVisibility(visibility);
                appear();
            } else if (currentAction == ACTION_APPEAR && nextAction == ACTION_DISAPPEAR) {
                // Cancel the pending disappear action
                nextAction = ACTION_NONE;
                nextListener = null;
            } else if (currentAction == ACTION_DISAPPEAR) {
                // Oh, rats! We have an appear action but we already started disappearing.
                // Queue up the appear action.
                nextAction = ACTION_APPEAR;
                nextListener = l;
            }
        } else if (visibility != View.VISIBLE && getVisibility() == View.VISIBLE) {
            if (currentAction == ACTION_NONE && getVisibility() == View.VISIBLE) {
                // Disappear!
                currentAction = ACTION_DISAPPEAR;
                currentListener = l;
                disappear();
            } else if (currentAction == ACTION_DISAPPEAR && nextAction == ACTION_DISAPPEAR) {
                // Cancel the pending appear action
                nextAction = ACTION_NONE;
                nextListener = null;
            } else if (currentAction == ACTION_APPEAR) {
                // We're currently appearing, so we have to disappear when we finish.
                nextAction = ACTION_DISAPPEAR;
                nextListener = l;
            }
        }
    }

    public void appear() {
        Interpolator interpolator = new DecelerateInterpolator();

        AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
        alpha.setInterpolator(interpolator);
        alpha.setDuration(DURATION);

        ScaleAnimation scale = new ScaleAnimation(0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(DURATION);
        scale.setInterpolator(interpolator);
        scale.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {  }
            @Override public void onAnimationRepeat(Animation animation) {  }

            @Override
            public void onAnimationEnd(Animation animation) {
                currentAction = ACTION_NONE;
                currentListener = null;
                if (nextAction == ACTION_DISAPPEAR) {
                    currentAction = nextAction;
                    currentListener = nextListener;
                    nextAction = ACTION_NONE;
                    nextListener = null;
                    disappear();
                }
            }
        });

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(alpha);
        set.addAnimation(scale);
        set.setAnimationListener(currentListener);

        startAnimation(set);
    }

    public void disappear() {
        Interpolator interpolator = new AccelerateInterpolator();

        AlphaAnimation alpha = new AlphaAnimation(1f, 0f);
        alpha.setInterpolator(interpolator);
        alpha.setDuration(DURATION);

        ScaleAnimation scale = new ScaleAnimation(1f, 0f, 1f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(DURATION);
        scale.setInterpolator(interpolator);
        scale.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {  }
            @Override public void onAnimationRepeat(Animation animation) {  }

            @Override
            public void onAnimationEnd(Animation animation) {
                currentAction = ACTION_NONE;
                currentListener = null;
                if (nextAction == ACTION_APPEAR) {
                    currentAction = nextAction;
                    nextAction = ACTION_NONE;
                    currentListener = nextListener;
                    nextListener = null;
                    appear();
                } else {
                    setVisibility(View.GONE);
                }
            }
        });

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(alpha);
        set.addAnimation(scale);
        set.setAnimationListener(currentListener);

        startAnimation(set);
    }
}
