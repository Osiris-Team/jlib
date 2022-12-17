package com.osiris.jlib.sort;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        for (JsonElement val : arr) {
            System.out.println(val.getAsInt());
        }
    }

    @Test
    void sort() {
        Integer[] arr = new Integer[]{1, -1, 0};
        new QuickSort().sort(arr, Comparator.comparingInt(thisNum -> (int) thisNum.obj));
        assertEquals(-1, arr[0]);
        assertEquals(0, arr[1]);
        assertEquals(1, arr[2]);
        for (Integer val : arr) {
            System.out.println(val);
        }
    }

    @Test
    void sortList() {
        List<Integer> arr = new ArrayList<>();
        arr.add(1);
        arr.add(-1);
        arr.add(0);
        new QuickSort().sort(arr, Comparator.comparingInt(thisNum -> (int) thisNum.obj));
        assertEquals(-1, arr.get(0));
        assertEquals(0, arr.get(1));
        assertEquals(1, arr.get(2));
        for (Integer val : arr) {
            System.out.println(val);
        }
    }
}