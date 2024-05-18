package com.example.myapplication;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.tensorflow.lite.Interpreter;
import android.view.TextureView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private CameraHelper cameraHelper;
    private Interpreter interpreter;
    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.texture_view);
        cameraHelper = CameraHelper.getInstance(this);

        // TensorFlow Lite model dosyasını yüklemek için gerekli işlemleri gerçekleştirin
        try {
            interpreter = new Interpreter(loadModelFile());
            cameraHelper.setInterpreterAndTextureView(interpreter, textureView);
        } catch (IOException e) {
            Log.e(TAG, "Model dosyası yüklenirken hata oluştu", e);
        }

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_camera) {
                selectedFragment = new CameraFragment();
                Log.d(TAG, "Camera fragment selected");
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
                Log.d(TAG, "History fragment selected");
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Varsayılan olarak CameraFragment'i göster
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_camera);
        }
    }

    public CameraHelper getCameraHelper() {
        return cameraHelper;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    // TensorFlow Lite model dosyasını yüklemek için bir yöntem ekleyin
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
}
