package com.StardewValley.modinstaller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;
import android.os.Build;

import java.io.File;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    int state = 1;
    boolean back = false;
    private boolean running = true;
    public Handler mainHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UnZipper.FILE_SIZE: {
                    Bundle bundle = msg.getData();
                    int size = bundle.getInt(UnZipper.DATA_KEY_FILESIZE);
                    progressDialog.setMax(size);
                    progressDialog.show();
                    break;
                }
                case UnZipper.PROGRESS_UPDATE: {
                    Bundle bundle = msg.getData();
                    int progress = bundle.getInt(UnZipper.DATA_KEY_PROGRESS);
                    progressDialog.setProgress(progress);
                    progressDialog.setMessage(bundle.getString(UnZipper.DATA_KEY_FILENAME));
                    break;
                }
                case UnZipper.PROGRESS_DONE: {
                    state++;
                    progressDialog.dismiss();
                }
                default:
                    break;
            }
        }
    };
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (state == 8 && PermissionTool.isGranted2) state++;
            if (state == 2 && PermissionTool.isGranted) state++;
            Log.d("State", String.valueOf(state));
            switch (state) {
                case 1: {
                    PermissionTool.reqExternalStorage(MainActivity.this);
                    state++;
                    break;
                }
                case 3: {
                    new FileTool().writeToStardewalleyFolder(MainActivity.this, mainHandler);
                    state++;
                    break;
                }
                case 5: {
                    LaunchApp();
                    state++;
                    break;
                }
                case 7: {
                    PermissionTool.reqDataFolderPermission(MainActivity.this);
                    state++;
                    break;
                }
                case 9: {
                    new FileTool().dealBeforeFiles(MainActivity.this, mainHandler);
                    state++;
                    break;
                }
                case 11: {
                    new FileTool().writeToDataFolder(MainActivity.this, mainHandler);
                    state++;
                    break;
                }
                case 13: {
                    running = false;
                    state++;
                    Toast.makeText(MainActivity.this, "安装完成!", Toast.LENGTH_LONG).show();
                    break;
                }
                default:
                    break;
            }
            if (running) mainHandler.postDelayed(this, 500);
        }
    };
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionTool.checkPermisssion(MainActivity.this);
        findViewById(R.id.button1).setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("安装中，稍等片刻");
        progressDialog.setMessage("解压中。");
        progressDialog.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                mainHandler.post(runnable);
            } else {
                if (PermissionTool.isGranted) {
                    new FileTool().writeToStardewalleyFolder(this, mainHandler);
                    new FileTool().writeToDataFolder(this, mainHandler);
                    Toast.makeText(this, "安装成功！", Toast.LENGTH_SHORT).show();
                    LaunchApp();
                    this.finish();
                } else {
                    PermissionTool.reqExternalStorage(this);
                    Toast.makeText(this, "请授权", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionTool.REQ_DATA_FOLDER_PERMISSION) {
            PermissionTool.handlePermissionResult(this, requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 用户返回界面时执行操作
        if (back && state == 6)
            state++;
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (PermissionTool.isGranted)
            back = true;
    }

    public void LaunchApp() {
        String targetPackageName = "com.zane.stardewvalley";
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(targetPackageName);
        if (launchIntent != null) {
            startActivityForResult(launchIntent, 123);
            Toast.makeText(this, "请点击OK授权！", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "请安装smapi星露谷物语", Toast.LENGTH_LONG).show();
        }
    }
}