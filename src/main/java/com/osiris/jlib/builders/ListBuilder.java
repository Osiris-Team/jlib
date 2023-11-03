package com.osiris.jlib.builders;

import java.util.ArrayList;
import java.util.List;

public class ListBuilder<T> {
    public List<T> list = new ArrayList<>();

    public ListBuilder<T> add(T t) {
        list.add(t);
        return this;
    }

    public ListBuilder<T> remove(T t) {
        list.remove(t);
        return this;
    }
}
