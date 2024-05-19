package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import android.widget.Toast;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class CameraFragment extends Fragment {

    private long lastTriggeredTime = 0;
    private TextView emotionTextView;
    private Map<String, String> emotionUrls = new HashMap<>();
    private Interpreter interpreter;
    private TextureView textureView;
    private CameraHelper cameraHelper;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable checkEmotionRunnable;
    private long intervalMillis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        emotionTextView = view.findViewById(R.id.emotion_text_view);
        textureView = view.findViewById(R.id.texture_view);

        // Ayarları yükleme
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        emotionUrls.put("Mutlu", prefs.getString("happy_url", ""));
        emotionUrls.put("Üzgün", prefs.getString("sad_url", ""));
        intervalMillis = Integer.parseInt(prefs.getString("interval_minutes", "5")) * 60 * 1000;

        // Interpreter başlatma
        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (IOException e) {
            Toast.makeText(getContext(), "Model yüklenemedi", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // CameraHelper başlatma
        cameraHelper = CameraHelper.getInstance(getContext(), interpreter, textureView);
        cameraHelper.setFrameCallback(emotion -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTriggeredTime >= intervalMillis) {
                lastTriggeredTime = currentTime;
                String url = emotionUrls.get(emotion);
                if (url != null && !url.isEmpty()) {
                    openUrl(url);
                }
            }

            // Burada getActivity() null olup olmadığını kontrol edin
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> emotionTextView.setText("Tespit edilen duygu: " + emotion));
            }

            // Zamanlayıcıyı başlat
            startEmotionCheckTimer();
        });

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                cameraHelper.openCamera();
                startEmotionCheckTimer(); // Zamanlayıcıyı başlat
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                stopEmotionCheckTimer(); // Zamanlayıcıyı durdur
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
            cameraHelper.openCamera();
            startEmotionCheckTimer(); // Zamanlayıcıyı başlat
        } else {
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    cameraHelper.openCamera();
                    startEmotionCheckTimer(); // Zamanlayıcıyı başlat
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    stopEmotionCheckTimer(); // Zamanlayıcıyı durdur
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraHelper.closeCamera();
        stopEmotionCheckTimer(); // Zamanlayıcıyı durdur
    }

    private void startEmotionCheckTimer() {
        if (checkEmotionRunnable != null) {
            handler.removeCallbacks(checkEmotionRunnable);
        }
        checkEmotionRunnable = () -> {
            cameraHelper.openCamera();
        };
        handler.postDelayed(checkEmotionRunnable, intervalMillis);
    }

    private void stopEmotionCheckTimer() {
        if (checkEmotionRunnable != null) {
            handler.removeCallbacks(checkEmotionRunnable);
            checkEmotionRunnable = null;
        }
    }

    private void openUrl(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "Bu URL'yi açabilecek bir uygulama bulunamadı.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "URL geçerli değil", Toast.LENGTH_SHORT).show();
        }
    }

    private ByteBuffer loadModelFile() throws IOException {
        InputStream inputStream = getActivity().getAssets().open("fer13.96px.tflite");
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
}
