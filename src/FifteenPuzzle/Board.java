package FifteenPuzzle;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.*;

/**
 * Created by Simon on 3/31/2017.
 */
public class Board {
    private final int[][] tiles;
    private final int N;
    private final int BIG_N;
    private int hash;

    public Board(int[][] tiles){
        this.tiles = tiles;
        this.N = tiles.length;
        this.BIG_N = N*N;
    }
    public int tileAt(int i, int j) {
        if (i < 0 || i >= N || j < 0 || j >=N) throw new IllegalArgumentException();
        return tiles[i][j];
    }
    public int size() { return N; }

    public int hamming() {
        int incorrect = 0;
        for (int i = 0; i < BIG_N; i++) {
            int t = tileAt1D(i);
            if (t != i + 1 && t != 0)
                incorrect++;
        }
        return incorrect;
    }

    public int manhattan(){
        int totalDist = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int t = tileAt(i, j) - 1;
                if (t != -1) {
                    totalDist += Math.abs((t / N) - i);
                    totalDist += Math.abs((t % N) - j);
                }
            }
        }

        return totalDist;
    }
    public boolean isGoal(){
        return (hamming() == 0);
    }

    public boolean isSolvable(){
        RBTree boardTree = new RBTree();
        int blankSpace = (N + 1) & 1; // 1 IFF N is even (if it's odd, we don't care)
        int invSum = 0;
        for (int i = 0; i < BIG_N; i++){
            int t = tileAt1D(i);
            if (t == 0)
                blankSpace = ((i / N) + 1) & blankSpace; // gets parity of empty space for even 'N's
            else {
                invSum += boardTree.putAndGetLarger(t);
            }
        }
        return ((invSum + blankSpace) % 2 == 0);
    }
    public boolean equals(Object y){
        Board cmp;
        if (!(y instanceof Board) || (cmp = (Board) y).size() != N)
            return false;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (tileAt(i, j) != cmp.tileAt(i, j))
                    return false;

            }
        }
        return true;
    }

    public int hashCode(){
        int h = hash;
        if (h != 0) return h;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                h = tileAt(i, j) + 11*h;
            }
            h = h/((N*N) << N);
        }
        return hash = h;
    }

    public Iterable<Board> neighbors(){
        ArrayList<Board> neighbors = new ArrayList<>();
        int blankRow = 0;
        int blankCol = 0;
        zFind: for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (tileAt(i, j) == 0){
                    blankRow = i;
                    blankCol = j;
                    break zFind;
                }
            }
        }
        for (Direction d : Direction.values()) {
            if (!isValid(blankRow + d.vShift, blankCol + d.hShift)) continue;
            int[][] copy = new int[N][N];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    copy[i][j] = tiles[i][j];
                }
            }
            swap(copy, blankRow, blankCol, blankRow + d.vShift, blankCol + d.hShift);
            neighbors.add(new Board(copy));
        }
        return neighbors;
    }

    private void swap(int[][] tiles, int r1, int c1, int r2, int c2){
        int c = tiles[r1][c1];
        tiles[r1][c1] = tiles[r2][c2];
        tiles[r2][c2] = c;
    }

    private boolean isValid(int r, int c){
        return (r >= 0 && r < N && c >= 0 && c < N);
    }

    public String toString(){
        StringJoiner row = new StringJoiner(" ", " ", " ");
        for (int i = 0; i < N; i++) {
            row.add("\n");
            for (int j = 0; j < N; j++){
                Integer tile = tileAt(i, j);
                row.add(tile.toString());
                if (tile < 10)
                    row.add("");
            }
        }
        row.add("\n");
        return row.toString();
    }
    private int tileAt1D(int t){
        return tileAt(t / N, t % N);
    }


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

    private static class RBTree { // Making my own because Java has no "rank" method.
        private Node root;
        private RBTree() {}
        private static final boolean RED = true;
        private static final boolean BLACK = false;

        private static final class Node {
            int val;
            Node left;
            Node right;
            Node parent;
            boolean color = BLACK;
            private int size;

            private Node(int val, Node parent, int size){
                this.val = val;
                this.parent = parent;
                this.size = size;
            }
        }
        /**
         * RB 'put' operation, copied (mostly) from Java's implementation.
         * Calculates the 'anti-rank' (number of larger nodes) at each step.
         * Also does away with Princeton's reliance on recursive methods by replacing
         * them with loops. This version also lets me calculate rank easily.
         *
         * Adapted to store only int values instead of generic KV pairs.
         */
        public int putAndGetLarger(int v) {
            Node t = root;
            int larger = 0;
            if (t == null) {
                root = new Node(v, null, 1);
                return 0;
            }
            Node parent;
            // split comparator and comparable paths
            do {
                parent = t;
                t.size++;
                if (v < t.val) {
                    larger += sizeOf(rightOf(t)) + 1;
                    t = t.left;
                }
                else
                    t = t.right;
            } while (t != null);

            Node e = new Node(v, parent, 1);
            if (v < parent.val)
                parent.left = e;
            else
                parent.right = e;
            fixAfterInsertion(e);
            return larger;
        }

        /**
         * Balancing operations.
         *
         * These are lifted from Java's implementation, with a few exceptions.
         * The size parameter is now localized to each subtree instead of
         * acting as a class property. This allows constant time checks of
         * rank at each step.
         */

        private static int sizeOf(Node p) {
            return (p == null ? 0 : p.size);
        }

        private static boolean colorOf(Node p) {
            return (p == null ? BLACK : p.color);
        }

        private static Node parentOf(Node p) {
            return (p == null ? null: p.parent);
        }

        private static void setColor(Node p, boolean c) {
            if (p != null)
                p.color = c;
        }

        private static Node leftOf(Node p) {
            return (p == null) ? null: p.left;
        }

        private static Node rightOf(Node p) {
            return (p == null) ? null: p.right;
        }

        private void rotateLeft(Node p) {
            if (p != null) {
                Node r = p.right;
                p.size -= sizeOf(rightOf(r)) + 1;
                r.size += sizeOf(leftOf(p)) + 1;
                p.right = r.left;
                if (r.left != null)
                    r.left.parent = p;
                r.parent = p.parent;
                if (p.parent == null)
                    root = r;
                else if (p.parent.left == p)
                    p.parent.left = r;
                else
                    p.parent.right = r;
                r.left = p;
                p.parent = r;
            }
        }

        private void rotateRight(Node p) {
            if (p != null) {
                Node l = p.left;
                p.size -= sizeOf(leftOf(l)) + 1;
                l.size += sizeOf(rightOf(p)) + 1;
                p.left = l.right;
                if (l.right != null) l.right.parent = p;
                l.parent = p.parent;
                if (p.parent == null)
                    root = l;
                else if (p.parent.right == p)
                    p.parent.right = l;
                else p.parent.left = l;
                l.right = p;
                p.parent = l;
            }
        }

        private void fixAfterInsertion(Node x) {
            x.color = RED;

            while (x != null && x != root && x.parent.color == RED) {
                if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                    Node y = rightOf(parentOf(parentOf(x)));
                    if (colorOf(y) == RED) {
                        setColor(parentOf(x), BLACK);
                        setColor(y, BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        x = parentOf(parentOf(x));
                    } else {
                        if (x == rightOf(parentOf(x))) {
                            x = parentOf(x);
                            rotateLeft(x);
                        }
                        setColor(parentOf(x), BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        rotateRight(parentOf(parentOf(x)));
                    }
                } else {
                    Node y = leftOf(parentOf(parentOf(x)));
                    if (colorOf(y) == RED) {
                        setColor(parentOf(x), BLACK);
                        setColor(y, BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        x = parentOf(parentOf(x));
                    } else {
                        if (x == leftOf(parentOf(x))) {
                            x = parentOf(x);
                            rotateRight(x);
                        }
                        setColor(parentOf(x), BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        rotateLeft(parentOf(parentOf(x)));
                    }
                }
            }
            root.color = BLACK;
        }

        /*
        public String toString() {
            Queue<Node> rowPrint = new Queue<>();
            if (root != null) {
                rowPrint.enqueue(root);
                StdOut.println(root.val);
                try {
                    while (!rowPrint.isEmpty()) {
                        Node current = rowPrint.dequeue();
                        StdOut.println();
                        if (current.left != null) {
                            StdOut.print(current.left.val + "    ");
                            rowPrint.enqueue(current.left);
                        }
                        if (current.right != null) {
                            StdOut.print(current.right.val + "    ");
                            rowPrint.enqueue(current.right);
                        }
                    }
                } catch (InterruptedException i) {

                }
            }
        }
        */
    }

    public static void
    main(String[] args) {
        StdOut.printf("%-25s %7s %8s\n", "filename", "moves", "time");
        StdOut.println("------------------------------------------");
        // for each command-line argument
        // read in the board specified in the filename
        In in = new In("res/p4data/puzzle4x4-02.txt");
        int N = in.readInt();
        int[][] blocks = new int[N][N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                blocks[i][j] = in.readInt();
        Board initial = new Board(blocks);
        if (initial.isSolvable())
            StdOut.println("solvable");
        else
            StdOut.println("unsolvable");
        StdOut.println(initial);
        StdOut.println("Neighbors:");
        for (Board neighbor : initial.neighbors()){
            StdOut.println(neighbor);
        }

        int[][] copy = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                copy[i][j] = initial.tileAt(i, j);
            }
        }
        Board c = new Board(copy);

    }
}
