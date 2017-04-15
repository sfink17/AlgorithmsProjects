package Autocomplete;

import edu.princeton.cs.algs4.*;

import java.util.Arrays;

/**
 * Created by Simon on 3/28/2017.
 */
public class Autocomplete {
    private final Term[] terms;

    // Initializes the data structure from the given array of terms.
    public Autocomplete(Term[] terms){
        if (terms == null) throw new NullPointerException();
        for (Term term : terms){
            if (term == null) throw new NullPointerException();
        }
        QuickX.sort(terms);
        this.terms = terms;
    }

    // Returns all terms that start with the given prefix, in descending order of weight.
    public Term[] allMatches(String prefix){
        if (prefix == null) throw new NullPointerException();
        Term myTerm = new Term(prefix, 0L);
        int first = 0;
        int last = terms.length - 1;
        if (prefix.length() != 0) {
            first = BinarySearchDeluxe.firstIndexOf(terms, myTerm, Term.byPrefixOrder(prefix.length()));
            if (first == -1) return new Term[0];
            last = BinarySearchDeluxe.lastIndexOf(terms, myTerm, Term.byPrefixOrder(prefix.length()));
        }
        Term[] copy = Arrays.copyOfRange(terms, first, last + 1);
        MergeX.sort(copy, Term.byReverseWeightOrder());
        return copy;
    }

    // Returns the number of terms that start with the given prefix.
    public int numberOfMatches(String prefix){
        if (prefix == null) throw new NullPointerException();
        Term myTerm = new Term(prefix, 0L);
        int first = 0;
        int last = terms.length - 1;
        if (prefix.length() != 0) {
            first = BinarySearchDeluxe.firstIndexOf(terms, myTerm, Term.byPrefixOrder(prefix.length()));
            if (first == -1) return 0;
            last = BinarySearchDeluxe.lastIndexOf(terms, myTerm, Term.byPrefixOrder(prefix.length()));
        }
        return last - first + 1;
    }

    public static void main(String[] args) {

        // read in the terms from a file
        String filename = args[0];
        In in = new In(filename);
        int N = in.readInt();
        Term[] terms = new Term[N];
        for (int i = 0; i < N; i++) {
            long weight = in.readLong();           // read the next weight
            in.readChar();                         // scan past the tab
            String query = in.readLine();          // read the next query
            terms[i] = new Term(query, weight);    // construct the term
        }

        // read in queries from standard input and print out the top k matching terms
        int k = Integer.parseInt(args[1]);
        Autocomplete autocomplete = new Autocomplete(terms);
        while (StdIn.hasNextLine()) {
            String prefix = StdIn.readLine();
            Term[] results = autocomplete.allMatches(prefix);
            int matches = autocomplete.numberOfMatches(prefix);
            int displayed = Math.min(k, results.length);
            StdOut.println("Displaying " + displayed + " of " + matches + " matches:");
            for (int i = 0; i < displayed; i++)
                StdOut.println(results[i]);
        }
    }
}
