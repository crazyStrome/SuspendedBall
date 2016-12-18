package com.example.hsp.suspendedball;

import android.os.Build;

import java.io.File;

/**
 * Created by hsp on 2016/12/15.
 */

public class Root {

    /**
     * 检测设备是否root了
     */
    private static String LOG_TAG = Root.class.getName();

    public boolean isDeviceRooted() {
        if (checkRootMethod1()) {
            return true;
        }
        if (checkRootMethod2()) {
            return true;
        }
        if (checkRootMethod3()) {
            return true;
        }
        return false;
    }



    public boolean checkRootMethod1() {
        String buildTags = Build.TAGS;

        if (buildTags != null && buildTags.contains("text-keys")) {
            return true;
        }
        return false;
    }

    public boolean checkRootMethod2() {
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) { }

        return false;
    }

    public boolean checkRootMethod3() {
        if (new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary) != null) {
            return true;
        } else {
            return false;
        }
    }
}
