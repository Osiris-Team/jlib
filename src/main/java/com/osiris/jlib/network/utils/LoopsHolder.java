package com.osiris.jlib.network.utils;

import java.util.concurrent.CopyOnWriteArrayList;

public class LoopsHolder {
    public final int interval;
    public CopyOnWriteArrayList<LoopRunnable> runnables = new CopyOnWriteArrayList<>();

    public LoopsHolder(int interval) {
        this.interval = interval;
    }
}
