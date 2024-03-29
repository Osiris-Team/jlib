/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.jlib.sort;

import com.google.gson.JsonArray;

import java.util.Comparator;
import java.util.List;

/**
 * @author Varun Upadhyay (https://github.com/varunu28)
 * @author Podshivalov Nikita (https://github.com/nikitap492)
 */
public class QuickSort implements SortAlgorithm {

    /**
     * Sorts the provided array and returns it.
     */
    public JsonArray sortJsonArray(JsonArray arr, Comparator<ComparableJsonElement> comparator) {
        ComparableJsonElement[] temp = new ComparableJsonElement[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            temp[i] = new ComparableJsonElement(arr.get(i), comparator);
        }
        temp = sort(temp);
        for (int i = 0; i < arr.size(); i++) {
            arr.set(i, temp[i].el);
        }
        return arr;
    }

    /**
     * Sorts the provided list and returns it.
     */
    public <T> List<T> sort(List<T> unsorted, Comparator<ComparableObject> comparator) {
        Object[] copy = sort(unsorted.toArray((T[]) new Object[unsorted.size()]), comparator);
        for (int i = 0; i < copy.length; i++) {
            unsorted.set(i, (T) copy[i]);
        }
        return unsorted;
    }

    /**
     * Sorts the provided array and returns it.
     */
    public <T> T[] sort(T[] arr, Comparator<ComparableObject> comparator) {
        ComparableObject[] temp = new ComparableObject[arr.length];
        for (int i = 0; i < arr.length; i++) {
            temp[i] = new ComparableObject(arr[i], comparator);
        }
        temp = sort(temp);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (T) temp[i].obj;
        }
        return arr;
    }

    /**
     * This method implements the Generic Quick Sort
     *
     * @param array The array to be sorted Sorts the array in increasing order
     */
    @Override
    public <T extends Comparable<T>> T[] sort(T[] array) {
        doSort(array, 0, array.length - 1);
        return array;
    }

    /**
     * The sorting process
     *
     * @param left  The first index of an array
     * @param right The last index of an array
     * @param array The array to be sorted
     */
    private <T extends Comparable<T>> void doSort(T[] array, int left, int right) {
        if (left < right) {
            int pivot = randomPartition(array, left, right);
            doSort(array, left, pivot - 1);
            doSort(array, pivot, right);
        }
    }

    /**
     * Ramdomize the array to avoid the basically ordered sequences
     *
     * @param array The array to be sorted
     * @param left  The first index of an array
     * @param right The last index of an array
     * @return the partition index of the array
     */
    private <T extends Comparable<T>> int randomPartition(T[] array, int left, int right) {
        int randomIndex = left + (int) (Math.random() * (right - left + 1));
        swap(array, randomIndex, right);
        return partition(array, left, right);
    }

    /**
     * This method finds the partition index for an array
     *
     * @param array The array to be sorted
     * @param left  The first index of an array
     * @param right The last index of an array Finds the partition index of an
     *              array
     */
    private <T extends Comparable<T>> int partition(T[] array, int left, int right) {
        int mid = (left + right) >>> 1;
        T pivot = array[mid];

        while (left <= right) {
            while (less(array[left], pivot)) {
                ++left;
            }
            while (less(pivot, array[right])) {
                --right;
            }
            if (left <= right) {
                swap(array, left, right);
                ++left;
                --right;
            }
        }
        return left;
    }
}
