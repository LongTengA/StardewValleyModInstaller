package com.StardewValley.modinstaller;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Stack;


public class UnZipper {
    public void unzip(InputStream inputStream, String destDir, Handler handler) throws IOException {
        int count = 0;
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("gbk"));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            String fileName = zipEntry.getName();
            File newFile = new File(destDir, fileName);
            if (zipEntry.isDirectory()) {
                newFile.mkdirs();
            } else {
                new File(newFile.getParent()).mkdirs();
                OutputStream outputStream = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
            }
            zipEntry = zis.getNextEntry();
            count++;
            if (count % 10 == 0) {
                Bundle b = new Bundle();
                Message m = new Message();
                m.what = MainActivity.PROGRESS_UPDATE;
                b.putInt("PROGRESS", count);
                m.setData(b);
                handler.sendMessage(m);
            }
        }
        zis.closeEntry();
        zis.close();
        inputStream.close();
    }

    public void unzipToDocumentFile(Context context, InputStream inputStream, DocumentFile destDir, Handler handler) throws IOException {
        int count = 0;
        Stack<DocumentFile> DFstack = new Stack<>();
        Stack<String> nameStack = new Stack<>();
        byte[] buffer = new byte[1024 * 20];
        ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("gbk"));
        ZipEntry zipEntry = zis.getNextEntry();
        boolean firstRun = false;
        DocumentFile newFile = destDir;
        DocumentFile tmpFile = null;
        String fileName;
        String tmpFileName;
        DFstack.push(newFile);
        nameStack.push("");
        while (zipEntry != null) {
            fileName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                while (!fileName.contains(nameStack.peek()) && firstRun) {
                    nameStack.pop();
                    DFstack.pop();
                }
                firstRun = true;
                tmpFileName = fileName.replace(nameStack.peek(), "");
                tmpFileName = tmpFileName.replace("/", "");
                tmpFile = DFstack.peek().createDirectory(tmpFileName);
                DFstack.push(tmpFile);
                nameStack.push(fileName);
            } else {
                while (!fileName.contains(nameStack.peek())) {
                    nameStack.pop();
                    DFstack.pop();
                }
                tmpFile = DFstack.peek().createFile(null, fileName.replace(nameStack.peek(), ""));
                OutputStream outputStream = context.getContentResolver().openOutputStream(tmpFile.getUri());
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
            }
            zipEntry = zis.getNextEntry();
            count++;
            if (count % 10 == 0) {
                Bundle b = new Bundle();
                Message m = new Message();
                m.what = MainActivity.PROGRESS_UPDATE;
                b.putInt("PROGRESS", count);
                m.setData(b);
                handler.sendMessage(m);
            }
        }
        zis.closeEntry();
        zis.close();
        inputStream.close();
    }
}