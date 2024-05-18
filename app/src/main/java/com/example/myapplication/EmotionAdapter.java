package com.example.myapplication;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmotionAdapter extends RecyclerView.Adapter<EmotionAdapter.EmotionViewHolder> {

    private List<EmotionRecord> emotionHistory;
    private static final String TAG = "EmotionAdapter";

    public EmotionAdapter(List<EmotionRecord> emotionHistory) {
        this.emotionHistory = emotionHistory;
    }

    @NonNull
    @Override
    public EmotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emotion, parent, false);
        return new EmotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmotionViewHolder holder, int position) {
        EmotionRecord record = emotionHistory.get(position);
        Log.d(TAG, "onBindViewHolder: Binding record at position " + position + ": " + record.getEmotion());
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + emotionHistory.size());
        return emotionHistory.size();
    }

    class EmotionViewHolder extends RecyclerView.ViewHolder {

        private TextView emotionTextView;
        private TextView timestampTextView;

        public EmotionViewHolder(@NonNull View itemView) {
            super(itemView);
            emotionTextView = itemView.findViewById(R.id.emotion_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
        }

        public void bind(EmotionRecord record) {
            emotionTextView.setText(record.getEmotion());
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(record.getTimestamp()));
            timestampTextView.setText(timestamp);
        }
    }
}
