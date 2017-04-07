package com.resilienthome.util;

import java.util.ArrayList;

public class LimitedSizeArrayList<T> extends ArrayList<T> {

    private final int maxSize;

    public LimitedSizeArrayList(final int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean add(T data) {
        boolean inserted = super.add(data);
        if (size() > maxSize) {
            removeRange(0, size() - maxSize - 1);
        }

        return inserted;
    }

    public T getYoungest() {
        if (size() == 0) return null;

        return get(size() - 1);
    }

    public T getEldest() {
        if (size() == 0) return null;

        return get(0);
    }
}
