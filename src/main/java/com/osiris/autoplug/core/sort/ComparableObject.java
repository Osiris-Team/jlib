/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.autoplug.core.sort;

import java.util.Comparator;

public final class ComparableObject implements Comparable<ComparableObject> {
    public Object obj;
    public Comparator<ComparableObject> comparator;

    public ComparableObject(Object obj, Comparator<ComparableObject> comparator) {
        this.obj = obj;
        this.comparator = comparator;
    }

    @Override
    public int compareTo(ComparableObject o) {
        return comparator.compare(this, o);
    }
}
