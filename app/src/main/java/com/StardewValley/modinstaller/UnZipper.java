package com.StardewValley.modinstaller;

import android.content.Context;
import android.os.Handler;

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
    public static final int PROGRESS_UPDATE = 1;
    public static final int TOTAL_FILES = 2;
    public static void unzip(InputStream inputStream, String destDir, Handler handler) throws IOException {
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
        }
        zis.closeEntry();
        zis.close();
        inputStream.close();
    }

    public static void unzipToDocumentFile(Context context, InputStream inputStream, DocumentFile destDir, Handler handler) throws IOException {

        Stack<DocumentFile> DFstack = new Stack<>();
        Stack<String> nameStack = new Stack<>();
        byte[] buffer = new byte[1024 * 10];
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
        }
        zis.closeEntry();
        zis.close();
        inputStream.close();
    }

    public static int countFiles(InputStream inputStream) throws IOException {

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