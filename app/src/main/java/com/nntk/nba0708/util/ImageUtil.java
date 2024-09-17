package com.nntk.nba0708.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ImageUtil {
    private Bitmap flipImageHorizontally(Bitmap image) {
        Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        matrix.postTranslate(image.getWidth(), 0);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }
}
