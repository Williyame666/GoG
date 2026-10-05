package com.aren.gridoverlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class GridView extends View {

    private Paint gridPaint;
    private float gridSpacingDp = 50f; // Default 50dp

    public GridView(Context context) {
        super(context);
        init();
    }

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#6600FF66")); // Green semi-transparent
        gridPaint.setStrokeWidth(2f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
    }

    public void setGridSpacingDp(float spacingDp) {
        this.gridSpacingDp = spacingDp;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float spacingPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, gridSpacingDp, getResources().getDisplayMetrics()
        );

        if (spacingPx <= 0) return;

        // Vertical lines
        for (float x = 0; x <= width; x += spacingPx) {
            canvas.drawLine(x, 0, x, height, gridPaint);
        }

        // Horizontal lines
        for (float y = 0; y <= height; y += spacingPx) {
            canvas.drawLine(0, y, width, y, gridPaint);
        }

        // Diagonals (X pattern in grid cells)
        for (float x = 0; x < width; x += spacingPx) {
            for (float y = 0; y < height; y += spacingPx) {
                float right = Math.min(x + spacingPx, width);
                float bottom = Math.min(y + spacingPx, height);
                // Top-left to bottom-right
                canvas.drawLine(x, y, right, bottom, gridPaint);
                // Top-right to bottom-left
                canvas.drawLine(right, y, x, bottom, gridPaint);
            }
        }
    }
}
