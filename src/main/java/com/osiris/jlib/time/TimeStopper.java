package com.osiris.jlib.time;

import java.text.DecimalFormat;

public class TimeStopper {
    private final DecimalFormat df = new DecimalFormat();
    private long time1 = 0;
    private long time2 = 0;

    public void start() {
        time1 = System.nanoTime();
    }

    public void stop() {
        time2 = System.nanoTime();
    }

    public Double getSeconds() throws Exception {
        check();
        return ((time2 - time1) / 1000000D) / 1000D;
    }

    public Double getMillis() throws Exception {
        check();
        return (time2 - time1) / 1000000D;
    }

    public Double getNanos() throws Exception {
        check();
        return (time2 - time1) + 0D;
    }

    public String getFormattedSeconds() throws Exception {
        Double d = getSeconds();
        return df.format(d).replace(".", ",");
    }

    public String getFormattedMillis() throws Exception {
        Double d = getMillis();
        return df.format(d).replace(".", ",");
    }

    public String getFormattedNanos() throws Exception {
        Double d = getNanos();
        return df.format(d).replace(".", ",");
    }

    private void check() throws Exception {
        if (time1 == 0 || time2 == 0) {
            throw new Exception("Time 1 or time 2 are null. Ensure that you have started and stopped the counter before retrieving the result!");
        }
    }
}
