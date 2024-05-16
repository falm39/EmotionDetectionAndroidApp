package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.Image;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageUtil {

    static final int INPUT_IMAGE_WIDTH = 96;
    static final int INPUT_IMAGE_HEIGHT = 96;
    static final int NUM_CLASSES = 7;

    public static Bitmap imageToBitmap(Image image, Context context) {
        if (image == null) return null;

        // YUV_420_888 formatında görüntüyü bitmap'e dönüştürme
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer(); // Y
        ByteBuffer uBuffer = planes[1].getBuffer(); // U
        ByteBuffer vBuffer = planes[2].getBuffer(); // V

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // Y verilerini NV21 array'ine kopyalama
        yBuffer.get(nv21, 0, ySize);

        // U ve V verilerini NV21 array'ine kopyalama
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);

        // YUV_420_888 -> ARGB_8888 dönüşümü
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Allocation in = Allocation.createSized(rs, Element.U8(rs), nv21.length);
        Allocation out = Allocation.createFromBitmap(rs, bitmap);

        in.copyFrom(nv21);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        out.copyTo(bitmap);

        return bitmap;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public static ByteBuffer bitmapToByteBuffer(Bitmap bitmap, int width, int height) {
        Log.d("bitmapToByteBuffer", "Bitmap ByteBuffer'a dönüştürülüyor.");

        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4); // Gri tonlama için 4 byte per pixel
        buffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int pixel : pixels) {
            int grey = (int) (0.299 * ((pixel >> 16) & 0xFF) + 0.587 * ((pixel >> 8) & 0xFF) + 0.114 * (pixel & 0xFF));
            buffer.putFloat(grey / 255.0f); // Normalize edilmiş gri değer
        }

        return buffer;
    }

}
