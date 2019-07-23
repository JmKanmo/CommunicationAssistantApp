package com.kakao.util.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.kakao.util.helper.log.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author leoshin on 15. 9. 8.
 */
public class MediaUtils {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private static final String[] IMAGE_FILE_COLUMNS = {
            MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION
    };

    public static String getImageFilePathFromUri(Uri uri, Context context) throws FileNotFoundException {
        if (uri == null) {
            throw new FileNotFoundException("uri is null");
        }

        if (context == null) {
            throw new FileNotFoundException("context is null.");
        }

        if ("file".equals(uri.getScheme())) {
            return uri.getPath();
        }

        Cursor cursor = null;
        String filePath = null;
        try {
            cursor = context.getApplicationContext().getContentResolver().query(uri, IMAGE_FILE_COLUMNS, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(IMAGE_FILE_COLUMNS[0]);
                filePath = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Logger.w(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (TextUtils.isEmpty(filePath) || "content".equals(uri.getScheme()) || filePath.startsWith("http")) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = context.getApplicationContext().getContentResolver().openInputStream(uri);
                File output = FileUtils.getExternalStorageTempFile();
                out = new FileOutputStream(output);
                copy(in, out);
                filePath = output.getAbsolutePath();
            } catch (Exception e) {
                Logger.w("cannot make a file", e);
            } finally {
                closeQuietly(in);
                closeQuietly(out);
            }
        }

        if (filePath == null) {
            filePath = uri.getPath();
        }

        if (null == filePath) {
            throw new FileNotFoundException("filePath is null");
        }

        Logger.d(">>> getImageFilePathFromUri - filePath : " + filePath);
        return filePath;
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void closeQuietly(InputStream input) {
        closeQuietly((Closeable)input);
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
}
