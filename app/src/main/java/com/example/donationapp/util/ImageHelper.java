package com.example.donationapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for image picking, compression, and rotation handling
 */
public class ImageHelper {
    private static final String TAG = "ImageHelper";
    private static final int MAX_IMAGE_SIZE = 1024; // Max width/height in pixels
    private static final int COMPRESSION_QUALITY = 85; // JPEG quality (0-100)
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB max file size

    /**
     * Compress image to reduce file size before upload
     * @param imageBytes Original image bytes
     * @return Compressed image bytes
     */
    public static byte[] compressImage(byte[] imageBytes) {
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode image");
                return imageBytes;
            }

            // Fix rotation if needed
            bitmap = fixImageRotation(bitmap, imageBytes);

            // Resize if too large
            bitmap = resizeImage(bitmap, MAX_IMAGE_SIZE);

            // Compress to JPEG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int quality = COMPRESSION_QUALITY;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

            // If still too large, reduce quality
            byte[] compressedBytes = outputStream.toByteArray();
            while (compressedBytes.length > MAX_FILE_SIZE && quality > 50) {
                quality -= 10;
                outputStream.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                compressedBytes = outputStream.toByteArray();
            }

            bitmap.recycle();
            Log.d(TAG, "Image compressed: " + imageBytes.length + " -> " + compressedBytes.length);
            return compressedBytes;
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return imageBytes;
        }
    }

    /**
     * Resize image if it exceeds max size
     */
    private static Bitmap resizeImage(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        if (resized != bitmap) {
            bitmap.recycle();
        }
        return resized;
    }

    /**
     * Fix image rotation based on EXIF data
     */
    private static Bitmap fixImageRotation(Bitmap bitmap, byte[] imageBytes) {
        try {
            ExifInterface exif = new ExifInterface(new java.io.ByteArrayInputStream(imageBytes));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.postScale(1, -1);
                    break;
                default:
                    return bitmap;
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }
            return rotatedBitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error fixing image rotation", e);
            return bitmap;
        }
    }

    /**
     * Convert Uri to byte array
     */
    public static byte[] uriToByteArray(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "Error reading image from URI", e);
            return null;
        }
    }

    /**
     * Create a temporary file for camera capture
     */
    public static File createImageFile(Context context) throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return imageFile;
    }

    /**
     * Get FileProvider URI for camera capture
     */
    public static Uri getFileProviderUri(Context context, File imageFile) {
        return FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider",
                imageFile);
    }
}

