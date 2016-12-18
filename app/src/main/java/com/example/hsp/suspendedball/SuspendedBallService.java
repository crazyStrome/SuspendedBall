package com.example.hsp.suspendedball;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by hsp on 2016/12/13.
 */

public class SuspendedBallService extends AccessibilityService {

    public static final int TYPE_ADD = 0;
    public static final int TYPE_DEL = 1;
    public static final int TYPE_SIZE = 2;
    public static final int TYPE_COLOR = 3;
    public static final int TYPE_ALPHA = 4;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle data = intent.getExtras();
        if (data != null) {
            int type = data.getInt("type");
            if (type == TYPE_ADD) {
                SuspendedBallManager.addBallView(this);
            } else if (type == TYPE_DEL) {
                SuspendedBallManager.removeBallView(this);
            } else if (type == TYPE_SIZE) {
                float size = data.getFloat("size");
                SuspendedBallManager.changerSize(this,size);
            } else if (type == TYPE_COLOR) {
                int colorRGB = data.getInt("color");
                SuspendedBallManager.changeBackgroundColor(this,colorRGB);
            } else if (type == TYPE_ALPHA) {
                float alpha = data.getFloat("alpha");
                SuspendedBallManager.changerAlpha(this,alpha);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
