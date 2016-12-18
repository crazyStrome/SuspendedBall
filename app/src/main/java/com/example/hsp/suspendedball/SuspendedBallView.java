package com.example.hsp.suspendedball;

import android.accessibilityservice.AccessibilityService;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Field;
/**
 * Created by hsp on 2016/12/12.
 */

public class SuspendedBallView extends LinearLayout {

    private CardView outsideCard;
    private CardView bigCard;
    private CardView centerCard;

    private WindowManager windowManager;

    private WindowManager.LayoutParams layoutParams;

    private long lastTime;
    private float lastX;
    private float lastY;

    private boolean isLongTouch;
    private boolean isTouching;

    private float touchSlop;
    private final static long LONG_CLICK_LIMIT = 300;
    private final static long REMOVE_LIMIT = 1000;
    private final static long CLICK_LIMIT = 200;

    private int statusBarHeight;

    private AccessibilityService service;

    private int currentMode;

    private final static int MODE_NONE = 0x000;
    private final static int MODE_DOWN = 0x001;
    private final static int MODE_UP = 0x002;
    private final static int MODE_LEFT = 0x003;
    private final static int MODE_RIGHT = 0x004;
    private final static int MODE_MOVE = 0x005;
    private final static int MODE_GONE = 0x006;
    private final static int MODE_SHUT = 0x007;

    private int offsetToParentX;
    private int offsetToParentY;
    private Vibrator vibrator;
    private long[] pattern = {0,100};

    private java.lang.Process process;
    private OutputStream outputStream;

    /**
     * 设备管理器
     */
    private DevicePolicyManager policyManager;

    private ComponentName componentName;

    private boolean isRoot = false;

    private String filePath = "";

    private boolean isShare = false;

    private float default_alpha = 0.5f;

    private static String[] data = new String[7];

    public SuspendedBallView (Context context) {
        super(context);
        data = new FileIO().pullData();
        if (data[5].equals("1")) {
            isRoot = true;
            try {
                process = Runtime.getRuntime().exec("su");
                outputStream = process.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            isRoot = false;
        }
        service = (AccessibilityService) context;
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        policyManager = (DevicePolicyManager) getContext().getSystemService(Context.
                DEVICE_POLICY_SERVICE);

        /**
         * 申请权限，AdminReceiver继承自DeviceAdminReceiver
         */
        componentName = new ComponentName(getContext(),AdminReceiver.class);
        initView();
    }

    /**
     * 初始化以及对手势的判断
     */
    public void initView() {
        inflate(getContext(),R.layout.suspend,this);

        outsideCard = (CardView)findViewById(R.id.outside);
        outsideCard.setAlpha(default_alpha);
        bigCard = (CardView)findViewById(R.id.bigcard);
        bigCard.setAlpha(default_alpha);
        centerCard = (CardView)findViewById(R.id.centercard);
        centerCard.setAlpha(default_alpha);

        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        currentMode = MODE_NONE;

        statusBarHeight = getStatusBarHeight();
        offsetToParentX = dip2px(25);
        offsetToParentY = statusBarHeight + offsetToParentX;

        outsideCard.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, final MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouching = true;
                        centerCard.setVisibility(INVISIBLE);
                        bigCard.setVisibility(VISIBLE);
                        lastTime = System.currentTimeMillis();
                        lastX = motionEvent.getX();
                        lastY = motionEvent.getY();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!isLongTouch && isTouching && currentMode==MODE_NONE) {
                                    isLongTouch = isLongClick(motionEvent);
                                }
                            }
                        },LONG_CLICK_LIMIT);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (!isLongTouch && isTouchSlop(motionEvent)) {
                            return true;
                        }
                        if (isLongTouch && (currentMode == MODE_NONE || currentMode == MODE_MOVE)) {
                            layoutParams.x = (int) (motionEvent.getRawX() - offsetToParentX - bigCard.getWidth() / 2);
                            layoutParams.y = (int) (motionEvent.getRawY() - offsetToParentY - bigCard.getHeight() / 2);
                            windowManager.updateViewLayout(SuspendedBallView.this,layoutParams);
                            currentMode = MODE_MOVE;
                        } else {
                            doGesture(motionEvent);
                        }
                        break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        isTouching = false;
                        if (isLongTouch) {
                            isLongTouch = false;
                        } else if (isClick(motionEvent)) {
                            AccessibilityUtils.doBack(service);
                        } else {
                            doUp();
                        }
                        centerCard.setVisibility(VISIBLE);
                        bigCard.setVisibility(INVISIBLE);
                        currentMode = MODE_NONE;
                        break;
                }
                return true;
            }
        });
    }

    public void setLayoutParams(WindowManager.LayoutParams params) {
        layoutParams = params;
    }

    private void doUp() {
        switch (currentMode) {
            case MODE_LEFT:
                lockScreen();
                break;

            case MODE_RIGHT:
                AccessibilityUtils.deLeftOrRight(service);
                break;

            case MODE_DOWN:
                AccessibilityUtils.doPullDown(service);
                break;
            case MODE_UP:
                AccessibilityUtils.doPullUp(service);
                break;

            case MODE_SHUT:
                if (!sharePicture(filePath)) {
                    Toast.makeText(service, getContext().getResources().getString(R.string.shareFail), Toast.LENGTH_SHORT).show();
                } else {
                    isShare = false;
                }
                break;
        }
        bigCard.setX(outsideCard.getX() + (outsideCard.getWidth() - bigCard.getWidth()) / 2);
        bigCard.setY(outsideCard.getY() + (outsideCard.getHeight() - bigCard.getHeight()) / 2);
    }

    private void lockScreen() {
        if (isRoot) {
            if (policyManager.isAdminActive(componentName)) {
                PolicyManagerUtils.lockScreen(getContext());
            } else {
                RootMethod.lockScreen(outputStream);
            }
        } else {
            PolicyManagerUtils.lockScreen(getContext());
        }
    }

    private boolean isClick(MotionEvent event) {
        float offsetX = Math.abs(event.getX() - lastX);
        float offsetY = Math.abs(event.getY() - lastY);
        long time = System.currentTimeMillis() - lastTime;
        if (offsetX < touchSlop * 2 && offsetY < touchSlop * 2 && time < CLICK_LIMIT) {
            return true;
        } else {
            return false;
        }
    }

    private void doGesture(MotionEvent event) {
        float offsetX = event.getX() - lastX;
        float offsetY = event.getY() - lastY;

        if (Math.abs(offsetX) < touchSlop && Math.abs(offsetY) < touchSlop) {
            return;
        }
        if (Math.abs(offsetX) > Math.abs(offsetY)) {
            if (offsetX > 0) {
                if (currentMode == MODE_RIGHT) {
                    return;
                }
                currentMode = MODE_RIGHT;
                bigCard.setX(outsideCard.getX() + (outsideCard.getWidth() - bigCard.getWidth()));
                bigCard.setY(outsideCard.getY() + (outsideCard.getHeight() - bigCard.getHeight()) / 2);
            } else {
                if (currentMode == MODE_LEFT) {
                    return;
                }
                currentMode = MODE_LEFT;
                bigCard.setX(outsideCard.getX());
                bigCard.setY(outsideCard.getY() + (outsideCard.getHeight() - bigCard.getHeight()) / 2);
            }
        } else {
            if (offsetY > 0) {
                if (currentMode == MODE_DOWN || currentMode == MODE_GONE) {
                    return;
                }
                currentMode = MODE_DOWN;
                bigCard.setY(outsideCard.getY() + (outsideCard.getWidth() - bigCard.getWidth()));
                bigCard.setX(outsideCard.getX() + (outsideCard.getWidth() - bigCard.getWidth()) / 2);

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (currentMode == MODE_DOWN && isTouching) {
                            toRemove();
                            currentMode = MODE_GONE;
                        }
                    }
                },REMOVE_LIMIT);
            } else {
                if (currentMode == MODE_UP || currentMode == MODE_SHUT) {
                    return;
                }
                currentMode = MODE_UP;
                bigCard.setX(outsideCard.getX() + (outsideCard.getWidth() - bigCard.getWidth()) / 2);
                bigCard.setY(outsideCard.getY());

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (currentMode == MODE_UP && isTouching) {
                            toScreenShut();
                            currentMode = MODE_SHUT;
                        }
                    }
                },REMOVE_LIMIT);
            }
        }
    }

    private void toScreenShut() {
        vibrator.vibrate(pattern,-1);
        if (isRoot) {
            filePath = RootMethod.screenShot(outputStream);
            if (filePath == null && filePath.equals("")) {
                isShare = false;
            } else {
                isShare = true;
            }
        } else {
            AccessibilityUtils.doPowerMenu(service);
        }
    }

    private boolean sharePicture(String filepath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (filepath == null || filepath.equals("")) {
            intent.setType("text/plain");
            return false;
        } else {
            File file = new File(filepath);
            if (file != null && file.exists() && file.isFile()) {
                System.out.println("111111111111111111111111111111111");
                intent.setType("image/*");
                Uri uri = Uri.fromFile(file);
                intent.putExtra(Intent.EXTRA_STREAM,uri);
                System.out.println("333333333333333333333333333333333333");
            } else {
                return false;
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT,getContext().getResources().getString(R.string.share));
        intent.putExtra(Intent.EXTRA_TEXT,getContext().getResources().getString(R.string.shareText));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        System.out.println("333333333333333333333333");
        getContext().startActivity(Intent.createChooser(intent,getContext().getResources().getString(R.string.share)));
        return true;
    }

    private void toRemove() {
        vibrator.vibrate(pattern,-1);
        try {
            outputStream.close();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SuspendedBallManager.removeBallView(getContext());
    }

    private boolean isTouchSlop(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        if (Math.abs(x-lastX) < touchSlop && Math.abs(y-lastY) < touchSlop) {
            return true;
        }
        return false;
    }

    private boolean isLongClick(MotionEvent motionEvent) {
        float offsetX = Math.abs(motionEvent.getX() - lastX);
        float offsetY = Math.abs(motionEvent.getY() - lastY);
        long time = System.currentTimeMillis() - lastTime;

        if (offsetX < touchSlop && offsetY <touchSlop && time >=LONG_CLICK_LIMIT) {
            vibrator.vibrate(pattern,-1);
            return true;
        } else {
            return false;
        }
    }

    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class<?> c=Class.forName("com.android.internal.R$dimen");
            Object o=c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer)field.get(o);
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    public int dip2px(float dip) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,dip,getContext().getResources().getDisplayMetrics()
        );
    }
}
