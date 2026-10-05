package com.aren.gridoverlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.core.app.NotificationCompat;

public class GridOverlayService extends Service {

    private final IBinder binder = new LocalBinder();
    private WindowManager windowManager;
    private GridView gridView;
    private ImageView toggleButton;
    private WindowManager.LayoutParams gridLayoutParams;
    private WindowManager.LayoutParams buttonLayoutParams;
    private boolean isGridVisible = true;
    private float currentSpacingDp = 50f;

    public class LocalBinder extends Binder {
        public GridOverlayService getService() {
            return GridOverlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        startForegroundServiceNotification();

        createGridView();
        createToggleButton();
    }

    private void startForegroundServiceNotification() {
        String channelId = "grid_overlay_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Grid Overlay Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Grid Overlay Running")
                .setContentText("Overlay active")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build();

        startForeground(1, notification);
    }

    private void createGridView() {
        gridView = new GridView(this);
        gridView.setGridSpacingDp(currentSpacingDp);

        int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        gridLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        windowManager.addView(gridView, gridLayoutParams);
    }

    private void createToggleButton() {
        toggleButton = new ImageView(this);
        toggleButton.setImageResource(android.R.drawable.ic_menu_view); // Eye/View icon
        toggleButton.setBackgroundColor(0x88000000); // Semi-transparent black background
        toggleButton.setPadding(16, 16, 16, 16);

        int sizePx = (int) (40 * getResources().getDisplayMetrics().density);

        int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        buttonLayoutParams = new WindowManager.LayoutParams(
                sizePx,
                sizePx,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        buttonLayoutParams.gravity = Gravity.TOP | Gravity.END;
        buttonLayoutParams.x = 20;
        buttonLayoutParams.y = 50;

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGridVisible = !isGridVisible;
                gridView.setVisibility(isGridVisible ? View.VISIBLE : View.GONE);
                toggleButton.setAlpha(isGridVisible ? 1.0f : 0.4f);
            }
        });

        windowManager.addView(toggleButton, buttonLayoutParams);
    }

    public void updateGridSpacing(float spacingDp) {
        this.currentSpacingDp = spacingDp;
        if (gridView != null) {
            gridView.setGridSpacingDp(spacingDp);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gridView != null && windowManager != null) {
            windowManager.removeView(gridView);
        }
        if (toggleButton != null && windowManager != null) {
            windowManager.removeView(toggleButton);
        }
    }
}
