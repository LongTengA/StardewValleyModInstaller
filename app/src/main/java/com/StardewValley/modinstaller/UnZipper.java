package com.StardewValley.modinstaller;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
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
    public static final int PROGRESS_START = 1;
    public static final int PROGRESS_UPDATE = 2;
    public static final int FILE_SIZE = 3;
    public static final int PROGRESS_DONE = 4;
    public static final String DATA_KEY_PROGRESS = "PROGRESS";
    public static final String DATA_KEY_FILENAME = "FILE_NAME";
    public static final String DATA_KEY_FILESIZE = "FILE_SIZE";

    int space = 1;

    public void unzip(Context context, String sourceName, String destDir, Handler handler) throws
            IOException {
        UnzipMsgTool unzipMsgTool = new UnzipMsgTool();
        unzipMsgTool.sentTotalFiles(context, handler, sourceName);

        InputStream inputStream = context.getAssets().open(sourceName);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("gbk"));
        ZipEntry zipEntry = zis.getNextEntry();
        while ((zipEntry = zis.getNextEntry()) != null) {
            String fileName = zipEntry.getName();
            File newFile = new File(destDir, fileName);
            if (zipEntry.isDirectory()) {
                newFile.mkdirs();
            } else {
                new File(newFile.getParent()).mkdirs();
                unzipMsgTool.sentFileNameMsg(handler, newFile.getName(), space);
                OutputStream outputStream = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
            }
        }
        zis.closeEntry();
        zis.close();
        inputStream.close();
        UnzipMsgTool.progressDone(handler);
    }

    public void unzipToDocumentFile(Context context, String sourceName, DocumentFile
            destDir, Handler handler) throws IOException {
        UnzipMsgTool unzipMsgTool = new UnzipMsgTool();
        unzipMsgTool.sentTotalFiles(context, handler, sourceName);

        InputStream inputStream = context.getAssets().open(sourceName);

        Stack<DocumentFile> DFstack = new Stack<>();
        Stack<String> nameStack = new Stack<>();
        byte[] buffer = new byte[1024 * 4];
        ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("gbk"));

        boolean firstRun = false;
        DocumentFile newFile = destDir;
        DocumentFile tmpFile = null;
        String fileName;
        String tmpFileName;
        DFstack.push(newFile);
        nameStack.push("");
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
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
                tmpFileName = fileName.replace(nameStack.peek(), "");
                tmpFile = DFstack.peek().createFile(null, tmpFileName);
                unzipMsgTool.sentFileNameMsg(handler, tmpFileName, space);
                OutputStream outputStream = context.getContentResolver().openOutputStream(tmpFile.getUri());
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
            }
        }
        zis.closeEntry();
        zis.close();
        inputStream.close();
        UnzipMsgTool.progressDone(handler);
    }

    public static int countFiles(InputStream inputStream) throws IOException {
        int count = 0;
        ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("gbk"));
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            if (!zipEntry.isDirectory())
                count++;
            zis.closeEntry();
        }
        zis.close();
        return count;
    }
}

class UnzipMsgTool {
    String fileName;
    Bundle update_data = new Bundle();
    Message message = new Message();
    int count = 0;
    static int DONE_COUNT = 0;

    public void sentProgressMsg(@NonNull Handler handler) {
        count++;
        message = message.obtain();
        update_data.clear();

        message.what = UnZipper.PROGRESS_UPDATE;
        update_data.putInt(UnZipper.DATA_KEY_PROGRESS, count);

        message.setData(update_data);
        handler.sendMessage(message);
    }

    public void sentProgessMsg(@NonNull Handler handler, int space) {
        if (count % space == 0) {
            sentProgressMsg(handler);
        } else {
            count++;
        }
    }

    public void sentFileNameMsg(Handler handler, String fileName) {
        count++;
        message = message.obtain();
        update_data.clear();

        message.what = UnZipper.PROGRESS_UPDATE;
        update_data.putInt(UnZipper.DATA_KEY_PROGRESS, count);
        update_data.putString(UnZipper.DATA_KEY_FILENAME, fileName);

        message.setData(update_data);
        handler.sendMessage(message);
    }

    public void sentFileNameMsg(Handler handler, String fileName, int space) {
        if (count % space == 0) {
            sentFileNameMsg(handler, fileName);
        } else {
            count++;
        }
    }

    public void sentTotalFiles(Context context, Handler handler, String sourceName) throws IOException {
        InputStream inputStream = context.getAssets().open(sourceName);
        int count = UnZipper.countFiles(inputStream);

        Bundle bundle = new Bundle();
        Message message = new Message();
        message.what = UnZipper.FILE_SIZE;
        bundle.putInt(UnZipper.DATA_KEY_FILESIZE, count);
        bundle.putString(UnZipper.DATA_KEY_FILENAME, sourceName);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public static void progressDone(Handler handler) {
        DONE_COUNT++;
        Message m = new Message();
        m.what = UnZipper.PROGRESS_DONE;
        m.arg1 = DONE_COUNT;
        handler.sendMessage(m);
    }
}