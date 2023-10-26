package com.StardewValley.modinstaller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final int REQ_DATA_FOLDER_PERMISSION = 1000;
    private static Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button0).setOnClickListener(this);
        findViewById(R.id.button1).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button0) {
            PermissionTool.reqPermission(this);
        } else if (v.getId() == R.id.button1) {
            if (PermissionTool.isGranted) {
                new FileTool().writeToStardewalleyFolder(this);
                new FileTool().writeToDataFolder(this);
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