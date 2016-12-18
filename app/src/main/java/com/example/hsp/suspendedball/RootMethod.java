package com.example.hsp.suspendedball;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;

import java.io.File;
import java.io.OutputStream;

/**
 * Created by hsp on 2016/12/15.
 */

/**
 * 设备root后可以调用的方法
 */
public class RootMethod {

    /**
     * 通过向shell发指令锁屏
     * @param outputStream
     */
    public static void lockScreen(OutputStream outputStream) {
        try {
            String cmd = "input keyevent " + KeyEvent.KEYCODE_POWER + "\n";
            outputStream.write(cmd.getBytes());
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过向shell发指令截图，并返回图片储存路径
     * 指令为：screencap -p +路径
     * @param outputStream
     * @return
     */
    public static String screenShot(OutputStream outputStream) {
        String filepath = "";
        try {
            filepath = "mnt/sdcard/DCIM/"+System.currentTimeMillis()+".png";
            String cmd = "screencap -p "+filepath+"\n";
            outputStream.write(cmd.getBytes());
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filepath;
    }
}
