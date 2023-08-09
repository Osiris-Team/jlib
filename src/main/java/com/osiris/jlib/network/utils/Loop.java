package com.osiris.jlib.network.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class Loop {
    /**
     * Main loop that executes code every second. <br>
     * Uses a single thread.
     */
    public static Loop main = new Loop();
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
                                loopRunnable.loopsLeft--;
                                if (loopRunnable.loopsLeft <= 0 || loopRunnable.isBreak)
                                    loopHolder.runnables.remove(loopRunnable);
                                else
                                    loopRunnable.runnable.accept(loopRunnable);
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

    public void add(int interval, int maxLoops, Consumer<LoopRunnable> runnable) {
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
            loopsHolder.runnables.add(new LoopRunnable(maxLoops, runnable));
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
