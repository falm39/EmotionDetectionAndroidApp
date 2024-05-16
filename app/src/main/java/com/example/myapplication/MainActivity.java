package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.tensorflow.lite.Interpreter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.InputStream;
import java.nio.ByteOrder;
import android.content.res.AssetManager;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private TextView emotionText;
    private Interpreter interpreter;
    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        emotionText = findViewById(R.id.emotion_text);
        textureView = findViewById(R.id.texture_view);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                initializeInterpreter();
                initializeCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
        });
    }

    private void initializeInterpreter() {
        try {
            interpreter = new Interpreter(loadModelFile());
            Log.d("TFLite", "TensorFlow Lite modeli başarıyla yüklendi.");
        } catch (IOException e) {
            Log.e("Interpreter Error", "Model yüklenirken hata oluştu", e);
            interpreter = null;
        }
    }

    private void initializeCamera() {
        if (interpreter != null) {
            CameraHelper cameraHelper = new CameraHelper(this, interpreter, textureView);
            cameraHelper.setFrameCallback(emotion -> runOnUiThread(() -> {
                emotionText.setText("Tespit edilen duygu: " + emotion);
            }));
            cameraHelper.openCamera();
        } else {
            Log.e("Interpreter Error", "Interpreter nesnesi null");
        }
    }

    @NonNull
    private ByteBuffer loadModelFile() throws IOException {
        AssetManager assetManager = getAssets();
        InputStream inputStream = assetManager.open("fer13.96px.tflite");
        byte[] modelBytes = new byte[inputStream.available()];
        int bytesRead = inputStream.read(modelBytes);
        if (bytesRead != modelBytes.length) {
            throw new IOException("Tüm model dosyası okunamadı.");
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(modelBytes.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(modelBytes);
        return buffer;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (textureView.isAvailable()) {
                    initializeInterpreter();
                    initializeCamera();
                }
            } else {
                Log.e("Camera", "Kamera izni reddedildi.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (interpreter != null) {
            interpreter.close();
        }
    }
}
