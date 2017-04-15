package Autocomplete;

import edu.princeton.cs.algs4.*;
import sun.misc.Queue;


/**
 * Created by Simon on 3/29/2017.
 */
public class RadixSort {

    private static final int QUICKSORT_CUTOFF = 60;
    private static final int W = 256;
    private static int[] count = new int[W];
    private static int[] pile = new int[W];
    private static Term bucket;
    private static int stopLetter = 0;


    private RadixSort(){}

    public static void sort(Term[] array){
        sort(array, 0, 0, array.length, true);
    }
    public static void sortReverseByWeight(Term[] array){
        long max = 0;
        int start;
        for (Term term : array){
            if (term.weight > max) max = term.weight;
        }
        start = 7 - (Long.toBinaryString(max).length() - 1) / 8;
        sortLongReverse(array, start, 0, array.length);
    }
    public static void sortByLetter(Term[] array, int letter){
        sort(array, letter, 0, array.length, false);
    }
    public static void sortLetters(Term[] array, int letter){
        stopLetter = letter;
        sort(array, letter, 0, array.length, false);
    }

    public static void sortFromLetter(Term[] array, int letter){
        sort(array, letter, 0, array.length, true);
    }

    private static void sort(Term[] array, int letter, int lo, int hi, boolean keepGoing){
        Queue<Integer> pairs = new Queue<>();
        int pointer;
        int min = 256;
        int max = 0;
        for (int i = lo; i < hi; i++) {
            int l = getLetterValue(array[i], letter);
            if (l > max) max = l;
            if (l < min && l != 0) min = l;
            count[getLetterValue(array[i], letter)]++;
        }
        if (max == 0) return;
        pile[0] = count[0] + lo;
        pointer = pile[0];
        pile[min] = count[min] + pile[0];

        for (int i = min + 1; i <= max; i++){
            pile[i] = pile[i - 1] + count[i];
        }

        for (int k = min; k <= max; k++){
            if (count[k] == 0) continue;
            bucket = array[pointer];
            while (true){
                int p = getLetterValue(bucket, letter);
                if (pile[p] == pointer && p != 0) break;
                swap(array, pointer, --pile[p]);
            }
            if (count[k] > 1 && keepGoing) {
                pairs.enqueue(pointer);
                pairs.enqueue(pointer + count[k]);
            }
            pointer += count[k];
            count[k] = 0;

        }
        count[0] = 0;

        if (keepGoing || stopLetter > letter) {
            while (!pairs.isEmpty()) {
                try {
                    int first = pairs.dequeue();
                    int last = pairs.dequeue();
                    if (last - first > QUICKSORT_CUTOFF) {
                        sort(array, letter + 1, first, last, true);
                    } else {
                        QuickX.sortRange(array, first, last);
                    }
                } catch (InterruptedException i) {
                }
            }
        }

    }

    private static void sortLongReverse(Term[] array, int place, int lo, int hi){
        Queue<Integer> pairs = new Queue<>();
        int pointer;
        int min = 256;
        int max = 0;
        for (int i = lo; i < hi; i++) {
            int l = getByteValue(array[reverseIndex(array.length, i)], place);
            if (l > max) max = l;
            if (l < min && l != 0) min = l;
            count[getByteValue(array[reverseIndex(array.length, i)], place)]++;
        }
        if (max == 0){
            count[0] = 0;
            if (place < 7) sortLongReverse(array, place + 1, lo, hi);
            return;
        }
        if (count[0] > 1){
            pairs.enqueue(lo);
            pairs.enqueue(count[0] + lo);
        }
        pile[0] = count[0] + lo;
        pointer = pile[0];
        pile[min] = count[min] + pile[0];

        for (int i = min + 1; i <= max; i++){
            pile[i] = pile[i - 1] + count[i];
        }

        for (int k = min; k <= max; k++){
            if (count[k] == 0) continue;
            bucket = array[reverseIndex(array.length, pointer)];
            while (true){
                int p = getByteValue(bucket, place);
                if (pile[p] == pointer && p != 0) break;
                swap(array, reverseIndex(array.length, pointer), reverseIndex(array.length, --pile[p]));
            }
            if (count[k] > 1) {
                pairs.enqueue(pointer);
                pairs.enqueue(pointer + count[k]);
            }
            pointer += count[k];
            count[k] = 0;

        }

        count[0] = 0;
        while (!pairs.isEmpty()) {
            try {
                int first = pairs.dequeue();
                int last = pairs.dequeue();
                if (last - first > QUICKSORT_CUTOFF) {
                    sortLongReverse(array, place + 1, first, last);
                } else {
                    insertionSortRev(array, first, last - 1);
                }
            } catch (InterruptedException i) {
            }
        }

    }



    // sort from a[lo] to a[hi] using insertion sort
    private static void insertionSort(Term[] a, int lo, int hi) {
        for (int i = lo; i <= hi; i++)
            for (int j = i; j > lo && less(a[j], a[j-1]); j--) {
                exch(a, j, j - 1);
            }
    }
    private static void insertionSortRev(Term[] a, int lo, int hi) {
        for (int i = lo; i <= hi; i++)
            for (int j = i; j > lo && a[reverseIndex(a.length, j)].weight < a[reverseIndex(a.length, j-1)].weight; j--) {
                exch(a, reverseIndex(a.length, j), reverseIndex(a.length, j - 1));
            }
    }
    private static boolean less(Term v, Term w) {
        return v.compareTo(w) < 0;
    }
    private static void exch(Term[] a, int i, int j) {
        Term swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

    private static int getLetterValue(Term term, int letter){
        try {
            return (int) term.query.charAt(letter);
        }
        catch (IndexOutOfBoundsException i){
            return 0;
        }
    }
    private static int getByteValue(Term term, int place){
        try {
            return (int) ((term.weight >> (56 - 8*place)) & 0xFF);
        }
        catch (IndexOutOfBoundsException i){
            return 0;
        }
    }

    private static int reverseIndex(int hi, int p){
        return hi - 1 - p;
    }

    private static void swap(Term[] array, int a, int b){
        array[a] = array[b];
        array[b] = bucket;
        bucket = array[a];
    }

    public static void main(String[] args){
        In in = new In("res/p3data/actors.txt");
        int N = in.readInt();
        long clock = System.nanoTime();
        Term[] terms = new Term[N];
        for (int i = 0; i < N; i++) {
            long weight = in.readLong();           // read the next weight
            String query = in.readLine().trim();          // read the next query
            terms[i] = new Term(query, weight);    // construct the term
        }
        double radix = 0;
        double merge = 0;
        double quick = 0;
        StdOut.println("Input in " + (clock = System.nanoTime() - clock) + "s");
        int trials = 1;
        for (int i = 0; i < trials; i++) {
            StdRandom.shuffle(terms);
            clock = System.nanoTime();
            RadixSort.sort(terms);
            radix += System.nanoTime() - clock;
            StdRandom.shuffle(terms);
            clock = System.nanoTime();
            QuickX.sort(terms);
            quick += System.nanoTime() - clock;
            StdRandom.shuffle(terms);
            clock = System.nanoTime();
            MergeX.sort(terms);
            merge += System.nanoTime() - clock;
        }
        radix = radix/trials;
        merge = merge/trials;
        quick = quick/trials;

        StdOut.println("Radix: " + radix);
        StdOut.println("Quick: " + quick);
        StdOut.println("Merge: " + merge);

        //for (Term term : terms){
        //    StdOut.println(term);
        //}

    }
}
