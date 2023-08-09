package com.osiris.jlib.network.utils;

import java.util.function.Consumer;

public class LoopRunnable {
    public int loopsLeft;
    public Consumer<LoopRunnable> runnable;
    public boolean isBreak = false;

    public LoopRunnable(int loopsLeft, Consumer<LoopRunnable> runnable) {
        this.loopsLeft = loopsLeft;
        this.runnable = runnable;
    }
}
