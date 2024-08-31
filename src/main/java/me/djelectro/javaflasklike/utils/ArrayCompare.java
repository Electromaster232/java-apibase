package me.djelectro.javaflasklike.utils;

import java.util.Arrays;

public class ArrayCompare {
  public static <T> boolean containsAll(T[] array1, T[] array2) {
    // Check if either of the arrays is null or empty
    if (array1 == null || array2 == null || array1.length == 0) {
      return false;
    }

    // Convert arrays to lists for easier manipulation
    Arrays.sort(array1);
    Arrays.sort(array2);

    // Iterate through elements in array1
    for (T element : array1) {
      // Use binary search to check if element exists in array2
      if (Arrays.binarySearch(array2, element) < 0) {
        return false;
      }
    }

    // All elements found
    return true;
  }


}
