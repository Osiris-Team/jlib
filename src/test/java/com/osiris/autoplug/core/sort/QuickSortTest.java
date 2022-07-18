package com.osiris.autoplug.core.sort;

import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class QuickSortTest {

    @Test
    void sortJsonArray() {
        JsonArray arr = new JsonArray();
        arr.add("1");
        arr.add("-1");
        arr.add("0");
        new QuickSort().sortJsonArray(arr, (thisEl, otherEl) -> {
            int thisId = thisEl.el.getAsInt();
            int otherId = otherEl.el.getAsInt();
            return Integer.compare(thisId, otherId);
        });
        assertEquals(-1, arr.get(0).getAsInt());
        assertEquals(0, arr.get(1).getAsInt());
        assertEquals(1, arr.get(2).getAsInt());
    }

    @Test
    void sort() {
        Integer[] arr = new Integer[]{1, 7, 1, 222, 0, -1};
        new QuickSort().sort(arr, Comparator.comparingInt(thisNum -> (int) thisNum.obj));
        assertEquals(-1, arr[0]);
        assertEquals(0, arr[1]);
        assertEquals(1, arr[2]);
    }
}