package com.hackthenorth.android.ui;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import com.devspark.robototextview.widget.RobotoTextView;

/**
 * Custom textview that will properly measure bounds for wrapping text
 */
public class TextView extends RobotoTextView {
    private final String TAG = "TextView";

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int specModeW = MeasureSpec.getMode(widthMeasureSpec);
        if (specModeW != MeasureSpec.EXACTLY) {
            Layout layout = getLayout();
            int linesCount = layout.getLineCount();
            if (linesCount > 1) {
                float textRealMaxWidth = 0;
                for (int n = 0; n < linesCount; ++n) {
                    textRealMaxWidth = Math.max(textRealMaxWidth, layout.getLineWidth(n));
                }

                int w = Math.round(textRealMaxWidth);
                if (w < getMeasuredWidth()) {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST), heightMeasureSpec);
                }
            }
        }
    }
}
