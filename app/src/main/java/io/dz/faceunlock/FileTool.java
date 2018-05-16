
package io.dz.faceunlock;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class FileTool {
    public static final int BUFSIZE = 8192;
    private static final String TAG = "FileTool";

    public FileTool() {
    }

    public static File getRootPath() {
        File path = null;
        if(sdCardIsAvailable()) {
            path = Environment.getExternalStorageDirectory();
        } else {
            path = Environment.getDataDirectory();
        }

        return path;
    }

    public static File getCecheFolder(Context context) {
        File folder = new File(context.getCacheDir(), "IMAGECACHE");
        if(!folder.exists()) {
            folder.mkdir();
        }

        return folder;
    }

    public static boolean isSDCardEnable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public static String getSDCardPath() {
        return !isSDCardEnable()?"sdcard unable!":Environment.getExternalStorageDirectory().getPath() + File.separator;
    }

    public static String getDataPath() {
        return !isSDCardEnable()?"sdcard unable!":Environment.getDataDirectory().getPath();
    }

    public static boolean sdCardIsAvailable() {
        if(Environment.getExternalStorageState().equals("mounted")) {
            File sd = new File(Environment.getExternalStorageDirectory().getPath());
            return sd.canWrite();
        } else {
            return false;
        }
    }

    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if(!file.exists()) {
            return flag;
        } else if(file.isFile()) {
            file.delete();
            return true;
        } else {
            File[] files = file.listFiles();

            for(int i = 0; i < files.length; ++i) {
                File exeFile = files[i];
                if(exeFile.isDirectory()) {
                    delAllFile(exeFile.getAbsolutePath());
                } else {
                    exeFile.delete();
                }
            }

            file.delete();
            return flag;
        }
    }

    public static boolean deleteFilesInDir(String dirPath) {
        return deleteFilesInDir(getFileByPath(dirPath));
    }

    public static boolean deleteFilesInDir(File dir) {
        if(dir == null) {
            return false;
        } else if(!dir.exists()) {
            return true;
        } else if(!dir.isDirectory()) {
            return false;
        } else {
            File[] files = dir.listFiles();
            if(files != null && files.length != 0) {
                File[] var2 = files;
                int var3 = files.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    File file = var2[var4];
                    if(file.isFile()) {
                        if(!deleteFile(file)) {
                            return false;
                        }
                    } else if(file.isDirectory() && !deleteDir(file)) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    public static boolean cleanInternalCache(Context context) {
        return deleteFilesInDir(context.getCacheDir());
    }

    public static boolean cleanInternalFiles(Context context) {
        return deleteFilesInDir(context.getFilesDir());
    }

    public static boolean cleanInternalDbs(Context context) {
        return deleteFilesInDir(context.getFilesDir().getParent() + File.separator + "databases");
    }

    public static boolean cleanInternalDbByName(Context context, String dbName) {
        return context.deleteDatabase(dbName);
    }

    public static boolean cleanInternalSP(Context context) {
        return deleteFilesInDir(context.getFilesDir().getParent() + File.separator + "shared_prefs");
    }

    public static boolean cleanExternalCache(Context context) {
        return isSDCardEnable() && deleteFilesInDir(context.getExternalCacheDir());
    }

    public static boolean cleanCustomCache(String dirPath) {
        return deleteFilesInDir(dirPath);
    }

    public static boolean cleanCustomCache(File dir) {
        return deleteFilesInDir(dir);
    }

    public static boolean copy(String srcFile, String destFile) {
        try {
            FileInputStream in = new FileInputStream(srcFile);
            FileOutputStream out = new FileOutputStream(destFile);
            byte[] bytes = new byte[1024];

            int c;
            while((c = in.read(bytes)) != -1) {
                out.write(bytes, 0, c);
            }

            in.close();
            out.flush();
            out.close();
            return true;
        } catch (Exception var6) {
            return false;
        }
    }

    public static void copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs();
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;

            for(int i = 0; i < file.length; ++i) {
                if(oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if(temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" + temp.getName().toString());
                    byte[] b = new byte[5120];

                    int len;
                    while((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }

                    output.flush();
                    output.close();
                    input.close();
                }

                if(temp.isDirectory()) {
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (NullPointerException var10) {
            ;
        } catch (Exception var11) {
            ;
        }

    }

    public static boolean renameFile(String resFilePath, String newFilePath) {
        File resFile = new File(resFilePath);
        File newFile = new File(newFilePath);
        return resFile.renameTo(newFile);
    }

    @SuppressLint({"NewApi"})
    public static long getSDCardAvailaleSize() {
        File path = getRootPath();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long availableBlocks;
        if(VERSION.SDK_INT >= 18) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = (long)stat.getBlockSize();
            availableBlocks = (long)stat.getAvailableBlocks();
        }

        return availableBlocks * blockSize;
    }

    @SuppressLint({"NewApi"})
    public static long getDirSize(String path) {
        StatFs stat = new StatFs(path);
        long blockSize;
        long availableBlocks;
        if(VERSION.SDK_INT >= 18) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = (long)stat.getBlockSize();
            availableBlocks = (long)stat.getAvailableBlocks();
        }

        return availableBlocks * blockSize;
    }

    public static long getFileAllSize(String path) {
        File file = new File(path);
        if(!file.exists()) {
            return 0L;
        } else if(!file.isDirectory()) {
            return file.length();
        } else {
            File[] childrens = file.listFiles();
            long size = 0L;
            File[] var5 = childrens;
            int var6 = childrens.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                File f = var5[var7];
                size += getFileAllSize(f.getPath());
            }

            return size;
        }
    }

    public static boolean initFile(String path) {
        boolean result = false;

        try {
            File file = new File(path);
            if(!file.exists()) {
                result = file.createNewFile();
            } else if(file.isDirectory()) {
                file.delete();
                result = file.createNewFile();
            } else if(file.exists()) {
                result = true;
            }
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        return result;
    }

    public static boolean initDirectory(String path) {
        boolean result = false;
        File file = new File(path);
        if(!file.exists()) {
            result = file.mkdir();
        } else if(!file.isDirectory()) {
            file.delete();
            result = file.mkdir();
        } else if(file.exists()) {
            result = true;
        }

        return result;
    }

    public static void copyFile(File from, File to) throws IOException {
        if(!from.exists()) {
            throw new IOException("The source file not exist: " + from.getAbsolutePath());
        } else {
            FileInputStream fis = new FileInputStream(from);

            try {
                copyFile((InputStream)fis, (File)to);
            } finally {
                fis.close();
            }

        }
    }

    public static long copyFile(InputStream from, File to) throws IOException {
        long totalBytes = 0L;
        FileOutputStream fos = new FileOutputStream(to, false);

        try {
            int len;
            for(byte[] data = new byte[1024]; (len = from.read(data)) > -1; totalBytes += (long)len) {
                fos.write(data, 0, len);
            }

            fos.flush();
            return totalBytes;
        } finally {
            fos.close();
        }
    }

    public static void saveFile(InputStream inputStream, String filePath) {
        try {
            OutputStream outputStream = new FileOutputStream(new File(filePath), false);
            byte[] buffer = new byte[1024];

            int len;
            while((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            outputStream.flush();
            outputStream.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }

    public static void saveFileUTF8(String path, String content, Boolean append) throws IOException {
        FileOutputStream fos = new FileOutputStream(path, append.booleanValue());
        Writer out = new OutputStreamWriter(fos, "UTF-8");
        out.write(content);
        out.flush();
        out.close();
        fos.flush();
        fos.close();
    }

    public static String getFileUTF8(String path) {
        String result = "";
        FileInputStream fin = null;

        try {
            fin = new FileInputStream(path);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            fin.close();
            result = new String(buffer, "UTF-8");
        } catch (Exception var5) {
            ;
        }

        return result;
    }

    public static Intent getFileIntent(String path, String mimeType) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(new File(path)), mimeType);
        return intent;
    }

    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if(!"mounted".equals(Environment.getExternalStorageState()) && Environment.isExternalStorageRemovable()) {
            cachePath = context.getCacheDir().getPath();
        } else {
            cachePath = context.getExternalCacheDir().getPath();
        }

        return cachePath;
    }

    public static String getDiskFileDir(Context context) {
        String cachePath = null;
        if(!"mounted".equals(Environment.getExternalStorageState()) && Environment.isExternalStorageRemovable()) {
            cachePath = context.getFilesDir().getPath();
        } else {
            cachePath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();
        }

        return cachePath;
    }

    public static void mergeFiles(Context context, File outFile, List<File> files) {
        FileChannel outChannel = null;

        try {
            outChannel = (new FileOutputStream(outFile)).getChannel();
            Iterator var4 = files.iterator();

            while(var4.hasNext()) {
                File f = (File)var4.next();
                FileChannel fc = (new FileInputStream(f)).getChannel();
                ByteBuffer bb = ByteBuffer.allocate(8192);

                while(fc.read(bb) != -1) {
                    bb.flip();
                    outChannel.write(bb);
                    bb.clear();
                }

                fc.close();
            }

            Log.d("FileTool", "拼接完成");
        } catch (IOException var16) {
            var16.printStackTrace();
        } finally {
            try {
                if(outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException var15) {
                ;
            }

        }

    }

    public static String getNativeM3u(Context context, File file, List<File> pathList) {
        InputStream in = null;
        int num = 0;
        StringBuffer buf = new StringBuffer();

        try {
            if(file != null) {
                in = new FileInputStream(file);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = "";

            while(true) {
                while((line = reader.readLine()) != null) {
                    if(line.length() > 0 && line.startsWith("http://")) {
                        buf.append("file:" + ((File)pathList.get(num)).getAbsolutePath() + "\r\n");
                        ++num;
                    } else {
                        buf.append(line + "\r\n");
                    }
                }

                in.close();
                write(file.getAbsolutePath(), buf.toString());
                Log.d("ts替换", "ts替换完成");
                break;
            }
        } catch (FileNotFoundException var8) {
            var8.printStackTrace();
        } catch (IOException var9) {
            var9.printStackTrace();
        }

        return buf.toString();
    }

    public static void write(String filePath, String content) {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(content);
        } catch (Exception var12) {
            var12.printStackTrace();
        } finally {
            if(bw != null) {
                try {
                    bw.close();
                } catch (IOException var11) {
                    bw = null;
                }
            }

        }

    }

    public static Vector<String> GetAllFileName(String fileAbsolutePath, String suffix) {
        Vector<String> vecFile = new Vector();
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();

        for(int iFileLength = 0; iFileLength < subFile.length; ++iFileLength) {
            if(!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                if(filename.trim().toLowerCase().endsWith(suffix)) {
                    vecFile.add(filename);
                }
            }
        }

        return vecFile;
    }

    public static File getFileByPath(String filePath) {
        return isNullString(filePath)?null:new File(filePath);
    }

    public static boolean isFileExists(String filePath) {
        return isFileExists(getFileByPath(filePath));
    }

    public static boolean isFileExists(File file) {
        return file != null && file.exists();
    }

    public static boolean isDir(String dirPath) {
        return isDir(getFileByPath(dirPath));
    }

    public static boolean isDir(File file) {
        return isFileExists(file) && file.isDirectory();
    }

    public static boolean isFile(String filePath) {
        return isFile(getFileByPath(filePath));
    }

    public static boolean isFile(File file) {
        return isFileExists(file) && file.isFile();
    }

    public static boolean createOrExistsDir(String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    public static boolean createOrExistsDir(File file) {
        boolean var10000;
        label25: {
            if(file != null) {
                if(file.exists()) {
                    if(file.isDirectory()) {
                        break label25;
                    }
                } else if(file.mkdirs()) {
                    break label25;
                }
            }

            var10000 = false;
            return var10000;
        }

        var10000 = true;
        return var10000;
    }

    public static boolean createOrExistsFile(String filePath) {
        return createOrExistsFile(getFileByPath(filePath));
    }

    public static boolean createOrExistsFile(File file) {
        if(file == null) {
            return false;
        } else if(file.exists()) {
            return file.isFile();
        } else if(!createOrExistsDir(file.getParentFile())) {
            return false;
        } else {
            try {
                return file.createNewFile();
            } catch (IOException var2) {
                var2.printStackTrace();
                return false;
            }
        }
    }

    public static boolean createFileByDeleteOldFile(String filePath) {
        return createFileByDeleteOldFile(getFileByPath(filePath));
    }

    public static boolean createFileByDeleteOldFile(File file) {
        if(file == null) {
            return false;
        } else if(file.exists() && file.isFile() && !file.delete()) {
            return false;
        } else if(!createOrExistsDir(file.getParentFile())) {
            return false;
        } else {
            try {
                return file.createNewFile();
            } catch (IOException var2) {
                var2.printStackTrace();
                return false;
            }
        }
    }

    public static boolean copyOrMoveDir(String srcDirPath, String destDirPath, boolean isMove) {
        return copyOrMoveDir(getFileByPath(srcDirPath), getFileByPath(destDirPath), isMove);
    }

    public static boolean copyOrMoveDir(File srcDir, File destDir, boolean isMove) {
        if(srcDir != null && destDir != null) {
            String srcPath = srcDir.getPath() + File.separator;
            String destPath = destDir.getPath() + File.separator;
            if(destPath.contains(srcPath)) {
                return false;
            } else if(srcDir.exists() && srcDir.isDirectory()) {
                if(!createOrExistsDir(destDir)) {
                    return false;
                } else {
                    File[] files = srcDir.listFiles();
                    File[] var6 = files;
                    int var7 = files.length;

                    for(int var8 = 0; var8 < var7; ++var8) {
                        File file = var6[var8];
                        File oneDestFile = new File(destPath + file.getName());
                        if(file.isFile()) {
                            if(!copyOrMoveFile(file, oneDestFile, isMove)) {
                                return false;
                            }
                        } else if(file.isDirectory() && !copyOrMoveDir(file, oneDestFile, isMove)) {
                            return false;
                        }
                    }

                    return !isMove || deleteDir(srcDir);
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean copyOrMoveFile(String srcFilePath, String destFilePath, boolean isMove) {
        return copyOrMoveFile(getFileByPath(srcFilePath), getFileByPath(destFilePath), isMove);
    }

    public static boolean copyOrMoveFile(File srcFile, File destFile, boolean isMove) {
        if(srcFile != null && destFile != null) {
            if(srcFile.exists() && srcFile.isFile()) {
                if(destFile.exists() && destFile.isFile()) {
                    return false;
                } else if(!createOrExistsDir(destFile.getParentFile())) {
                    return false;
                } else {
                    try {
                        return writeFileFromIS((File)destFile, new FileInputStream(srcFile), false) && (!isMove || deleteFile(srcFile));
                    } catch (FileNotFoundException var4) {
                        var4.printStackTrace();
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean copyDir(String srcDirPath, String destDirPath) {
        return copyDir(getFileByPath(srcDirPath), getFileByPath(destDirPath));
    }

    public static boolean copyDir(File srcDir, File destDir) {
        return copyOrMoveDir(srcDir, destDir, false);
    }

    public static boolean copyFile(String srcFilePath, String destFilePath) {
        return copyFile(getFileByPath(srcFilePath), getFileByPath(destFilePath), false);
    }

    public static boolean copyFile(File srcFile, File destFile, boolean isCopy) {
        return copyOrMoveFile(srcFile, destFile, false);
    }

    public static boolean moveDir(String srcDirPath, String destDirPath) {
        return moveDir(getFileByPath(srcDirPath), getFileByPath(destDirPath));
    }

    public static boolean moveDir(File srcDir, File destDir) {
        return copyOrMoveDir(srcDir, destDir, true);
    }

    public static boolean moveFile(String srcFilePath, String destFilePath) {
        return moveFile(getFileByPath(srcFilePath), getFileByPath(destFilePath));
    }

    public static boolean moveFile(File srcFile, File destFile) {
        return copyOrMoveFile(srcFile, destFile, true);
    }

    public static boolean deleteDir(String dirPath) {
        return deleteDir(getFileByPath(dirPath));
    }

    public static boolean deleteDir(File dir) {
        if(dir == null) {
            return false;
        } else if(!dir.exists()) {
            return true;
        } else if(!dir.isDirectory()) {
            return false;
        } else {
            File[] files = dir.listFiles();
            File[] var2 = files;
            int var3 = files.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                File file = var2[var4];
                if(file.isFile()) {
                    if(!deleteFile(file)) {
                        return false;
                    }
                } else if(file.isDirectory() && !deleteDir(file)) {
                    return false;
                }
            }

            return dir.delete();
        }
    }

    public static boolean deleteFile(String srcFilePath) {
        return deleteFile(getFileByPath(srcFilePath));
    }

    public static boolean deleteFile(File file) {
        return file != null && (!file.exists() || file.isFile() && file.delete());
    }

    public static List<File> listFilesInDir(String dirPath, boolean isRecursive) {
        return listFilesInDir(getFileByPath(dirPath), isRecursive);
    }

    public static List<File> listFilesInDir(File dir, boolean isRecursive) {
        if(isRecursive) {
            return listFilesInDir(dir);
        } else if(dir != null && isDir(dir)) {
            List<File> list = new ArrayList();
            Collections.addAll(list, dir.listFiles());
            return list;
        } else {
            return null;
        }
    }

    public static List<File> listFilesInDir(String dirPath) {
        return listFilesInDir(getFileByPath(dirPath));
    }

    public static List<File> listFilesInDir(File dir) {
        if(dir != null && isDir(dir)) {
            List<File> list = new ArrayList();
            File[] files = dir.listFiles();
            File[] var3 = files;
            int var4 = files.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                File file = var3[var5];
                list.add(file);
                if(file.isDirectory()) {
                    list.addAll(listFilesInDir(file));
                }
            }

            return list;
        } else {
            return null;
        }
    }

    public static List<File> listFilesInDirWithFilter(String dirPath, String suffix, boolean isRecursive) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), suffix, isRecursive);
    }

    public static List<File> listFilesInDirWithFilter(File dir, String suffix, boolean isRecursive) {
        if(isRecursive) {
            return listFilesInDirWithFilter(dir, suffix);
        } else if(dir != null && isDir(dir)) {
            List<File> list = new ArrayList();
            File[] files = dir.listFiles();
            File[] var5 = files;
            int var6 = files.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                File file = var5[var7];
                if(file.getName().toUpperCase().endsWith(suffix.toUpperCase())) {
                    list.add(file);
                }
            }

            return list;
        } else {
            return null;
        }
    }

    public static List<File> listFilesInDirWithFilter(String dirPath, String suffix) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), suffix);
    }

    public static List<File> listFilesInDirWithFilter(File dir, String suffix) {
        if(dir != null && isDir(dir)) {
            List<File> list = new ArrayList();
            File[] files = dir.listFiles();
            File[] var4 = files;
            int var5 = files.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                File file = var4[var6];
                if(file.getName().toUpperCase().endsWith(suffix.toUpperCase())) {
                    list.add(file);
                }

                if(file.isDirectory()) {
                    list.addAll(listFilesInDirWithFilter(file, suffix));
                }
            }

            return list;
        } else {
            return null;
        }
    }

    public static List<File> listFilesInDirWithFilter(String dirPath, FilenameFilter filter, boolean isRecursive) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), filter, isRecursive);
    }

    public static List<File> listFilesInDirWithFilter(File dir, FilenameFilter filter, boolean isRecursive) {
        if(isRecursive) {
            return listFilesInDirWithFilter(dir, filter);
        } else if(dir != null && isDir(dir)) {
            List<File> list = new ArrayList();
            File[] files = dir.listFiles();
            File[] var5 = files;
            int var6 = files.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                File file = var5[var7];
                if(filter.accept(file.getParentFile(), file.getName())) {
                    list.add(file);
                }
            }

            return list;
        } else {
            return null;
        }
    }

    public static List<File> listFilesInDirWithFilter(String dirPath, FilenameFilter filter) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), filter);
    }

    public static List<File> listFilesInDirWithFilter(File dir, FilenameFilter filter) {
        if(dir != null && isDir(dir)) {
            List<File> list = new ArrayList();
            File[] files = dir.listFiles();
            File[] var4 = files;
            int var5 = files.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                File file = var4[var6];
                if(filter.accept(file.getParentFile(), file.getName())) {
                    list.add(file);
                }

                if(file.isDirectory()) {
                    list.addAll(listFilesInDirWithFilter(file, filter));
                }
            }

            return list;
        } else {
            return null;
        }
    }

    public static List<File> searchFileInDir(String dirPath, String fileName) {
        return searchFileInDir(getFileByPath(dirPath), fileName);
    }

    public static List<File> searchFileInDir(File dir, String fileName) {
        if(dir != null && isDir(dir)) {
            List<File> list = new ArrayList();
            File[] files = dir.listFiles();
            File[] var4 = files;
            int var5 = files.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                File file = var4[var6];
                if(file.getName().toUpperCase().equals(fileName.toUpperCase())) {
                    list.add(file);
                }

                if(file.isDirectory()) {
                    list.addAll(listFilesInDirWithFilter(file, fileName));
                }
            }

            return list;
        } else {
            return null;
        }
    }

    public static boolean writeFileFromIS(String filePath, InputStream is, boolean append) {
        return writeFileFromIS(getFileByPath(filePath), is, append);
    }

    public static boolean writeFileFromIS(File file, InputStream is, boolean append) {
        if(file != null && is != null) {
            if(!createOrExistsFile(file)) {
                return false;
            } else {
                BufferedOutputStream os = null;

                boolean var5;
                try {
                    os = new BufferedOutputStream(new FileOutputStream(file, append));
                    byte[] data = new byte[1024];

                    int len;
                    while((len = is.read(data, 0, 1024)) != -1) {
                        os.write(data, 0, len);
                    }

                    boolean var6 = true;
                    return var6;
                } catch (IOException var10) {
                    var10.printStackTrace();
                    var5 = false;
                } finally {
                    closeIO(new Closeable[]{is, os});
                }

                return var5;
            }
        } else {
            return false;
        }
    }

    public static boolean writeFileFromString(String filePath, String content, boolean append) {
        return writeFileFromString(getFileByPath(filePath), content, append);
    }

    public static boolean writeFileFromString(File file, String content, boolean append) {
        if(file != null && content != null) {
            if(!createOrExistsFile(file)) {
                return false;
            } else {
                FileWriter fileWriter = null;

                boolean var5;
                try {
                    fileWriter = new FileWriter(file, append);
                    fileWriter.write(content);
                    boolean var4 = true;
                    return var4;
                } catch (IOException var9) {
                    var9.printStackTrace();
                    var5 = false;
                } finally {
                    closeIO(new Closeable[]{fileWriter});
                }

                return var5;
            }
        } else {
            return false;
        }
    }

    public static String readFile2String(String filePath, String charsetName) {
        return readFile2String(getFileByPath(filePath), charsetName);
    }

    public static String readFile2String(File file, String charsetName) {
        if(file == null) {
            return null;
        } else {
            BufferedReader reader = null;

            String line;
            try {
                StringBuilder sb = new StringBuilder();
                if(isNullString(charsetName)) {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                } else {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
                }

                while((line = reader.readLine()) != null) {
                    sb.append(line).append("\r\n");
                }

                String var5 = sb.delete(sb.length() - 2, sb.length()).toString();
                return var5;
            } catch (IOException var9) {
                var9.printStackTrace();
                line = null;
            } finally {
                closeIO(new Closeable[]{reader});
            }

            return line;
        }
    }

    public static String getFileCharsetSimple(String filePath) {
        return getFileCharsetSimple(getFileByPath(filePath));
    }

    public static String getFileCharsetSimple(File file) {
        int p = 0;
        BufferedInputStream is = null;

        try {
            is = new BufferedInputStream(new FileInputStream(file));
            p = (is.read() << 8) + is.read();
        } catch (IOException var7) {
            var7.printStackTrace();
        } finally {
            closeIO(new Closeable[]{is});
        }

        switch(p) {
        case 61371:
            return "UTF-8";
        case 65279:
            return "UTF-16BE";
        case 65534:
            return "Unicode";
        default:
            return "GBK";
        }
    }

    public static int getFileLines(String filePath) {
        return getFileLines(getFileByPath(filePath));
    }

    public static int getFileLines(File file) {
        int count = 1;
        BufferedInputStream is = null;

        try {
            is = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];

            int readChars;
            while((readChars = is.read(buffer, 0, 1024)) != -1) {
                for(int i = 0; i < readChars; ++i) {
                    if(buffer[i] == 10) {
                        ++count;
                    }
                }
            }
        } catch (IOException var9) {
            var9.printStackTrace();
        } finally {
            closeIO(new Closeable[]{is});
        }

        return count;
    }

    public static void closeIO(Closeable... closeables) {
        if(closeables != null) {
            try {
                Closeable[] var1 = closeables;
                int var2 = closeables.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    Closeable closeable = var1[var3];
                    if(closeable != null) {
                        closeable.close();
                    }
                }
            } catch (IOException var5) {
                var5.printStackTrace();
            }

        }
    }

    public static String getDirName(File file) {
        return file == null?null:getDirName(file.getPath());
    }

    public static String getDirName(String filePath) {
        if(isNullString(filePath)) {
            return filePath;
        } else {
            int lastSep = filePath.lastIndexOf(File.separator);
            return lastSep == -1?"":filePath.substring(0, lastSep + 1);
        }
    }

    public static String getFileName(File file) {
        return file == null?null:getFileName(file.getPath());
    }

    public static String getFileName(String filePath) {
        if(isNullString(filePath)) {
            return filePath;
        } else {
            int lastSep = filePath.lastIndexOf(File.separator);
            return lastSep == -1?filePath:filePath.substring(lastSep + 1);
        }
    }

    public static String getFileNameNoExtension(File file) {
        return file == null?null:getFileNameNoExtension(file.getPath());
    }

    public static String getFileNameNoExtension(String filePath) {
        if(isNullString(filePath)) {
            return filePath;
        } else {
            int lastPoi = filePath.lastIndexOf(46);
            int lastSep = filePath.lastIndexOf(File.separator);
            return lastSep == -1?(lastPoi == -1?filePath:filePath.substring(0, lastPoi)):(lastPoi != -1 && lastSep <= lastPoi?filePath.substring(lastSep + 1, lastPoi):filePath.substring(lastSep + 1));
        }
    }

    public static String getFileExtension(File file) {
        return file == null?null:getFileExtension(file.getPath());
    }

    public static String getFileExtension(String filePath) {
        if(isNullString(filePath)) {
            return filePath;
        } else {
            int lastPoi = filePath.lastIndexOf(46);
            int lastSep = filePath.lastIndexOf(File.separator);
            return lastPoi != -1 && lastSep < lastPoi?filePath.substring(lastPoi):"";
        }
    }

    public static Uri getUriForFile(Context mContext, File file) {
        Uri fileUri = null;
        if(VERSION.SDK_INT >= 24) {
            fileUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", file);
        } else {
            fileUri = Uri.fromFile(file);
        }

        return fileUri;
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{"_id"}, "_data=? ", new String[]{filePath}, (String)null);
        if(cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else if(imageFile.exists()) {
            ContentValues values = new ContentValues();
            values.put("_data", filePath);
            return context.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return null;
        }
    }

    @TargetApi(19)
    public static String getPathFromUri(Context context, Uri uri) {
        boolean isKitKat = VERSION.SDK_INT >= 19;
        if(isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            String docId;
            String[] split;
            String type;
            if(isExternalStorageDocument(uri)) {
                docId = DocumentsContract.getDocumentId(uri);
                split = docId.split(":");
                type = split[0];
                if("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else {
                if(isDownloadsDocument(uri)) {
                    docId = DocumentsContract.getDocumentId(uri);
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId).longValue());
                    return getDataColumn(context, contentUri, (String)null, (String[])null);
                }

                if(isMediaDocument(uri)) {
                    docId = DocumentsContract.getDocumentId(uri);
                    split = docId.split(":");
                    type = split[0];
                    Uri contentUri = null;
                    if("image".equals(type)) {
                        contentUri = Media.EXTERNAL_CONTENT_URI;
                    } else if("video".equals(type)) {
                        contentUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if("audio".equals(type)) {
                        contentUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, "_id=?", selectionArgs);
                }
            }
        } else {
            if("content".equalsIgnoreCase(uri.getScheme())) {
                if(isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }

                return getDataColumn(context, uri, (String)null, (String[])null);
            }

            if("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return "";
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = new String[]{column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, (String)null);
            if(cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                String var8 = cursor.getString(index);
                return var8;
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }

        }

        return null;
    }

    public static void closeIOQuietly(Closeable... closeables) {
        if(closeables != null) {
            Closeable[] var1 = closeables;
            int var2 = closeables.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                Closeable closeable = var1[var3];
                if(closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException var6) {
                        ;
                    }
                }
            }

        }
    }

    public static String file2Base64(String filePath) {
        FileInputStream fis = null;
        String base64String = "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            fis = new FileInputStream(filePath);
            byte[] buffer = new byte[102400];
            boolean var5 = false;

            int count;
            while((count = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, count);
            }

            fis.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        base64String = Base64.encodeToString(bos.toByteArray(), 0);
        return base64String;
    }

    public void TextToFile(String strFilePath, String strBuffer) {
        FileWriter fileWriter = null;

        try {
            File fileText = new File(strFilePath);
            fileWriter = new FileWriter(fileText);
            fileWriter.write(strBuffer);
        } catch (IOException var13) {
            var13.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException var12) {
                var12.printStackTrace();
            }

        }

    }

    public void readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;

        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;

            for(int line = 1; (tempString = reader.readLine()) != null; ++line) {
                System.out.println("line?????????????????????????????????? " + line + ": " + tempString);
            }

            reader.close();
        } catch (IOException var15) {
            var15.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException var14) {
                    ;
                }
            }

        }

    }

    public static boolean isNullString(@Nullable String str) {
        return str == null || str.length() == 0 || "".equals(str) || "null".equals(str);
    }
}
