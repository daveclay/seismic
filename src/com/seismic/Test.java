package com.seismic;

import com.seismic.p.PAppletRunner;
import processing.core.PApplet;

public class Test extends PApplet {
    public static void main(String[] args) {
        PAppletRunner.run(new Test());
    }

    public void drwa() {
        background(color(255, 100, 0));
    }
}
