package com.seismic.ui.p;

public class ProcessingUtils {

    public static final int map(int value, int start1, int stop1, int start2, int stop2) {
        int outgoing = start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
        String badness = null;
        if(outgoing != outgoing) {
            badness = "NaN (not a number)";
        } else if(outgoing == -1.0F / 0.0 || outgoing == 1.0F / 0.0) {
            badness = "infinity";
        }

        if(badness != null) {
            throw new IllegalArgumentException(String.format("map(%s, %s, %s, %s, %s) called, which returns %s",
                    new Object[]{value, start1, stop1, start2, stop2, badness}));
        }

        return outgoing;
    }
}
