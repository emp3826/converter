package com.example.converter;

public enum VideoFormat {
    MP4("MP4"),
    MKV("MKV"),
    WMV("WMV");

    private final String format;

    VideoFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return format;
    }
}
