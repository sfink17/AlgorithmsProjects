package Autocomplete;

import edu.princeton.cs.algs4.StdOut;

import java.util.Comparator;

/**
 * Created by Simon on 3/28/2017.
 */
public class BinarySearchDeluxe {

    // Returns the index of the first key in a[] that equals the search key, or -1 if no such key.
    public static <Key> int firstIndexOf(Key[] a, Key key, Comparator<Key> comparator){
        if (a == null || key == null || comparator == null) throw new NullPointerException();
        int hi = a.length - 1;
        int lo = 0;
        int lastMatch = -1;

        while (lo <= hi){
            int mid = lo + (hi - lo) / 2;
            int compare = comparator.compare(key, a[mid]);
            if (compare <= 0){
                if (compare == 0) lastMatch = mid;
                hi = mid - 1;
            }
            else {
                lo = mid + 1;
            }
        }
        return lastMatch;
    }


    // Returns the index of the last key in a[] that equals the search key, or -1 if no such key.
    public static <Key> int lastIndexOf(Key[] a, Key key, Comparator<Key> comparator){

        if (a == null || key == null || comparator == null) throw new NullPointerException();

        int hi = a.length - 1;
        int lo = 0;
        int lastMatch = -1;

        while (lo <= hi){
            int mid = lo + (hi - lo) / 2;
            int compare = comparator.compare(key, a[mid]);
            if (compare >= 0){
                if (compare == 0) lastMatch = mid;
                lo = mid + 1;
            }
            else {
                hi = mid - 1;
            }
        }
        return lastMatch;
    }

    private static Comparator<Integer> testComparator() {
        return new TestComparator();
    }

    private static class TestComparator implements Comparator<Integer>{
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    }


    // unit testing (required)
    public static void main(String[] args){
        Integer[] test = new Integer[15];
        for (int i = 1; i < 4; i++){
            for (int j = 0; j < 5; j++){
                test[5*(i - 1) + j] = i;
                StdOut.print(i + " ");
            }
        }
        StdOut.println();
        StdOut.println("First index of 1: " + BinarySearchDeluxe.firstIndexOf(test, 1, testComparator()));
        StdOut.println("First index of 2: " + BinarySearchDeluxe.firstIndexOf(test, 2, testComparator()));
        StdOut.println("First index of 3: " + BinarySearchDeluxe.firstIndexOf(test, 3, testComparator()));
        StdOut.println("Last index of 1: " + BinarySearchDeluxe.lastIndexOf(test, 1, testComparator()));
        StdOut.println("Last index of 2: " + BinarySearchDeluxe.lastIndexOf(test, 2, testComparator()));
        StdOut.println("Last index of 3: " + BinarySearchDeluxe.lastIndexOf(test, 3, testComparator()));

    }
}
