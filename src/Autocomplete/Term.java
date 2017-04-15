package Autocomplete;

import edu.princeton.cs.algs4.StdOut;

import java.util.Comparator;

/**
 * Created by Simon on 3/28/2017.
 */
public class Term implements Comparable<Term> {
    public final String query;
    public final long weight;

    // Initializes a term with the given query string and weight.
    public Term(String query, long weight){
        if (query == null) throw new NullPointerException();
        if (weight < 0) throw new IllegalArgumentException();
        this.query = query;
        this.weight = weight;
    }

    // Compares the two terms in descending order by weight.
    public static Comparator<Term> byReverseWeightOrder(){
        return new ReverseComparator();
    }

    // Compares the two terms in lexicographic order but using only the first r characters of each query.
    public static Comparator<Term> byPrefixOrder(int r){
        if (r < 0) throw new IllegalArgumentException();
        return new PrefixComparator(r);
    }

    // Compares the two terms in lexicographic order by query.
    public int compareTo(Term that){
        return query.compareTo(that.query);
    }

    // Returns a string representation of this term in the following format:
    // the weight, followed by a tab, followed by the query.
    public String toString(){
        return weight + "\t" + query;
    }

    private static class ReverseComparator implements Comparator<Term>{
        @Override
        public int compare(Term o1, Term o2) {
            if (o1.weight > o2.weight) return -1;
            else if (o1.weight < o2.weight) return 1;
            else return 0;
        }
    }

    private static class PrefixComparator implements Comparator<Term>{
        private final int r;

        private PrefixComparator(int r){
            this.r = r;
        }

        @Override
        public int compare(Term o1, Term o2) {
            String p1 = o1.query.substring(0, r);
            String p2;
            if (o2.query.length() < r)
                p2 = o2.query;
            else
                p2 = o2.query.substring(0, r);
            return p1.compareTo(p2);
        }
    }

    // unit testing (required)
    public static void main(String[] args){
        Term t1 = new Term("Alpha", 1000);
        Term t2 = new Term("Beta", 0);
        Term t3 = new Term("Bet on red", Long.MAX_VALUE);

        StdOut.println(t1.compareTo(t2));
        StdOut.println(t2.compareTo(t3));
        StdOut.println(byPrefixOrder(2).compare(t2, t3));
        StdOut.println(byReverseWeightOrder().compare(t2, t3));
    }
}