package com.example.hsp.suspendedball;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener{

    /**
     *需要初始化的控件
     */
    private Toolbar toolbar;
    
    private ImageButton buttons[]=new ImageButton[10];
    
    private SeekBar seekBar;
    private SeekBar seekBar_alpha;

    private Switch open;

    /**
     * 设备管理器
     */
    private DevicePolicyManager policyManager;

    private ComponentName componentName;

    /**
     * startActivityForResult()的返回码
     */
    private static int CODE_OVERLAY = 1;
    private static int CODE_ACCESSIBILITY = 2;
    private static int CODE_MANAGER = 3;

    /**
     * 系统是否root
     */
    private boolean isRoot = false;

    private int state_seekbar = -1;
    private int state_seekbar2 = -1;
    private int state_switch = -1;

    private AdView adView;

    private String[] data = new String[]{"-1","-1","-1","-1","-1","-1","-1"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(getApplicationContext(),"ca-app-pub-1103366534239838~7266805308");

        adView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        /**
         * 获取设备管理接收者
         */
        policyManager = (DevicePolicyManager) getSystemService(Context.
                DEVICE_POLICY_SERVICE);

        /**
         * 申请权限，AdminReceiver继承自DeviceAdminReceiver
         */
        componentName = new ComponentName(this,AdminReceiver.class);
        isRoot = new Root().isDeviceRooted();
        if (new FileIO().checkFileDir()) {
            if (new FileIO().createFile()) {
                data = new FileIO().pullData();
            }
        }

        if (data[5].equals("-1")) {
            checkRoot();
        }
        init();
        initState();

        /**
         * 判断系统是否root
         * 以及获取root权限
         */
        if (isRoot) {
            Toast.makeText(this, getResources().getString(R.string.isRoot), Toast.LENGTH_SHORT).show();
        }

        /**
         * 检查应用是否有权限出现在最上层
         */
        checkDrawOverlays();
    }

    private void checkRoot() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("check root");
        builder.setMessage(getResources().getString(R.string.isRoot)+"?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isRoot = true;
                new Root().isDeviceRooted();
                data = new FileIO().pullData();
                data[5] = "1";
                new FileIO().clearData();
                new FileIO().pushData(data);
            }
        });
        builder.setNegativeButton("NOP", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isRoot = false;
                data = new FileIO().pullData();
                data[5] = "0";
                new FileIO().clearData();
                new FileIO().pushData(data);
            }
        });
        builder.show();
    }

    private void initState() {
        if (!isDataEmpty(data)) {
            seekBar_alpha.setProgress((int)(100*Float.parseFloat(data[3].trim())));
            seekBar.setProgress((int)(50*Float.parseFloat(data[6].trim())));
            open.setChecked(data[4].equals("1"));
            isRoot = data[5].equals("1");
        } else {
            data[0] = "-1";
            data[1] = "-1";
            data[2] = "-1";
            data[3] = String.valueOf(seekBar_alpha.getProgress()/100.0f);
            data[4] = open.isChecked()?"1":"0";
            data[5] = isRoot?"1":"0";
            data[6] = String.valueOf(seekBar.getProgress()/50.0f);
            new FileIO().pushData(data);
        }
    }

    private boolean isDataEmpty(String Data[]) {
        for (int i = 0;i <Data.length;i++) {
            if (!Data[i].equals("-1")){
                return false;
            }
        }
        return true;
    }

    private void checkDrawOverlays() {
        if (Build.VERSION.SDK_INT>=23) {
            if (! Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent,CODE_OVERLAY);
                Toast.makeText(this, getResources().getString(R.string.top), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.overlaySuccess), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void activeManage() {
        if (!isRoot) {
            if (! policyManager.isAdminActive(componentName)) {
                /**
                 *启动设备管理(隐式Intent) - 在AndroidManifest.xml中设定相应过滤器
                 */
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);

                /**
                 * 权限列表
                 */
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);

                /**
                 * 描述(additional explanation) 在申请权限时出现的提示语句
                 */
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        getResources().getString(R.string.active));

                startActivityForResult(intent, CODE_MANAGER);
            } else {
                Toast.makeText(this, getResources().getString(R.string.active_complete), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.isRoot), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_MANAGER && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, getResources().getString(R.string.active_complete), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.active_fail), Toast.LENGTH_SHORT).show();
        }
        if (requestCode == CODE_ACCESSIBILITY && requestCode ==Activity.RESULT_OK) {
            Toast.makeText(this, getResources().getString(R.string.accessibilitySuccess), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.accessibilityFail), Toast.LENGTH_SHORT).show();
        }
        if (requestCode == CODE_OVERLAY && requestCode == Activity.RESULT_OK) {
            Toast.makeText(this, getResources().getString(R.string.overlaySuccess), Toast.LENGTH_SHORT).show();
            activeManage();
        } else {
            Toast.makeText(this, getResources().getString(R.string.overlayFail), Toast.LENGTH_SHORT).show();
            activeManage();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this,SuspendedBallService.class);
        Bundle data = new Bundle();
        data.putInt("type",SuspendedBallService.TYPE_COLOR);
        switch (view.getId()){

            case R.id.button1:
                data.putInt("color",0);
                intent.putExtras(data);
                startService(intent);
                break;

            case R.id.button2:
                data.putInt("color",1);
                intent.putExtras(data);
                startService(intent);
                break;

            case R.id.button3:
                data.putInt("color",2);
                intent.putExtras(data);
                startService(intent);
                break;

            case R.id.button4:
                data.putInt("color",3);
                intent.putExtras(data);
                startService(intent);
                break;

            case R.id.button5:
                data.putInt("color",4);
                intent.putExtras(data);
                startService(intent);
                break;

            case R.id.button6:
                data.putInt("color",5);
                intent.putExtras(data);
                startService(intent);
                break;

            case R.id.button7:
                data.putInt("color",6);
                intent.putExtras(data);
                startService(intent);
                break;

            case R.id.button8:
                data.putInt("color",7);
                intent.putExtras(data);
                startService(intent);
                break;

            case R.id.button9:
                data.putInt("color",8);
                intent.putExtras(data);
                startService(intent);
                break;

            case R.id.button10:
                data.putInt("color",9);
                intent.putExtras(data);
                startService(intent);
                break;

            default:break;
        }
    }

    /**
     * 初始化操作
     */
    public void init(){

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId()==R.id.exit) {
                    finish();
                } else if (item.getItemId() == R.id.acvite_menu) {
                    /**
                     *启动设备管理(隐式Intent) - 在AndroidManifest.xml中设定相应过滤器
                     */
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);

                    /**
                     * 权限列表
                     */
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);

                    /**
                     * 描述(additional explanation) 在申请权限时出现的提示语句
                     */
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            getResources().getString(R.string.active));

                    startActivityForResult(intent, CODE_MANAGER);
                } else if (item.getItemId() == R.id.root_menu) {
                    checkRoot();
                } else if (item.getItemId() == R.id.introduce) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://crazystrome.github.io/SuspendedBallIntroduce.github.io/"));
                    startActivity(intent);
                } else if (item.getItemId() == R.id.accessibility_menu) {
                    startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),CODE_ACCESSIBILITY);
                } else if (item.getItemId() == R.id.overlay){
                    if (Build.VERSION.SDK_INT>=23) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent,CODE_OVERLAY);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.top), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    return true;
                }
                return false;
            }
        });

        buttons[0]=(ImageButton)findViewById(R.id.button1);
        buttons[1]=(ImageButton)findViewById(R.id.button2);
        buttons[2]=(ImageButton)findViewById(R.id.button3);
        buttons[3]=(ImageButton)findViewById(R.id.button4);
        buttons[4]=(ImageButton)findViewById(R.id.button5);
        buttons[5]=(ImageButton)findViewById(R.id.button6);
        buttons[6]=(ImageButton)findViewById(R.id.button7);
        buttons[7]=(ImageButton)findViewById(R.id.button8);
        buttons[8]=(ImageButton)findViewById(R.id.button9);
        buttons[9]=(ImageButton)findViewById(R.id.button10);
        for (int i=0;i<10;i++){
            buttons[i].setOnClickListener(this);
        }

        seekBar=(SeekBar)findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar_alpha = (SeekBar)findViewById(R.id.seekbar_alpha);
        seekBar_alpha.setOnSeekBarChangeListener(this);

        open = (Switch) findViewById(R.id.switch_openball);
        open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkAccessibility();
                    Intent intent = new Intent(MainActivity.this,SuspendedBallService.class);
                    Bundle data = new Bundle();
                    data.putInt("type",SuspendedBallService.TYPE_ADD);
                    intent.putExtras(data);
                    startService(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this,SuspendedBallService.class);
                    Bundle data = new Bundle();
                    data.putInt("type",SuspendedBallService.TYPE_DEL);
                    intent.putExtras(data);
                    startService(intent);
                }
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (seekBar.getId() == R.id.seekbar) {

            /**
             * 改变悬浮球的大小
             */
            float times = (float)i/50f;
            Intent intent = new Intent(MainActivity.this,SuspendedBallService.class);
            Bundle data = new Bundle();
            data.putInt("type",SuspendedBallService.TYPE_SIZE);
            data.putFloat("size",times);
            intent.putExtras(data);
            startService(intent);
        } else {

            /**
             * 改变悬浮球的透明度
             */
            float alpha = (float)i/100f;
            Intent intent = new Intent(MainActivity.this,SuspendedBallService.class);
            Bundle data = new Bundle();
            data.putInt("type",SuspendedBallService.TYPE_ALPHA);
            data.putFloat("alpha",alpha);
            intent.putExtras(data);
            startService(intent);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    /**
     * 判断辅助功能是否打开
     */
    private void checkAccessibility() {
        if (! AccessibilityUtils.isAccessibilitySettingOn(this)) {
            startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),CODE_ACCESSIBILITY);
            Toast.makeText(this, getResources().getString(R.string.accessibility), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.accessibilitySuccess), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if ((state_seekbar != -1) && (state_seekbar2 != -1) && (state_switch != -1)) {
            seekBar_alpha.setProgress(state_seekbar2);
            seekBar.setProgress(state_seekbar);
            if (state_switch == 1) {
                open.setChecked(true);
            } else {
                open.setChecked(false);
            }
            state_seekbar = -1;
            state_switch = -1;
            state_seekbar2 = -1;
        }
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
        state_seekbar = seekBar.getProgress();
        state_seekbar2 = seekBar_alpha.getProgress();
        state_switch = open.isChecked()?1:0;
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
        data = new FileIO().pullData();
        data[3] = String.valueOf(seekBar_alpha.getProgress()/100.0f);
        data[4] = open.isChecked()?"1":"0";
        data[5] = isRoot?"1":"0";
        data[6] = String.valueOf(seekBar.getProgress()/50.0f);
        new FileIO().clearData();
        new FileIO().pushData(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }
}
