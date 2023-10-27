package com.StardewValley.modinstaller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;

import java.util.Queue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final int REQ_DATA_FOLDER_PERMISSION = 1000;

    String filename;
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
                    if (msg.arg1 == 1) {
                        new FileTool().writeToDataFolder(MainActivity.this, mainHandler);
                    }
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "安装完成！", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button0).setOnClickListener(this);
        findViewById(R.id.button1).setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("安装中......");
        progressDialog.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button0) {
            PermissionTool.reqPermission(this);
        } else if (v.getId() == R.id.button1) {
            if (PermissionTool.isGranted) {
                new FileTool().writeToStardewalleyFolder(this, mainHandler);
            } else PermissionTool.reqPermission(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DATA_FOLDER_PERMISSION) {
            PermissionTool.handlePermissionResult(this, requestCode, resultCode, data);
        }
    }
}