package com.osiris.jlib.network.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class Loop {
    /**
     * Main loop that executes code every (s) second. <br>
     * Uses a single thread.
     */
    public static Loop s = new Loop();
    public final int sleepIntervalMillis;
    public final Thread thread;
    public final List<LoopsHolder> list = new ArrayList<>();

    public Loop() {
        this(1000);
    }

    public Loop(int sleepIntervalMillis) {
        this.sleepIntervalMillis = sleepIntervalMillis;
        this.thread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(sleepIntervalMillis);
                    synchronized (list) {
                        for (LoopsHolder loopHolder : list) {
                            for (LoopRunnable loopRunnable : loopHolder.runnables) {

                                if (loopRunnable.i >= loopRunnable.maxLoops || loopRunnable.isBreak){
                                    loopHolder.runnables.remove(loopRunnable);
                                    loopRunnable.onFinish.accept(loopRunnable);
                                }
                                else{
                                    loopRunnable.code.accept(loopRunnable);
                                    loopRunnable.i++;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.setName(sleepIntervalMillis + "ms-" + this.getClass().getSimpleName() + "#" + Integer.toHexString(this.hashCode()));
        thread.start();
    }

    /**
     * @param interval time to wait before executing the code again.
     *                 May be seconds, or milliseconds, or anything else,
     *                 depending on the Loop. The Loop.{@link Loop#s} interval is in seconds for example.
     * @param maxLoops the max amount of times the code should be executed.
     * @param loopCode the code to execute in this loop.
     */
    public void add(int interval, int maxLoops, Consumer<LoopRunnable> loopCode) {
        add(interval, maxLoops, loopCode, l -> {});
    }

    public void add(int interval, int maxLoops, Consumer<LoopRunnable> loopCode,
                    Consumer<LoopRunnable> onFinish) {
        synchronized (list) {
            LoopsHolder loopsHolder = null;
            for (LoopsHolder le : list) {
                if (le.interval == interval)
                    loopsHolder = le;
            }
            if (loopsHolder == null) {
                loopsHolder = new LoopsHolder(interval);
                list.add(loopsHolder);
            }
            loopsHolder.runnables.add(new LoopRunnable(maxLoops, loopCode, onFinish));
        }
    }

    public void remove(int interval) {
        synchronized (list) {
            List<LoopsHolder> toRemove = new ArrayList<>();
            for (LoopsHolder l : list) {
                if (l.interval == interval)
                    toRemove.add(l);
            }
            list.removeAll(toRemove);
        }
    }

    public void remove(Runnable runnable) {
        synchronized (list) {
            for (LoopsHolder l : list) {
                l.runnables.remove(runnable);
            }
        }
    }

    public void removeAll(Collection<Runnable> runnables) {
        synchronized (list) {
            for (LoopsHolder l : list) {
                l.runnables.removeAll(runnables);
            }
        }
    }
}
