package com.hackthenorth.android.ui.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.hackthenorth.android.R;
import com.hackthenorth.android.util.Units;

import java.util.ArrayList;
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

    protected void drawRipple(@NonNull final Canvas canvas) {
        Paint red = new Paint();
        red.setColor(android.graphics.Color.RED);
        red.setStyle(Paint.Style.FILL_AND_STROKE);

        canvas.clipRect(getPaddingLeft(), getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom(),
                Region.Op.REPLACE);

        // Calculate the new color
        int color = Color.argb(0, 0, 0, 0);
        for (Animator animator : animatorSet) {
            color = composeColors(color, animator.paint.getColor());
        }

        // Draw once using that new color.
        for (Animator animator : animatorSet) {
            Paint paint = new Paint(animator.paint);
            paint.setColor(color);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            break;
        }

        for (RegularHexagon hexagon : mHexagons) {

            // Repeat the process here for each hexagon
            color = Color.argb(0, 0, 0, 0);
            for (Animator animator : animatorSet) {
                double d = dist(animator.x, animator.y, hexagon.x, hexagon.y);

                if (d - 100 < animator.radius) {
                    int hexRGB = hexagon.getColor(getContext(), animator);
                    int hexColor = Color.argb(animator.paint.getAlpha(),
                            Color.red(hexRGB), Color.green(hexRGB), Color.blue(hexRGB));

                    color = composeColors(color, hexColor);
                }
            }

            // Build the paint and draw once.
            for (Animator animator : animatorSet) {
                Paint paint = new Paint(animator.paint);
                paint.setColor(color);
                hexagon.draw(canvas, paint);
                break;
            }
        }
    }

    public static String hex(int n) {
        // call toUpperCase() if that's required
        return String.format("0x%8s", Integer.toHexString(n)).replace(' ', '0');
    }

    private int composeColors(int col1, int col2) {
        // reference: http://en.wikipedia.org/wiki/Alpha_compositing

        float a_1 = (float)Color.alpha(col1) / 255f;
        float a_2 = (float)Color.alpha(col2) / 255f;

        // calculate the alpha of the composed color
        float alpha = a_1 + a_2 * (1 - a_1);

        // calculate the RGB values
        float red = (1f / alpha) * ((Color.red(col1) * a_1) + (Color.red(col2) * a_2 * (1f - a_1)));
        float green = (1f / alpha) * ((Color.green(col1) * a_1) + (Color.green(col2) * a_2 * (1f - a_1)));
        float blue = (1f / alpha) * ((Color.blue(col1) * a_1) + (Color.blue(col2) * a_2 * (1f - a_1)));

        red = Float.isNaN(red) ? 0 : red;
        green = Float.isNaN(green) ? 0 : green;
        blue = Float.isNaN(blue) ? 0 : blue;

        int result = Color.argb((int) (255 * alpha), (int) red, (int) green, (int) blue);

        return result;
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

        public int getColor(Context context, Animator animator) {
            Integer color = colors.get(animator);
            if (color == null) {
                color = Math.round(4 * Math.random()) % 4 == 0 ?
                        context.getResources().getColor(R.color.blue) :
                        context.getResources().getColor(R.color.background_gray);
                colors.put(animator, color);
            }
            return color;
        }

        public void draw(Canvas canvas, Paint paint) {
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(false);

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
