package Percolator;

import edu.princeton.cs.algs4.WeightedQuickUnionUF;


/**
 * Created by Simon on 3/21/2017.
 */
public class Percolation {

    private enum Direction {
        UP (-1, 0),
        LEFT (0, -1),
        RIGHT (0, 1),
        DOWN (1, 0);

        private final int hShift;
        private final int vShift;

        Direction(int hShift, int vShift) {
            this.hShift = hShift;
            this.vShift = vShift;
        }
    }

    private int[] grid;
    private final int N;
    private WeightedQuickUnionUF unionizer;
    private int count = 0;
    private boolean percolates = false;

    // create N-by-N grid, with all sites initially blocked
    public Percolation(int N) {
        this.N = N;
        if (N <=0) {
            throw new IllegalArgumentException();
        }
        this.unionizer = new WeightedQuickUnionUF(N*N);
        grid = new int[N*N];
        for (int i = 0; i < N; i++){
            grid[i] = 2;
            grid[(N*N - 1) - i] = 4;
        }



    }
    // open the site (row, col) if it is not open already
    public void open(int row, int col){
        if (!isOpen(row, col)) {
            if (!isValid(row, col)) throw new IndexOutOfBoundsException();
            grid[getNode(row, col)]++;

            for (Direction d : Direction.values()) {
                int adjRow = row + d.hShift;
                int adjCol = col + d.vShift;
                try {
                    if (isOpen(adjRow, adjCol)) {
                        grid[getNode(row, col)] = grid[getNode(row, col)] | grid[unionizer.find(getNode(adjRow, adjCol))];
                        unionizer.union(getNode(row, col), getNode(adjRow, adjCol));
                    }
                } catch (IndexOutOfBoundsException i) {
                }
            }
            grid[unionizer.find(getNode(row, col))] = grid[getNode(row, col)];
            if (grid[getNode(row, col)] == 7) percolates = true;
            count++;
        }
    }

    // is the site (row, col) open?
    public boolean isOpen(int row, int col) {
        if (!isValid(row, col)) throw new IndexOutOfBoundsException();
        return (grid[getNode(row, col)] & 1) != 0;
    }

    // is the site (row, col) full?
    public boolean isFull(int row, int col){
        if (!isValid(row, col)) throw new IndexOutOfBoundsException();
        return (grid[unionizer.find(getNode(row, col))] & 3) == 3;
    }

    // number of open sites
    public int numberOfOpenSites() {
        return count;
    }

    // does the system percolate?
    public boolean percolates() {
        return percolates;
    }

    private boolean isValid(int row, int col){
        return (row < N && row >= 0) && (col < N && col >= 0);
    }

    private int getNode(int row, int col){
        return (N * row) + col;
    }

    // unit testing (required)
    public static void main(String[] args) {
        int failures = 0;
        try {
            Percolation test1 = new Percolation(-20);
            System.out.println("Failure: Invalid argument passed");
        }

        catch (IllegalArgumentException i){
        }

        Percolation test2 = new Percolation(20);

        if (test2.isOpen(0,0)) {
            System.out.println("Failure: (0, 0) should not be open");
            failures++;
        }

        try{
            test2.isOpen(-1, 0);
            System.out.println("Failure: isOpen out of bounds.");
            failures++;
        }
        catch (IndexOutOfBoundsException i){}

        try{
            test2.open(0, 21);
            System.out.println("Failure: open out of bounds.");
            failures++;
         }
        catch (IndexOutOfBoundsException i){}

        try {
            test2.isFull(0, 20);
            System.out.println("Failure: isFull out of bounds.");
            failures++;
        }
        catch (IndexOutOfBoundsException i){}

        test2.open(0, 0);

        for (int i = 0; i < 20; i++){
            test2.open(i, 0);
        }

        if (!test2.percolates()){
            System.out.println("Failure: does not percolate");
            failures++;
        }

        if (test2.numberOfOpenSites() != 20){
            System.out.println("Failure: number of open sites = " + test2.numberOfOpenSites());
            failures++;
        }

        if (failures != 0) {
            System.out.println("Failures: " + failures + ", debug me");
        }
        else {
            System.out.println("Test success.");
        }
    }
}
