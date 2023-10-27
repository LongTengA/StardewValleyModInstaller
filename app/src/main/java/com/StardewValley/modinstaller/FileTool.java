package com.StardewValley.modinstaller;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;


public class FileTool {
    final static String path_to_mods = "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Mods";
    final static String path_to_data = "/storage/emulated/0/Android/data/com.zane.stardewvalley";
    final static String path_to_stardew = "/storage/emulated/0/StardewValley";

    public void writeToStardewalleyFolder(Context context, Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File p = new File(path_to_stardew);
                    if (p.exists()) {
                        deleteFolderRecursively(p);
                    }
                    if (p.mkdir()) {
                        new UnZipper().unzip(context, "StardewValley.zip", path_to_stardew, handler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void writeToDataFolder(Context content, Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        DocumentFile documentFile = creatLegalDocFileByPath(content, path_to_mods);
                        new UnZipper().unzipToDocumentFile(content, "Mod.zip", documentFile, handler);
                    } else {
                        new UnZipper().unzip(content,"Mod.zip", path_to_mods, handler);
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

    //删除指定DocumentFile所在文件夹以及子目录
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


    public static DocumentFile creatLegalDocFileByPath(Context context, String path) throws IOException {
        if (!path.contains(path_to_data))
            throw new IOException("path is not legal" + context.getPackageName());
        Uri uri = Uri.parse(changeToUri(path_to_data));
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, uri);
        String[] tarpath = path.replace(path_to_data, "").split("/");
        for (int i = 1; i < tarpath.length; i++) {
            String s = tarpath[i];
            if (documentFile.findFile(s) == null) {
                documentFile = documentFile.createDirectory(s);
            } else {
                documentFile = documentFile.findFile(s);
            }
        }
        return documentFile;
    }
}


