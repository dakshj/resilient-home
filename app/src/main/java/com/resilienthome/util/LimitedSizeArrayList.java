package com.resilienthome.util;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Returns the nth youngest element.
     * <p>
     * Valid values are from 1 to {@link List#size()}.
     *
     * @param n How young the element being fetched should be
     *          <p>
     *          E.g.: 1st youngest element --> Last element in the list
     * @return The nth youngest element
     */
    public T getNthYoungest(final int n) {
        if (size() == 0) return null;

        return get(size() - n);
    }
}
