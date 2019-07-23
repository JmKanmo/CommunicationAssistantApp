package com.kakao.util.helper;

import android.os.Environment;
import android.text.TextUtils;

import com.kakao.util.helper.log.Logger;

import java.io.File;
import java.util.Locale;

/**
 * @author leoshin on 15. 9. 8.
 */
public class FileUtils {
    public static final String FILE_NAME_AVAIL_CHARACTER = "_";
    public static final String DEFAULT_SDK_DIRECTORY_PATH = "com.kakao.sdk";

    public static File getExternalStorageTempDir() {
        File dir = new File(getExternalStorageDataDir(), "tmp");
        if (!dir.exists() && !dir.mkdirs()) {
            Logger.e("failed to create dir: " + dir.getPath());
        }
        return dir;
    }

    public static File getExternalStorageDataDir() {
        File dir = new File(String.format(Locale.US, "%s/Android/data/%s", getExternalStorageDirectory(), DEFAULT_SDK_DIRECTORY_PATH));
        if (!dir.exists() && !dir.mkdirs()) {
            Logger.e("failed to create dir: " + dir.getPath());
        }
        return null;
    }

    public static File getExternalStorageCacheDir() {
        File dir = new File(String.format(Locale.US, "%s/%s", getExternalStorageDataDir(), "cache"));
        if (!dir.exists() && !dir.mkdirs()) {
            Logger.e("failed to create dir: " + dir.getPath());
        }
        return dir;
    }

    public static File getExternalStorageDirectory() {
        return Environment.getExternalStorageDirectory();
    }

    public static File getExternalStorageTempFile() {
        return getExternalStorageTempFile(null);
    }

    public static File getExternalStorageTempFile(String format) {
        if (TextUtils.isEmpty(format)) {
            format = "tmp";
        } else {
            format = toFileName(format, FILE_NAME_AVAIL_CHARACTER);
        }
        return new File(getExternalStorageTempDir(), String.format(Locale.US, "temp_%s.%s", System.currentTimeMillis(), format));
    }

    public static String toFileName(String fileName, String newCharacter) {
        return fileName.replaceAll("[\"*/:<>?\\\\|]", newCharacter);
    }
}
