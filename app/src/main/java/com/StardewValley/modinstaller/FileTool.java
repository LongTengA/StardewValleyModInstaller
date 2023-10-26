package com.StardewValley.modinstaller;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;


public class FileTool {
    final String path_to_mods = "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Mods";
    final String path_to_data = "/storage/emulated/0/Android/data/com.zane.stardewvalley";
    final String path_to_stardew = "/storage/emulated/0/StardewValley";
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    public boolean writeToStardewalleyFolder(Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File p = new File(path_to_stardew);
                if (p.exists()) {
                    deleteFolderRecursively(p);
                }
                try {
                    if (p.mkdir()) {
                        AssetManager assetManager = context.getAssets();
                        InputStream inputStream = assetManager.open("StardewValley.zip");
                        new UnZipper().unzip(inputStream, path_to_stardew, handler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }

    public void writeToDataFolder(Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AssetManager assetManager = context.getAssets();
                    InputStream inputStream = assetManager.open("Mod.zip");
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        Uri uri = Uri.parse(changeToUri(path_to_data));
                        DocumentFile documentFile = DocumentFile.fromTreeUri(context, uri);
                        //找到Mods文件夹
                        String[] path = path_to_mods.replace(path_to_data, "").split("/");
                        for (int i = 1; i < path.length; i++) {
                            String s = path[i];
                            if (documentFile.findFile(s) == null) {
                                documentFile = documentFile.createDirectory(s);
                            } else {
                                documentFile = documentFile.findFile(s);
                            }
                        }
                        new UnZipper().unzipToDocumentFile(context, inputStream, documentFile, handler);
                    } else {
                        new UnZipper().unzip(inputStream, path_to_mods, handler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //递归删除文件夹
    static public void deleteFolderRecursively(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolderRecursively(file); // 递归删除子文件夹
                } else {
                    file.delete(); // 删除文件
                }
            }
        }
        folder.delete(); // 删除文件夹
    }

    public static void deleteDocumentFileRecursively(Context context, DocumentFile documentFile) {
        if (documentFile == null || !documentFile.exists()) {
            return;
        }
        if (documentFile.isDirectory()) {
            for (DocumentFile childFile : documentFile.listFiles()) {
                deleteDocumentFileRecursively(context, childFile);
            }
        }
        documentFile.delete();
    }

    public static String changeToUri(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F");
        return "content://com.android.externalstorage.documents/tree/primary%3A" + path2;
    }


}


