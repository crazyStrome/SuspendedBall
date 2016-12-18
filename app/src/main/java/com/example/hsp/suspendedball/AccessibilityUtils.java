package com.example.hsp.suspendedball;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.provider.Settings;

/**
 * Created by hsp on 2016/12/12.
 */

public class AccessibilityUtils {

    /*
     *单击返回
     */
    public static void doBack(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /*
     *下拉打开通知栏
     */
    public static void doPullDown(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
    }

    /*
     *上拉返回桌面
     */
    public static void doPullUp(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    /*
     *左右滑动打开多任务
     */
    public static void deLeftOrRight(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
    }

    /*
     *检查辅助功能是否开启
     */
    public static boolean isAccessibilitySettingOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled= Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }
        return false;
    }

    /*
     *打开电源菜单
     */
    public static void doPowerMenu(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
    }
}
