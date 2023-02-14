package com.jerry.rt.jva.utils;

import java.io.*;


public class FileUtil {
    public static File getParent(File file, int level) throws IOException {
        if (level < 1 || null == file) {
            return file;
        }

        File parentFile;
        try {
            parentFile = file.getCanonicalFile().getParentFile();
        } catch (IOException e) {
            throw new IOException(e);
        }
        if (1 == level) {
            return parentFile;
        }
        return getParent(parentFile, level - 1);
    }

    public static File mkParentDirs(File file) throws IOException {
        if (null == file) {
            return null;
        }
        return mkdir(getParent(file, 1));
    }

    public static File touch(File file) throws IOException {
        if (null == file) {
            return null;
        }
        if (false == file.exists()) {
            mkParentDirs(file);
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return file;
    }

    public static BufferedOutputStream getOutputStream(File file) throws IOException {
        final OutputStream out;
        try {
            out = new FileOutputStream(touch(file));
        } catch (IOException e) {
            throw new IOException(e);
        }
        return IoUtil.toBuffered(out);
    }

    public static int lastIndexOfSeparator(String filePath) {
        if (StrUtil.isNotEmpty(filePath)) {
            int i = filePath.length();
            char c;
            while (--i >= 0) {
                c = filePath.charAt(i);
                if (CharUtil.isFileSeparator(c)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static File createTempFile(String prefix, String suffix, File dir, boolean isReCreat) throws IOException {
        int exceptionsCount = 0;
        while (true) {
            try {
                File file = File.createTempFile(prefix, suffix, mkdir(dir)).getCanonicalFile();
                if (isReCreat) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                }
                return file;
            } catch (IOException ioex) { // fixes java.io.WinNTFileSystem.createFileExclusively access denied
                if (++exceptionsCount >= 50) {
                    throw new IOException(ioex);
                }
            }
        }
    }

    public static File mkdir(File dir) {
        if (dir == null) {
            return null;
        }
        if (false == dir.exists()) {
            mkdirsSafely(dir, 5, 1);
        }
        return dir;
    }

    public static boolean mkdirsSafely(File dir, int tryCount, long sleepMillis) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            return true;
        }
        for (int i = 1; i <= tryCount; i++) { // 高并发场景下，可以看到 i 处于 1 ~ 3 之间
            // 如果文件已存在，也会返回 false，所以该值不能作为是否能创建的依据，因此不对其进行处理
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            if (dir.exists()) {
                return true;
            }
            sleep(sleepMillis);
        }
        return dir.exists();
    }



    private static boolean sleep(long millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }
}
