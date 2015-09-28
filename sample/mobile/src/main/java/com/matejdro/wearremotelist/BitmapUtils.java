package com.matejdro.wearremotelist;

import android.graphics.Bitmap;

public class BitmapUtils
{
    public static Bitmap resizePreservingRatio(Bitmap original, int newWidth, int newHeight)
    {
        if (original == null)
            return null;

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        if (newWidth / (float) originalWidth < newHeight / (float) originalHeight)
        {
            newHeight = originalHeight * newWidth / originalWidth;
        }
        else
        {
            newWidth = originalWidth * newHeight / originalHeight;
        }

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
}
