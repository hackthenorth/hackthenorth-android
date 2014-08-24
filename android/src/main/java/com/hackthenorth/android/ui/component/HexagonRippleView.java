package com.hackthenorth.android.ui.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;

import com.hackthenorth.android.util.Units;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class HexagonRippleView extends RippleView {

    private static final String TAG = "HexagonRippleView";
    // O(n) drawing is fine because n won't ever be too large
    // Could maybe use a breadth-first graph traversal if it gets to that point though heh
    ArrayList<RegularHexagon> mHexagons = new ArrayList<RegularHexagon>();

    public HexagonRippleView(Context context) {
        this(context, null, 0);
    }
    public HexagonRippleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public HexagonRippleView(Context context, AttributeSet attrs, int idk) {
        super(context, attrs, idk);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        int radius = Units.dpToPx(getContext(), 40);
        int d = (int)(4 * radius * Math.cos(11 * Math.PI / 6));

        boolean stagger = false;
        for (int i = 0; i < h + radius; i += radius) {
            int j = 0;
            if (stagger) {
                j = (int)(2 * radius * Math.cos(11 * Math.PI / 6));
            }
            stagger = !stagger;

            for (; j < w + radius; j += d) {
                mHexagons.add(new RegularHexagon(j, i, radius));
            }
        }
    }

    @Override
    protected void drawRipple(@NonNull final Canvas canvas) {
        Paint red = new Paint();
        red.setColor(android.graphics.Color.RED);
        red.setStyle(Paint.Style.FILL_AND_STROKE);

        for (Animator animator : animatorSet) {
            for (RegularHexagon hexagon : mHexagons) {
                if (dist(animator.x, animator.y, hexagon.x, hexagon.y) - 50 <= animator.radius) {
                    hexagon.draw(canvas, animator.paint);
                }
            }
        }
    }

    private double dist(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx*dx+dy*dy);
    }

    private static class Point {
        public double x;
        public double y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    //    __
    //   /  \
    //   \__/
    //
    private static class RegularHexagon {
        // Center and radius of bounding circle
        public double x;
        public double y;
        public double radius;

        public RegularHexagon(double x, double y, double radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        public String toString() {
            return String.format("(%f,%f):%f", x, y, radius);
        }

        public ArrayList<Point> getPoints() {
            ArrayList<Point> results = new ArrayList<Point>(6);
            for (int i = 0; i < 6; i++) {
                double px = x + Math.cos(i * Math.PI / 3) * radius;
                double py = y + Math.sin(i * Math.PI / 3) * radius;
                results.add(new Point(px, py));
            }
            return results;
        }

        public void draw(Canvas canvas, Paint paint) {
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setAntiAlias(true);

            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);

            ArrayList<Point> points = getPoints();
            Point start = points.get(0);
            path.moveTo((float)start.x, (float)start.y);
            for (int i = 1; i < points.size(); i++) {
                Point point = points.get(i);
                path.lineTo((float)point.x, (float)point.y);
            }
            path.lineTo((float)start.x, (float)start.y);

            path.close();
            canvas.drawPath(path, paint);
        }
    }
}
