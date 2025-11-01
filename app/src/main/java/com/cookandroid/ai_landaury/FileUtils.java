package com.cookandroid.ai_landaury;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    public static String getPath(Context context, Uri uri) {
        try {
            // Android 10(Q) 이상 → SAF 구조로 인해 _data 접근 불가 → 임시 복사 방식 사용
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                File tempFile = copyUriToTempFile(context, uri);
                return (tempFile != null) ? tempFile.getAbsolutePath() : null;
            }

            // 하위 버전은 기존 방식 유지
            String[] proj = {android.provider.MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                cursor.close();
                return path;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ Q 이상에서 URI를 캐시폴더로 복사해 실제 파일 경로 확보
    private static File copyUriToTempFile(Context context, Uri uri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            String fileName = getFileName(context, uri);
            if (fileName == null) fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";

            File tempDir = new File(context.getCacheDir(), "temp_images");
            if (!tempDir.exists()) tempDir.mkdirs();

            File file = new File(tempDir, fileName);
            InputStream inputStream = resolver.openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) result = uri.getLastPathSegment();
        return result;
    }
}
