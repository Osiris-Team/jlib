package com.osiris.jlib.network;

public class Data<T> {
    public final int id;
    public final T object;

    public Data(int id, T object) {
        this.id = id;
        this.object = object;
    }
}
