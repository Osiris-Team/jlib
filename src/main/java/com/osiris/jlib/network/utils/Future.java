package com.osiris.jlib.network.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Future<T> extends CompletableFuture<T> {

    public boolean isSuccess(){
        return this.isDone() && !this.isCancelled() && !this.isCompletedExceptionally();
    }

    /**
     * If already {@link #isSuccess()} directly executes.
     */
    public Future<T> onSuccess(Consumer<? super T> action) {
        CompletableFuture<Void> f = super.thenAccept(action);
        if(isSuccess()) {
            action.accept(getNow(null));
        }
        return this;
    }

    public Future<T> onError(Consumer<Throwable> action) {
        CompletableFuture<T> f = super.exceptionally((ex) -> {
            action.accept(ex);
            return null;
        });
        if(isCompletedExceptionally()) {
            action.accept(error);
        }
        return this;
    }

    protected Throwable error = null;
    @Override
    public boolean completeExceptionally(Throwable ex) {
        error = ex;
        ex.printStackTrace();
        return super.completeExceptionally(ex);
    }

    public void onFinish(BiConsumer<T, Throwable> action){
        onSuccess(value -> {
            action.accept(value, null);
        });
        onError(exception -> {
            action.accept(null, exception);
        });
    }

    public boolean isPending() {
        return !isDone();
    }

    /**
     * Same as {@link #complete(Object)} but returns itself
     * for method chaining.
     */
    public Future<T> finish(T v){
        complete(v);
        return this;
    }
}