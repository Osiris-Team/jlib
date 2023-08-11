package com.osiris.jlib.network.utils;

import java.util.function.Consumer;

public class LoopRunnable {
    public int maxLoops;
    public int i = 0;
    public Consumer<LoopRunnable> code;
    public Consumer<LoopRunnable> onFinish;
    public boolean isBreak = false;

    public LoopRunnable(int maxLoops, Consumer<LoopRunnable> code, Consumer<LoopRunnable> onFinish) {
        this.maxLoops = maxLoops;
        this.code = code;
        this.onFinish = onFinish;
    }

    public LoopRunnable break_(){
        isBreak = true;
        return this;
    }
}
