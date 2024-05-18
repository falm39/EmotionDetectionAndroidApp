package com.example.myapplication;

public class EmotionRecord {
    private String emotion;
    private long timestamp;

    public EmotionRecord(String emotion, long timestamp) {
        this.emotion = emotion;
        this.timestamp = timestamp;
    }

    public String getEmotion() {
        return emotion;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
