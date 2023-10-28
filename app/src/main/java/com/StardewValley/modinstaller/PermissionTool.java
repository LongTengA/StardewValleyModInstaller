package com.StardewValley.modinstaller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.OnPermissionCallback;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import kotlin.contracts.Returns;

public class PermissionTool {
    static String path_to_data = "/storage/emulated/0/Android/data/com.zane.stardewvalley";
    static boolean isGranted = false;
    static boolean isGranted2 = false;
    static final int REQ_DATA_FOLDER_PERMISSION = 1000;

    public static void reqExternalStorage(Activity activity) {
        {
            XXPermissions.with(activity)
                    // 申请单个权限
                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                    // 设置权限请求拦截器（局部设置）
                    //.interceptor(new PermissionInterceptor())
                    // 设置不触发错误检测机制（局部设置）
                    //.unchecked()
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                            if (!allGranted) {
                                Toast.makeText(activity, "获取部分权限成功，但部分权限未正常授予", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            isGranted = true;
                            Toast.makeText(activity, "获取存储权限成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                            if (doNotAskAgain) {
                                Toast.makeText(activity, "被拒绝授权，请手动授予存储权限", Toast.LENGTH_SHORT).show();
                                // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                XXPermissions.startPermissionActivity(activity, permissions);
                            } else {
                                Toast.makeText(activity, "获取存储权限失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public static void reqDataFolderPermission(Activity activity) {
        Uri uri = Uri.parse(FileTool.changeToUri(path_to_data));
        DocumentFile documentFile = DocumentFile.fromTreeUri(activity, uri);
        //检查权限
        if (documentFile.canRead() && documentFile.canWrite()) {
            return;
        } else {
            // 请求/Android/data/com.zane.stardewvalley/文件夹权限
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION |
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            );
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile.getUri());
            activity.startActivityForResult(intent, REQ_DATA_FOLDER_PERMISSION);
        }
    }

    public static void checkPermisssion(Activity activity) {
        if (XXPermissions.isGranted(activity, Permission.MANAGE_EXTERNAL_STORAGE)) {
            isGranted = true;
        }
        DocumentFile documentFile = DocumentFile.fromTreeUri(activity, Uri.parse(FileTool.changeToUri(path_to_data)));
        if (documentFile.canRead() && documentFile.canWrite()) {
            isGranted2 = true;
        }

    }


    public static void handlePermissionResult(Activity activity, int requestCode, int resultCode, Intent data) {
        String targetPackageName = "com.zane.stardewvalley";
        Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(targetPackageName);
        if (launchIntent == null) {
            Toast.makeText(activity, "请先安装smapi星露谷物语，再启动安装器安装！", Toast.LENGTH_LONG).show();
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            DocumentFile documentFile = DocumentFile.fromTreeUri(activity, uri);
            if (documentFile.canRead() && documentFile.canWrite()) {
                // 获取到权限了
                isGranted2 = true;
                Toast.makeText(activity, "获取到权限了", Toast.LENGTH_SHORT).show();
                //持久化
                activity.getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                Toast.makeText(activity, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
