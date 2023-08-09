package com.osiris.jlib.network.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Later<T> extends CompletableFuture<T> {

    public boolean isSuccess(){
        return this.isDone() && !this.isCancelled() && !this.isCompletedExceptionally();
    }

    /**
     * If already {@link #isSuccess()} directly executes.
     */
    public CompletableFuture<Void> accept(Consumer<? super T> action) {
        CompletableFuture<Void> f = super.thenAccept(action);
        if(isSuccess()) {
            f.complete(null);
            action.accept(getNow(null));
        }
        return f;
    }
}