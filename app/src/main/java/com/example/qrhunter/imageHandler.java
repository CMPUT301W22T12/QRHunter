package com.example.qrhunter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Matrix;

import android.media.Image;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public class imageHandler {

    public imageHandler(){

    }

    public Bitmap convertImageToBitmap(@NonNull Image image){
        //convert Image to Bitmap
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        return bitmapImage;
    }

    public Bitmap rotateBitmapIfNeeded(Bitmap image, int orientation){
        if(orientation != 0) {
            return rotateBitmap(image, orientation);
        }
        return image;
    }

    private Bitmap rotateBitmap(Bitmap image, int rotationVal){
        Matrix matrix = new Matrix();
        matrix.postRotate((float) rotationVal);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }
}
