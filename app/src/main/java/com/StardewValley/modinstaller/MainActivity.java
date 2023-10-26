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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final int REQ_DATA_FOLDER_PERMISSION = 1000;
    static final int FIEL_COUNT = 1;
    static final int PROGRESS_UPDATE = 2;
    static final int PROGRESS_END = 3;
    public Handler mainHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FIEL_COUNT: {
                    Bundle bundle = msg.getData();
                    int count = bundle.getInt("FILESIZE");
                    progressDialog.setMax(count);
                    progressDialog.show();
                    break;
                }
                case PROGRESS_UPDATE: {
                    Bundle bundle = msg.getData();
                    int progress = bundle.getInt("PROGRESS");
                    progressDialog.setProgress(progress);
                    break;
                }
                case PROGRESS_END: {
                    if (msg.arg1 == 1) {
                        new FileTool().writeToDataFolder(MainActivity.this, mainHandler);
                    }
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "完成！", Toast.LENGTH_SHORT).show();
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
        progressDialog.setMessage("解压中....");

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