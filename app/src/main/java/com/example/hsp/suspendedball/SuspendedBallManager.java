package com.example.hsp.suspendedball;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.PixelCopy;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by hsp on 2016/12/13.
 */

public class SuspendedBallManager {

    private static int[] imageColor = new int[]{R.color.reset,R.color.reset1,R.color.reset2,R.color.reset3,
            R.color.reset4,R.color.reset5,R.color.reset6,R.color.reset7,R.color.reset8,R.color.reset9};

    private static SuspendedBallView suspendedBallView;

    private static WindowManager windowManager;

    private static WindowManager.LayoutParams params;

    private static float defaultOutsideSize = 40;
    private static float defaultOutsideRadius = 20;
    private static float defaultBigCardSize = 30;
    private static float defaultBigCardRadius = 15;
    private static float defaultCenterSize = 20;
    private static float defaultCenterRadius = 10;

    private static float default_alpha = 0.5f;

    private static String[] data = new String[7];

    private static int color = -1;

    private static float sizeStatic = -1f;

    private static float alphaStatic = -1f;
    /**
     * 在屏幕上添加悬浮球
     * @param context
     */
    public static void addBallView(Context context) {
        if (suspendedBallView == null) {
            data = new FileIO().pullData();
            WindowManager manager = getWindowManager(context);
            int screenWidth = manager.getDefaultDisplay().getWidth();
            int screenHeight = manager.getDefaultDisplay().getHeight();
            suspendedBallView = new SuspendedBallView(context);
            params = new WindowManager.LayoutParams();
            if (!data[0].equals("-1")) {
                params.x = (int)Float.parseFloat(data[0].trim());
            } else {
                params.x = screenWidth;
            }
            if (!data[1].equals("-1")) {
                params.y = (int)Float.parseFloat(data[1].trim());
            } else {
                params.y = screenHeight/2;
            }
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
            params.format = PixelFormat.RGBA_8888;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            if (!data[2].equals("-1")) {
                changeBackgroundColor(context,Integer.parseInt(data[2].trim()));
            }
            if (!data[6].equals("-1")) {
                changerSize(context,Float.parseFloat(data[6].trim()));
            }
            //if (!data[3].equals("-1")) {
                //changerAlpha(context,Float.parseFloat(data[3].trim()));
            //}
            suspendedBallView.setLayoutParams(params);
            manager.addView(suspendedBallView,params);
        }
    }

    /**
     * 改变悬浮球的背景颜色
     * @param context
     * @param colorRGB
     */
    public static void changeBackgroundColor(Context context,int colorRGB) {
        if (!(suspendedBallView==null)) {
            color = colorRGB;
            ((CardView)suspendedBallView.findViewById(R.id.outside)).setCardBackgroundColor(
                    context.getResources().getColor(imageColor[colorRGB])
            );
        }
    }

    /**
     * 改变悬浮球的大小
     * @param context
     * @param size
     */
    public static void changerSize(Context context, float size) {
        if (suspendedBallView != null) {
            sizeStatic =size;
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)(suspendedBallView.findViewById(R.id.outside))
                    .getLayoutParams();
            params.width = (int) (size * dip2px(context,defaultOutsideSize));
            params.height = (int) (size * dip2px(context,defaultOutsideSize));
            params.gravity = Gravity.CENTER;
            ((CardView)suspendedBallView.findViewById(R.id.outside)).setRadius(size * dip2px(context,defaultOutsideRadius));
            (suspendedBallView.findViewById(R.id.outside)).setLayoutParams(params);


            FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams)(suspendedBallView.findViewById(R.id.bigcard))
                    .getLayoutParams();
            params1.width = (int) (size * dip2px(context,defaultBigCardSize));
            params1.height = (int) (size * dip2px(context,defaultBigCardSize));
            params1.gravity = Gravity.CENTER;
            ((CardView)suspendedBallView.findViewById(R.id.bigcard)).setRadius(size * dip2px(context,defaultBigCardRadius));
            (suspendedBallView.findViewById(R.id.bigcard)).setLayoutParams(params1);


            FrameLayout.LayoutParams params2 = (FrameLayout.LayoutParams)(suspendedBallView.findViewById(R.id.centercard))
                    .getLayoutParams();
            params2.width = (int) (size * dip2px(context,defaultCenterSize));
            params2.height = (int) (size * dip2px(context,defaultCenterSize));
            params2.gravity = Gravity.CENTER;
            ((CardView)suspendedBallView.findViewById(R.id.centercard)).setRadius(size * dip2px(context,defaultCenterRadius));
            (suspendedBallView.findViewById(R.id.centercard)).setLayoutParams(params2);
        }
    }

    /**
     * 移除悬浮球
     * @param context
     */
    public static void removeBallView(Context context) {
        if (suspendedBallView != null) {
            float x = ((WindowManager.LayoutParams)suspendedBallView.getLayoutParams()).x;
            float y = ((WindowManager.LayoutParams)suspendedBallView.getLayoutParams()).y;
            data = new FileIO().pullData();
            data[0] = String.valueOf(x);
            data[1] = String.valueOf(y);
            data[2] = String.valueOf(color);
            data[3] = String.valueOf(alphaStatic);
            data[6] = String.valueOf(sizeStatic);
            new FileIO().clearData();
            new FileIO().pushData(data);
            WindowManager manager = getWindowManager(context);
            manager.removeView(suspendedBallView);
            suspendedBallView = null;
        }
    }

    /**
     * 获取windowManager
     * @param context
     * @return
     */
    private static WindowManager getWindowManager(Context context) {
        if (windowManager == null) {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return windowManager;
    }

    /**
     * do转成px
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 修改悬浮球透明度
     * @param context
     * @param alpha
     */
    public static void changerAlpha(Context context,float alpha) {
        if (!(suspendedBallView==null)) {
            alphaStatic = alpha;
            (suspendedBallView.findViewById(R.id.outside)).setAlpha(alpha*default_alpha);
            (suspendedBallView.findViewById(R.id.bigcard)).setAlpha(alpha*default_alpha);
            (suspendedBallView.findViewById(R.id.centercard)).setAlpha(alpha*default_alpha);
        }
    }
}
