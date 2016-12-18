package com.example.hsp.suspendedball;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Created by hsp on 2016/12/15.
 */

public class PolicyManagerUtils {

    /**
     * 使用设备管理器锁屏，会导致指纹识别不可用
     * @param context
     */
    public static void lockScreen(Context context) {
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(context,AdminReceiver.class);
        if (policyManager.isAdminActive(componentName)) {
            policyManager.lockNow();
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.active), Toast.LENGTH_SHORT).show();
        }
    }
}
