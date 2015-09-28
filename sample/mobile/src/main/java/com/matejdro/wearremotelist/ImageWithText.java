package com.matejdro.wearremotelist;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public class ImageWithText implements Comparable<ImageWithText>
{
    private Bitmap image;
    private String text;

    public ImageWithText(Bitmap image, String text)
    {
        this.image = BitmapUtils.resizePreservingRatio(image, 50, 50);
        this.text = text;
    }

    public Bitmap getImage()
    {
        return image;
    }

    public String getText()
    {
        return text;
    }


    @Override
    public int compareTo(@NonNull ImageWithText another)
    {
        return this.text.compareTo(another.text);
    }
}
