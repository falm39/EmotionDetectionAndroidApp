package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
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

    private RecyclerView recyclerView;
    private EmotionAdapter adapter;
    private CameraHelper cameraHelper;
    private static final String TAG = "HistoryFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Duygu geçmişini alma
        cameraHelper = CameraHelper.getInstance(getContext());
        List<EmotionRecord> emotionHistory = cameraHelper.getEmotionHistory();

        Log.d(TAG, "onCreateView: " + emotionHistory.size() + " records found.");

        adapter = new EmotionAdapter(emotionHistory);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Verilerin güncellenmesini sağla
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
