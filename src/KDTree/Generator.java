/******************************************************************************
 *  Compilation:  javac Generator.java
 *  Execution:    java Generator N
 *  Dependencies: none
 *
 *  Creates N random points in the unit square and print to standard output.
 *
 *  % java Generator 5
 *  0.195080 0.938777
 *  0.351415 0.017802
 *  0.556719 0.841373
 *  0.183384 0.636701
 *  0.649952 0.237188
 *
 ******************************************************************************/
package KDTree;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

public class Generator {

    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        for (int i = 0; i < N; i++) {
            double x = StdRandom.uniform(0.0, 1.0);
            double y = StdRandom.uniform(0.0, 1.0);
            StdOut.printf("%8.6f %8.6f\n", x, y);
        }
    }
}
