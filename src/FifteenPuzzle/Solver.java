package FifteenPuzzle;

import edu.princeton.cs.algs4.*;
import KDTree.NanoWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Each instance of this class wraps a solution of the target 15-puzzle board.
 *
 * Created by Simon on 3/31/2017.
 */

public class Solver {

    private PriorityQueue<Node> p;
    private PriorityQueue<Node> q;
    private HashMap<Board, Node> hashedP = new HashMap<>();
    private HashMap<Board, Node> hashedQ = new HashMap<>();
    private HashMap<Board, Node> pClosed = new HashMap<>();
    private HashMap<Board, Node> qClosed = new HashMap<>();
    private int[] start;
    private final int N;
    private Node solution;
    private Node qSol;
    private final int FORWARDS = 0;
    private final int BACKWARDS = 1;
    private final int INFINITY = Integer.MAX_VALUE;
    private int pMin;
    private int qMin;
    int dMin = INFINITY;

    /**
     * The constructor (and current driver) for the solver class. Initializes bidirectional
     * search.
     *
     * @param initial The board to be solved.
     */
    public Solver(Board initial) {
        p = new PriorityQueue<>(10, (Node o1, Node o2) -> getPriority(o1, FORWARDS) - getPriority(o2, FORWARDS));
        q = new PriorityQueue<>(10, (Node o1, Node o2) -> getPriority(o1, BACKWARDS) - getPriority(o2, BACKWARDS));
        N = initial.size();
        start = get1DTilePositions(initial);

        // The starting nodes from both sides of the search space.
        Node current = new Node(initial, null, 0, null);
        Node reverse = new Node(finishedBoard(), null, 0, null);
        pMin = getPriority(current, FORWARDS);
        reverse.priority = pMin;
        qMin = pMin;
        addAndHash(current, FORWARDS);
        addAndHash(reverse, BACKWARDS);

        int dir = FORWARDS;

        int min = pMin;
        PriorityQueue<Node> open = p;
        PriorityQueue<Node> rev = q;
        HashMap<Board, Node> closed = pClosed;
        HashMap<Board, Node> rClosed = qClosed;
        boolean switchSides = false;

        // Loop handles search from both ends.
        while (p.size() > 0 && q.size() > 0) {
            if (switchSides) {
                if (dir == FORWARDS) {
                    min = pMin;
                    open = p;
                    rev = q;
                    closed = pClosed;
                    rClosed = qClosed;

                } else {
                    min = qMin;
                    open = q;
                    rev = p;
                    closed = qClosed;
                    rClosed = pClosed;
                }
                switchSides = false;
            }

            // Simple check to break the search if one end finds the other.
            current = pollHash(dir);
            if (getPriority(current, dir) == current.order){
                solution = current;
                break;
            }
            closed.put(current.board, current);

            // Prunes opposite end of search space if some nodes are shared.
            if (dMin > 0) {
                if (rClosed.containsKey(current.board)) {
                    for (Board b : current.board.neighbors()) {
                        dehashAndDel(b, dir);
                    }
                }
            }

            getNeighborsAndCost(current, dir);
            int newF = getPriority(current, dir);

            // This handles switching logic. Only switches if the min priority of the current
            // tree has gone up, and the current search tree is larger than its counterpart.
            if (newF > min) {
                min = newF;
                if (dir == FORWARDS) {
                    pMin = min;
                } else qMin = min;

                if (open.size() > rev.size()) {
                    dir = (dir + 1) % 2;
                    switchSides = true;
                }
            }

        }
        // Contains the solution in holder for solution() method.
        Node n = qSol;
        Node t;
        Node f = solution.prev;
        while (n != null) {
            t = n.prev;
            n.prev = f;
            n.order = n.prev.order + 1;
            f = n;
            n = t;
        }
        qSol = f;
    }


    /**
     * Gets moves taken to solve.
     *
     * @return The number of moves taken to solve.
     */
    public int moves() { return (qSol == null) ? solution.order : qSol.order; }

    /**
     * Gets solution from the final board, returning a stack with the
     * first board on top.
     *
     * @return The solution, in move order.
     */
    public Iterable<Board> solution(){
        Stack<Board> boards = new Stack<>();
        Node current = (qSol == null) ? solution : qSol;
        while (current != null){
            boards.push(current.board);
            current = current.prev;
        }
        return boards;
    }

    // 2D to 1D conversion
    private int[] get1DTilePositions(Board b){
        int[] tiles = new int[N*N];
        for (int i = 0; i < N; i++){
            for (int j = 0; j < N; j++){
                tiles[b.tileAt(i, j)] = i*N + j;
            }
        }
        return tiles;
    }

    // Makes finished board for the starting point of the
    // other end of the search.
    private Board finishedBoard() {
        int[][] f = new int[N][N];
        for (int i = 0; i < N; i++){
            for (int j = 0; j < N; j++){
                f[i][j] = i*N + j + 1;
            }
        }
        f[N-1][N-1] = 0;
        return new Board(f);
    }

    // Handles board neighbor finding and tree pruning conditions.
    // I put this here instead of in Board because I needed to be able
    // to access search parameters while considering neighbors.
    private void getNeighborsAndCost(Node n, int dir){
        ArrayList<Node> neighbors = new ArrayList<>();
        Board b = n.board;
        boolean trimTree = false;
        int blankRow = 0;
        int blankCol = 0;

        zFind: for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (b.tileAt(i, j) == 0){
                    blankRow = i;
                    blankCol = j;
                    break zFind;
                }
            }
        }
        for (Direction d : Direction.values()) {
            if (!isValid(blankRow + d.vShift, blankCol + d.hShift) || inverseDirections(n, d)) continue;
            int[][] copy = new int[N][N];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    copy[i][j] = b.tileAt(i, j);
                }
            }
            swap(copy, blankRow, blankCol, blankRow + d.vShift, blankCol + d.hShift);
            neighbors.add(new Node(new Board(copy), n, n.order + 1, d));
        }
            for (Node neighbor : neighbors) {
                Node t;
                if ((t = inTree(neighbor, dir)) == null) {
                    Node r;
                    getQuickPriority(neighbor, blankRow, blankCol, dir);
                    int heurMax = biMax(neighbor, dir);
                    if (heurMax < dMin) {
                        addAndHash(neighbor, dir);
                        r = inOtherTree(neighbor, dir);
                        if (r != null && r.order + neighbor.order < dMin) {
                            dMin = r.order + neighbor.order;
                            solution = (dir == FORWARDS) ? neighbor : r;
                            qSol = (dir == FORWARDS) ? r : neighbor;
                            trimTree = true;
                        }
                    }
                }
                // Handles cases where boards are refound after a smaller series
                // of moves
                else {
                    if (neighbor.order < t.order) {
                        t.priority -= t.order - neighbor.order;
                        t.order = neighbor.order;
                        t.prev = neighbor.prev;
                        HashMap<Board, Node> closed = (dir == FORWARDS) ? pClosed : qClosed;
                        if (closed.containsKey(t.board)) {
                            closed.remove(t.board);
                            addAndHash(t, dir);
                        }
                    }
                }
            }
            // Prunes if new best is found.
        if (trimTree) trimTree();
    }

    private boolean inverseDirections(Node n, Direction d){
        if (n.last == null) return false;
        if (n.last == Direction.RIGHT) return d == Direction.LEFT;
        if (n.last == Direction.DOWN) return d == Direction.UP;
        if (n.last == Direction.UP) return d == Direction.DOWN;
        return (n.last == Direction.LEFT && d == Direction.RIGHT);
    }

    // The heuristic used. Gets the max of either the priority or the minimum heuristic distance
    // measured from the other side of the search space.
    private int biMax(Node n, int dir){
        int min = (dir == FORWARDS) ? qMin : pMin;
        return Math.max(getPriority(n, dir), min + (n.order - computeManhattan(n, ~dir & 1, false)));
    }

    private void swap(int[][] tiles, int r1, int c1, int r2, int c2){
        int c = tiles[r1][c1];
        tiles[r1][c1] = tiles[r2][c2];
        tiles[r2][c2] = c;
    }

    private boolean isValid(int r, int c){
        return (r >= 0 && r < N && c >= 0 && c < N);
    }

    // Uses hashing for constant in checks.
    private Node inTree(Node n, int dir){

        HashMap<Board, Node> closed = (dir == FORWARDS) ? pClosed : qClosed;
        if (closed.containsKey(n.board)) return closed.get(n.board);
        dehashAndDel(n, dir);

        return null;
    }

    private Node inOtherTree(Node n, int dir){
        HashMap<Board, Node> closed = (dir == FORWARDS) ? qClosed : pClosed;
        HashMap<Board, Node> pqHash = (dir == FORWARDS) ? hashedQ : hashedP;

        
        if (closed.containsKey(n.board)) { return closed.get(n.board);}
        if (pqHash.containsKey(n.board)) { return pqHash.get(n.board);}
        return null;
    }

    // Trims boards that fail bimax check
    private void trimTree(){
        ArrayList<Node> removeP = new ArrayList<>();
        ArrayList<Node> removeQ = new ArrayList<>();
        for (Node node : p){
            if (biMax(node, FORWARDS) >= dMin) {
                
                hashedP.remove(node.board);
                
                removeP.add(node);
            }
        }

        for (Node node : q){
            if (biMax(node, BACKWARDS) >= dMin) {
                hashedQ.remove(node.board);
                removeQ.add(node);
            }
        }
        p.removeAll(removeP);
        q.removeAll(removeQ);
    }


    // Utilizes the fact that to find a new manhattan distance, only
    // the previous one is needed, in addition to the tile moved.
    private void getQuickPriority(Node n, int row, int col, int d){
        Direction dir = n.last;
        int i = n.board.tileAt(row, col);
        if (dir == Direction.UP || dir == Direction.DOWN){
            int firstRow = (d == FORWARDS) ? (i - 1) / N : start[i] / N;
            n.priority = (Math.abs(row + n.last.vShift - firstRow) > Math.abs(row - firstRow))
                    ? n.prev.priority : n.prev.priority + 2;

        }
        else {
            int firstCol = (d == FORWARDS) ? (i - 1) % N : start[i] % N;
            n.priority = (Math.abs(col + n.last.hShift - firstCol) > Math.abs(col - firstCol))
                    ? n.prev.priority : n.prev.priority + 2;
        }
    }

    private int getPriority(Node n, int dir){
        if (n.priority == -1) return n.priority = computeManhattan(n, dir, true) + n.order;
        return n.priority;
    }

    private int computeManhattan(Node n, int d, boolean cache){
        int totalDist = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int t = n.board.tileAt(i, j);
                if (t != 0) {
                    if (d == FORWARDS) {
                        totalDist += Math.abs(((t - 1) / N) - i);
                        totalDist += Math.abs(((t - 1) % N) - j);
                    }
                    else {
                        totalDist += Math.abs((start[t] / N) - i);
                        totalDist += Math.abs((start[t] % N) - j);
                    }
                }
            }
        }
        if (cache) {
            n.priority = totalDist + n.order;
        }
        return totalDist;

    }
    private void addAndHash(Node n, int dir){
        HashMap<Board, Node> pqHash = (dir == FORWARDS) ? hashedP : hashedQ;
        PriorityQueue<Node> pq = (dir == FORWARDS) ? p : q;

        pqHash.put(n.board, n);
        
        pq.add(n);
    }

    private Node pollHash(int dir){
        HashMap<Board, Node> pqHash = (dir == FORWARDS) ? hashedP : hashedQ;
        PriorityQueue<Node> pq = (dir == FORWARDS) ? p : q;

        Node n = pq.poll();
        pqHash.remove(n.board);
        
        return n;
    }

    private void dehashAndDel(Node n, int dir){
        HashMap<Board, Node> pqHash = (dir == FORWARDS) ? hashedP : hashedQ;
        PriorityQueue<Node> pq = (dir == FORWARDS) ? p : q;

        
        Node node = pqHash.remove(n.board);
        
        pq.remove(node);
    }

    private void dehashAndDel(Board b, int dir){
        HashMap<Board, Node> pqHash = (dir == FORWARDS) ? hashedP : hashedQ;
        PriorityQueue<Node> pq = (dir == FORWARDS) ? p : q;

        
        Node node = pqHash.remove(b);
        
        pq.remove(node);
    }

    private enum Direction {
        UP (-1, 0),
        LEFT (0, -1),
        RIGHT (0, 1),
        DOWN (1, 0);

        private final int hShift;
        private final int vShift;

        Direction(int vShift, int hShift) {
            this.hShift = hShift;
            this.vShift = vShift;
        }
    }

    private class Node{
        int order;
        final Direction last;
        final Board board;
        Node prev;
        int priority = -1;

        private Node(Board board, Node prev, int order, Direction last){
            this.board = board;
            this.prev = prev;
            this.order = order;
            this.last = last;
        }

    }
    public static void main(String[] args) {
        // create initial board from file
        In in = new In("res/p4data/puzzle49.txt");
        int N = in.readInt();
        int[][] tiles = new int[N][N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                tiles[i][j] = in.readInt();
        Board initial = new Board(tiles);

        // check if puzzle is solvable; if so, solve it and output solution
        if (initial.isSolvable()) {
            NanoWatch clock = new NanoWatch();
            Solver solver = new Solver(initial);
            StdOut.println(clock.readTime());
            StdOut.println("Minimum number of moves = " + solver.moves());
            for (Board board : solver.solution())
                StdOut.println(board);
        }

        // if not, report unsolvable
        else {
            StdOut.println("Unsolvable puzzle");
        }
    }
}
