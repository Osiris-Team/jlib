package com.osiris.jlib.network.utils;

import java.util.ArrayList;
import java.util.List;

public class ListBuilder {
    public List<Object> list = new ArrayList<>();

    public ListBuilder add(Object t) {
        list.add(t);
        return this;
    }

    public ListBuilder remove(Object t) {
        list.remove(t);
        return this;
    }
}
