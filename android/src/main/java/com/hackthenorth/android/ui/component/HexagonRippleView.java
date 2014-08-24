package com.hackthenorth.android.ui.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;

import com.hackthenorth.android.R;
import com.hackthenorth.android.util.Units;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import java.util.WeakHashMap;

public class HexagonRippleView extends RippleView {

    private static final String TAG = "HexagonRippleView";

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
        initializeHexagons(w, h, oldWidth, oldHeight);
    }

    private void initializeHexagons(int w, int h, int ow, int oh) {

        // Set the ripple radius and duration

        if (w != ow || h != oh) {
            // Clear the hexagons
            mHexagons.clear();

            // The radius of the bounding circle of the hexagon
            double radius = Units.dpToPx(getContext(), 50);

            // The length of the shortest line segment where one endpoint is the center
            // and the other resides on the boundary of the hexagon
            double smallRadius = Math.sqrt(radius * radius * 3.0d / 4.0d);

            boolean stagger = false;

            double vadjust = Units.dpToPx(getContext(), 0.0d);
            double hadjust = (radius * radius) / (250.0d * 250.0d);

            for (double i = 0; i < h + radius; i += smallRadius + vadjust) {
                double j = 0.0d;
                if (stagger) {
                    j = (int) (2 * smallRadius * Math.cos(11 * Math.PI / 6));
                }

                for (; j < w + radius; j += 3 * radius) {
                    if (stagger) {
                        j += hadjust;
                    }

                    mHexagons.add(new RegularHexagon(j, i, radius));
                }
                stagger = !stagger;
            }
        }
    }

    @Override
    protected void drawRipple(@NonNull final Canvas canvas) {
        Paint red = new Paint();
        red.setColor(android.graphics.Color.RED);
        red.setStyle(Paint.Style.FILL_AND_STROKE);

        canvas.clipRect(getPaddingLeft(), getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom(),
                Region.Op.REPLACE);

        for (Animator animator : animatorSet) {
            if (animator.paint.getAlpha() > 5) {
                for (RegularHexagon hexagon : mHexagons) {
                    double d = dist(animator.x, animator.y, hexagon.x, hexagon.y);

                    if (d - 100 < animator.radius) {
                        hexagon.draw(getContext(), canvas, animator.paint, animator);
                    }
                }
            }
            canvas.drawRect(0, 0, getWidth(), getHeight(), animator.paint);
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
        public WeakHashMap<Animator, Integer> colors = new WeakHashMap<Animator, Integer>();

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

        public void draw(Context context, Canvas canvas, Paint paint, Animator animator) {
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(false);

            paint = new Paint(paint);
            // Setting the color resets the alpha, so keep track of it as we set the color
            int alpha = paint.getAlpha();
            Integer color = colors.get(animator);
            if (color == null) {
                color = Math.round(6 * Math.random()) % 4 == 0 ?
                        context.getResources().getColor(R.color.theme_primary) :
                        context.getResources().getColor(R.color.background_gray);
                colors.put(animator, color);
            }
            paint.setColor(color);
            paint.setAlpha(alpha);

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
