package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryFragment extends Fragment {

    private CameraHelper cameraHelper;
    private RecyclerView recyclerView;
    private EmotionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // CameraHelper örneğini alın
        cameraHelper = CameraHelper.getInstance(getContext());
        List<EmotionRecord> emotionHistory = cameraHelper.getEmotionHistory();

        // Adapter'i ayarlayın
        adapter = new EmotionAdapter(emotionHistory);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
