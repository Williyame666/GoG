package com.aren.gridoverlay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;

    private Button btnStart, btnStop;
    private SeekBar seekBarSpacing;
    private TextView tvSpacingValue;

    private GridOverlayService overlayService;
    private boolean isBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GridOverlayService.LocalBinder binder = (GridOverlayService.LocalBinder) service;
            overlayService = binder.getService();
            isBound = true;
            if (overlayService != null) {
                overlayService.updateGridSpacing(seekBarSpacing.getProgress());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            overlayService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        seekBarSpacing = findViewById(R.id.seekBarSpacing);
        tvSpacingValue = findViewById(R.id.tvSpacingValue);

        // Grid spacing range: 20dp to 150dp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBarSpacing.setMin(20);
        }
        seekBarSpacing.setMax(150);
        seekBarSpacing.setProgress(50);
        tvSpacingValue.setText("Grid Spacing: 50 dp");

        seekBarSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = Math.max(20, progress);
                tvSpacingValue.setText("Grid Spacing: " + value + " dp");
                if (isBound && overlayService != null) {
                    overlayService.updateGridSpacing(value);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkOverlayPermission()) {
                    startAndBindService();
                } else {
                    requestOverlayPermission();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopOverlayService();
            }
        });
    }

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        }
    }

    private void startAndBindService() {
        Intent intent = new Intent(this, GridOverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Toast.makeText(this, "Overlay Service Started", Toast.LENGTH_SHORT).show();
    }

    private void stopOverlayService() {
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        Intent intent = new Intent(this, GridOverlayService.class);
        stopService(intent);
        Toast.makeText(this, "Overlay Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (checkOverlayPermission()) {
                startAndBindService();
            } else {
                Toast.makeText(this, "Overlay Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
