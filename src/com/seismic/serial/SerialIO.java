package com.seismic.serial;

public interface SerialIO {
    void open(SerialListener serialListener);

    String readStringUntil(int inByte);
}
