package com.example.eatnow.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.nio.charset.StandardCharsets;

// Class to deal image bytes / strings
public final class ImageConverter {

    public static byte[] convertASCIItoBinary(byte[] ASCII) {
        return Base64.decode(ASCII, Base64.DEFAULT);
    }

    public static Bitmap convertASCIItoBitmap(byte[] ASCII) {
        byte[] decodedBinary = convertASCIItoBinary(ASCII);
        return BitmapFactory.decodeByteArray(decodedBinary, 0, decodedBinary.length);
    }

    public static byte[] convertStringtoASCII(String image) {
        return image.getBytes(StandardCharsets.UTF_8);
    }

    public static String convertBinaryToASCIIString(byte[] target) {
        byte[] ASCII = convertBinaryToASCII(target);
        return new String(ASCII, StandardCharsets.UTF_8);
    }

    public static byte[] convertBinaryToASCII(byte[] target) {
        return Base64.encode(target, Base64.DEFAULT);
    }
}
