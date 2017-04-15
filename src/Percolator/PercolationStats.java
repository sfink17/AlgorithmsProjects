package Percolator;

import edu.princeton.cs.algs4.*;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Simon on 3/21/2017.
 */

public class PercolationStats {

    private double[] localPercPoints;
    private final int N;
    private final int T;
    private double mean;
    private double stdDev;
    private final double totalTime;
    private final double avgTime;

    public PercolationStats(int N, int T){
        if (N <= 0 || T <= 0) throw (new IllegalArgumentException());
        this.N = N;
        this.T = T;
        Percolation trialGrid;
        localPercPoints = new double[T];
        long clock = System.nanoTime();

        for (int i = 0; i < T; i++){

            trialGrid = new Percolation(N);
            while (!trialGrid.percolates()){
                int row = StdRandom.uniform(0, N);
                int col = StdRandom.uniform(0, N);
                if (!trialGrid.isOpen(row, col)) {
                    trialGrid.open(row, col);
                }
            }
            localPercPoints[i] = ( ((double) trialGrid.numberOfOpenSites()) / (N*N));
        }
        this.totalTime = System.nanoTime() - clock;
        this.avgTime = this.totalTime / T;
    }
    public double mean() {
        mean = StdStats.mean(localPercPoints);
        return mean;
    }
    public double stddev() {
        stdDev = StdStats.stddev(localPercPoints);
        return stdDev;
    }
    public double confidenceLow() {
        return mean - (1.96 * stdDev / Math.sqrt(T));
    }
    public double confidenceHigh() {
        return mean + (1.96 * stdDev / Math.sqrt(T));
    }

    private void printOut(){
        StdOut.println("N = " + N + ", Trials: " + T);
        StdOut.println("Mean: " + mean());
        StdOut.println("Standard Deviation: " + stddev());
        StdOut.println("Confidence Interval: ( " + confidenceLow() + " , " + confidenceHigh() + " )");
        StdOut.println("Total time: " + totalTime + ", Avg time: " + avgTime);

    }

    public static void main(String args[]){
        PercolationStats pstat = new PercolationStats(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        pstat.printOut();

        // set this to measure times from N = n*1000 to 20*n*1000
        // quickfind is pretty awful for everything but n = 1
        int n;
        int dataSize = 4;
        double[] times = new double[dataSize];
        double sum = 0;
        int len = 0;
        double maxT;
        // Little n, used as the base case for measuring growth.
        n = 10000;
        for (int i = 0; i < dataSize; i++) {
            PercolationStats tester = new PercolationStats((int) Math.sqrt(n), 3);
            times[i] = tester.totalTime;
            n *= 2;
        }
        n *= .5;
        maxT = times[dataSize - 1];
        double[] ratios = new double[dataSize - 1];
        for (int i = 0; i < dataSize - 1; i++) {
            ratios[i] = times[i + 1] / times[i];
            System.out.println(ratios[i]);

        }
        for (double number : ratios) {
            String str = Double.toString(number);
            if (!str.equals("Infinity") && !str.equals("NaN")) {
                sum += number;
                len++;
            }
        }

        if (len != 0) {
            double estPower = log2(sum / len);
            double estCoef = maxT / (Math.pow(n, estPower));
            System.out.println("Growth estimated at " + round(estCoef) + "N^" + round(estPower));
        }
        else {
            System.out.println("Recorded invalid times. Try increasing little n.");
        }

    }

    private static String round(double rounded){
        BigDecimal bd = new BigDecimal(rounded);
        bd = bd.round(new MathContext(3));
        return bd.toEngineeringString();
    }

    private static double log2(double a){
        return Math.log(a) / Math.log(2.0);
    }
}
