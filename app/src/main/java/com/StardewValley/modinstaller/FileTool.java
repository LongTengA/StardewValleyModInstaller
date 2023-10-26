package com.StardewValley.modinstaller;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;


public class FileTool {
    final String path_to_mods = "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Mods";
    final String path_to_data = "/storage/emulated/0/Android/data/com.zane.stardewvalley";
    final String path_to_stardew = "/storage/emulated/0/StardewValley";

    public boolean writeToStardewalleyFolder(Context context, Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AssetManager assetManager = context.getAssets();

                    InputStream inputStream2 = assetManager.open("StardewValley.zip");
                    int count = countFiles(inputStream2);
                    Bundle bundle = new Bundle();
                    Message message = new Message();
                    message.what = MainActivity.FIEL_COUNT;
                    bundle.putInt("FILESIZE", count);
                    message.setData(bundle);
                    handler.sendMessage(message);

                    File p = new File(path_to_stardew);
                    if (p.exists()) {
                        deleteFolderRecursively(p);
                    }
                    if (p.mkdir()) {

                        InputStream inputStream = assetManager.open("StardewValley.zip");
                        new UnZipper().unzip(inputStream, path_to_stardew, handler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Message m = new Message();
                m.what = MainActivity.PROGRESS_END;
                m.arg1 = 1;
                handler.sendMessage(m);
            }
        }).start();
        return true;
    }

    public void writeToDataFolder(Context content, Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AssetManager assetManager = content.getAssets();

                    InputStream inputStream2 = assetManager.open("Mod.zip");
                    int count = countFiles(inputStream2);
                    Bundle bundle = new Bundle();
                    Message message = new Message();
                    message.what = MainActivity.FIEL_COUNT;
                    bundle.putInt("FILESIZE", count);
                    message.setData(bundle);
                    handler.sendMessage(message);

                    InputStream inputStream = assetManager.open("Mod.zip");
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        Uri uri = Uri.parse(changeToUri(path_to_data));
                        DocumentFile documentFile = DocumentFile.fromTreeUri(content, uri);
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
                        new UnZipper().unzipToDocumentFile(content, inputStream, documentFile, handler);
                    } else {
                        new UnZipper().unzip(inputStream, path_to_mods, handler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Message m = new Message();
                m.what = MainActivity.PROGRESS_END;
                m.arg1 = 2;
                handler.sendMessage(m);
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

    public int countFiles(InputStream inputStream) throws IOException {
        int count = 0;
        ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("gbk"));
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            // 获取条目的名称
            String entryName = zipEntry.getName();
            count++;
            // 关闭当前条目
            zis.closeEntry();
        }
        zis.close();
        return count;
    }

}


