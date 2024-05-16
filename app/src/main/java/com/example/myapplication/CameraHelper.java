package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import org.tensorflow.lite.Interpreter;

public class CameraHelper {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private final Context context;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    private FrameCallback frameCallback;
    private Interpreter interpreter;
    private TextureView textureView;

    public CameraHelper(Context context, Interpreter interpreter, TextureView textureView) {
        this.context = context;
        this.interpreter = interpreter;
        this.textureView = textureView;
    }

    public interface FrameCallback {
        void onFrameProcessed(String emotion);
    }

    public void setFrameCallback(FrameCallback callback) {
        this.frameCallback = callback;
    }

    public void openCamera() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                Size[] outputSizes = map.getOutputSizes(SurfaceTexture.class);
                Size size = outputSizes[0];

                imageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.YUV_420_888, 2);
                imageReader.setOnImageAvailableListener(reader -> {
                    Image image = reader.acquireLatestImage();
                    if (image != null) {
                        processImage(image);
                        image.close();
                    }
                }, null);

                cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        cameraDevice = camera;
                        createCameraPreviewSession();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        closeCamera();
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        closeCamera();
                    }
                }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(imageReader.getWidth(), imageReader.getHeight());
            Surface surface = new Surface(texture);

            CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }
                    cameraCaptureSession = session;
                    try {
                        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                        session.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e("CameraHelper", "Kamera önizleme oturumu yapılandırılamadı.");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    private void processImage(Image image) {
        Log.d("processImage", "Görüntü işleme başladı.");

        Bitmap bitmap = ImageUtil.imageToBitmap(image, context);
        if (bitmap != null) {
            Log.d("processImage", "Bitmap oluşturuldu: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            Bitmap resizedBitmap = ImageUtil.resizeBitmap(bitmap, ImageUtil.INPUT_IMAGE_WIDTH, ImageUtil.INPUT_IMAGE_HEIGHT);
            Log.d("processImage", "Bitmap yeniden boyutlandırıldı: " + resizedBitmap.getWidth() + "x" + resizedBitmap.getHeight());

            ByteBuffer inputBuffer = ImageUtil.bitmapToByteBuffer(resizedBitmap, ImageUtil.INPUT_IMAGE_WIDTH, ImageUtil.INPUT_IMAGE_HEIGHT);
            Log.d("processImage", "ByteBuffer oluşturuldu. Boyut: " + inputBuffer.capacity());

            inputBuffer.rewind();
            float[][] outputScores = new float[1][ImageUtil.NUM_CLASSES];

            if (interpreter != null) {
                interpreter.run(inputBuffer, outputScores);
                Log.d("processImage", "Model çalıştırıldı. Çıktılar: " + Arrays.toString(outputScores[0]));

                int maxIndex = 0;
                for (int i = 1; i < ImageUtil.NUM_CLASSES; i++) {
                    if (outputScores[0][i] > outputScores[0][maxIndex]) {
                        maxIndex = i;
                    }
                }

                String emotion = getEmotionLabel(maxIndex);
                Log.d("processImage", "Tespit edilen duygu: " + emotion);

                if (frameCallback != null) {
                    frameCallback.onFrameProcessed(emotion);
                }
            } else {
                Log.e("Interpreter Error", "Interpreter nesnesi null");
            }
        } else {
            Log.e("CameraHelper", "Bitmap oluşturulamadı.");
        }
    }

    private String getEmotionLabel(int index) {
        switch (index) {
            case 0: return "Kızgın";
            case 1: return "Tiksinti";
            case 2: return "Korkmuş";
            case 3: return "Mutlu";
            case 4: return "Nötr";
            case 5: return "Üzgün";
            case 6: return "Şaşırmış";
            default: return "Bilinmeyen";
        }
    }
}
