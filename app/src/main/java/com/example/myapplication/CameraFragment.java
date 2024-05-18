package com.example.myapplication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

public class CameraFragment extends Fragment {

    private CameraHelper cameraHelper;
    private Interpreter interpreter;
    private TextureView textureView;
    private TextView emotionTextView;
    private static final String TAG = "CameraFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        textureView = view.findViewById(R.id.texture_view);
        emotionTextView = view.findViewById(R.id.emotion_text_view);

        // CameraHelper singleton'ını al ve ayarla
        cameraHelper = CameraHelper.getInstance(getContext());

        // MainActivity üzerinden Interpreter'ı al ve CameraHelper'ı ayarla
        interpreter = ((MainActivity) getActivity()).getInterpreter();
        cameraHelper.setInterpreterAndTextureView(interpreter, textureView);

        // Kamera başlatma işlemleri
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                cameraHelper.openCamera();
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

        // Kamera Helper'da FrameCallback ayarlama
        cameraHelper.setFrameCallback(emotion -> getActivity().runOnUiThread(() -> {
            emotionTextView.setText("Tespit edilen duygu: " + emotion);
        }));

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraHelper.closeCamera();
    }
}
