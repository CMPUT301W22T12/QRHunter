package com.example.qrhunter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Matrix;

import android.media.Image;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * The imageHandler class is a class designed to allow for handling of images taken by the user and saved
 * This class allows for converting to a bitmap and correctly rotating the image.
 */
public class imageHandler {

    public imageHandler(){ }

    /**
     * This function will convert and Image object into a Bitmap object
     * @param image the Image object to convert to Bitmap
     * @return the Bitmap object created
     */
    public Bitmap convertImageToBitmap(@NonNull Image image){
        //convert Image to Bitmap
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        return bitmapImage;
    }

    /**
     * This function will take an image, and the image orientation, and if the image is not portrait rotate it as needed
     * @param image the Bitmap image seeking rotation
     * @param orientation the Orientation value of the image
     * @return the correctly rotated image
     */
    public Bitmap rotateBitmapIfNeeded(Bitmap image, int orientation){
        if(orientation != 0) {
            return rotateBitmap(image, orientation);
        }
        return image;
    }

    /**
     * Function that executes image rotation for rotateBitmapIfNeeded()
     * @param image bitmap Image rotating
     * @param rotationVal amount of rotation needed
     * @return rotated bitmap image
     */
    private Bitmap rotateBitmap(Bitmap image, int rotationVal){
        Matrix matrix = new Matrix();
        matrix.postRotate((float) rotationVal);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }
}
