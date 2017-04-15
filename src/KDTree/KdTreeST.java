package KDTree;

import edu.princeton.cs.algs4.*;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Simon on 4/4/2017.
 */

public class KdTreeST<Value> {
    private kdNode root;
    private int size;
    public static int depth;
    private List<kdNode> preSorted = new ArrayList<>(1000);
    private boolean treeMade;
    private final boolean BALANCE_TREE = false;
    private final int     SEARCH_CUTOFF = 10;

    public KdTreeST()   {
        treeMade = !BALANCE_TREE;
    }

    public boolean isEmpty()           { return size == 0; }
    public int size()                  { return size; }
    public boolean contains(Point2D p) { return get(p) != null; }

    // Drives put method for balanced and unbalanced trees.
    public void put(Point2D p, Value val) {
        if (p == null || val == null) throw new NullPointerException();
        if (BALANCE_TREE) putB(p, val);
        else putNB(p, val);
    }

    // Dynamically places in unbalanced tree
    private void putNB(Point2D p, Value val) {
        if (size++ == 0) {
            depth++;
            root = new kdNode(p, val, 0);
            return;
        }
        int localDepth = 1;
        kdNode n = root;
        kdNode prev = root;
        int a = 0;
        boolean left = false;

        while (n != null){
            prev = n;
            if (left = axisValue(p, a) < n.getSplit()) { n = n.lt; }
            else { n = n.gt; }
            localDepth++;
            a = (a + 1) % 2;
        }

        n = new kdNode(p, val, a);
        if (localDepth > depth) depth = localDepth;
        size++;
        if (left) prev.lt = n;
        else prev.gt = n;
    }


    // Stores point in array for later balancing.
    private void putB(Point2D p, Value val) {
        preSorted.add(new kdNode(p, val, 0));
        size++;
    }


    // Drives nearest for both balanced and unbalanced trees.
    public Point2D nearest(Point2D p){
        if (p == null) throw new NullPointerException();
        if (!treeMade) {
            root = makeTree();
            treeMade = true;
        }
        return getNearest(p);
    }



    // Get k nearest, works fine with boids
    public Iterable<Point2D> nearest(Point2D p, int k){
        int aprxCount = SEARCH_CUTOFF;

        PriorityQueue<Point2D> pq = new PriorityQueue<>(k*2 + 1,
                (Point2D p1, Point2D p2) -> (p.distanceSquaredTo(p1) < p.distanceSquaredTo(p2)) ? -1 : 1);

        if (!treeMade) {
            root = makeTree();
            treeMade = true;
        }

        Stack<kdNode> up = new Stack<>();
        Stack<kdNode> down = new Stack<>();
        kdNode n = root;
        kdNode nearest = root;
        double d;

        while (n != null){
            nearest = n;
            up.push(n);
            if (axisValue(p, n.axis) < n.getSplit()) { n = n.lt; }
            else { n = n.gt; }
        }
        pq.add(nearest.point);
        d = (nearest.point.distanceSquaredTo(p) == 0) ? Double.POSITIVE_INFINITY : nearest.point.distanceSquaredTo(p);

        while (!up.isEmpty()){
            if (aprxCount-- <= 0 && pq.size() == k) { return pq;}
            kdNode parent = up.pop();
            kdNode check;
            check = (axisValue(p, parent.axis) < parent.getSplit()) ? parent.gt : parent.lt;
            if (pq.size() < k || p.distanceSquaredTo(parent.point) < d) {
                if (!parent.point.equals(p)) pq.add(parent.point);
                if (pq.size() > k) d = pq.poll().distanceSquaredTo(p);
            }
            if (pq.size() < k || d > Math.abs(parent.getSplit() - axisValue(p, parent.axis))) {
                if (check != null) {
                    down.push(check);
                    while (!down.isEmpty()) {
                        kdNode current = down.pop();
                        if (current.lt != null && axisValue(p, current.axis) < current.getSplit())
                            down.push(current.lt);
                        else if (current.gt != null && axisValue(p, current.axis) >= current.getSplit())
                            down.push(current.gt);
                        up.push(current);
                    }
                }
            }
        }
        return pq;
    }

    // Nearest neighbor. Considers only branches whose splitting axes
    // are within the shortest distance found so far.
    private Point2D getNearest(Point2D p){

        int aprxCount = SEARCH_CUTOFF;

        Stack<kdNode> up = new Stack<>();
        Stack<kdNode> down = new Stack<>();
        kdNode n = root;
        kdNode nearest = root;
        double d;

        while (n != null){
            nearest = n;
            up.push(n);
            if (axisValue(p, n.axis) < n.getSplit()) { n = n.lt; }
            else { n = n.gt; }
        }
        d = nearest.point.distanceTo(p);
        while (!up.isEmpty()){
            if (aprxCount-- <= 0) {return nearest.point;}
            double temp;
            kdNode parent = up.pop();
            kdNode check;
            check = (axisValue(p, parent.axis) < parent.getSplit()) ? parent.gt : parent.lt;
            if ((temp = p.distanceSquaredTo(parent.point)) < d) {

                nearest = parent;
                d = temp;
            }
            if (d > Math.abs(parent.getSplit() - axisValue(p, parent.axis))) {
                if (check != null) {
                    down.push(check);
                    while (!down.isEmpty()) {
                        kdNode current = down.pop();
                        if (current.lt != null && axisValue(p, current.axis) < current.getSplit())
                            down.push(current.lt);
                        else if (current.gt != null && axisValue(p, current.axis) >= current.getSplit())
                            down.push(current.gt);
                        up.push(current);
                    }
                }
            }
        }
        return nearest.point;
    }


    // Returns balanced tree
    private kdNode makeTree() { return makeTree(preSorted, 0); }


    // Tree balancing method. For large subarrays, finds the median of 100 random points
    // to act as the partition splitter and parent node. Smaller subarrays simply sort and
    // use the true median.
    private kdNode makeTree(List<kdNode> rq, int depth){
        if (depth + 1 > this.depth) this.depth = depth + 1;
        if (rq.size() > 1000){
            Comparator<kdNode> cmp = (kdNode n1, kdNode n2) -> ((Double) axisValue(n1.point, depth % 2)).compareTo(axisValue(n2.point, depth % 2));
            TreeSet<kdNode> random = new TreeSet<>(cmp);
            while (random.size() < 100){
                random.add(rq.get(StdRandom.uniform(rq.size())));
            }
            List<kdNode> sorted = random.stream().collect(Collectors.toList());
            sorted.sort(cmp);
            final kdNode med = sorted.get(50);
            med.axis = depth % 2;
            med.lt = makeTree(rq.stream().filter((kdNode -> cmp.compare(kdNode, med) < 0)).collect(Collectors.toList()), depth + 1);
            med.gt = makeTree(rq.stream().filter((kdNode k) -> cmp.compare(k, med) >= 0 && !med.equals(k)).collect(Collectors.toList()), depth + 1);
            return med;
        }

        if (rq.size() > 2){
            Comparator<kdNode> cmp = (kdNode n1, kdNode n2) ->
                    ((Double) axisValue(n1.point, depth % 2)).compareTo(axisValue(n2.point, depth % 2));
            rq.sort(cmp);
            kdNode med = rq.get(rq.size()/2);
            int i = rq.size()/2;
            while (i > 0 && cmp.compare(rq.get(i - 1), med) == 0){ med = rq.get(--i); }
            med.axis = depth % 2;
            med.lt = makeTree(rq.subList(0, i), depth + 1);
            med.gt = makeTree(rq.subList(i + 1, rq.size()), depth + 1);
            return med;
        }
        else if (rq.size() == 2) {
            int med = (axisValue(rq.get(0).point, depth % 2) < axisValue(rq.get(1).point, depth % 2)) ? 0 : 1;
            rq.get(med).axis = depth % 2;
            rq.get(med).gt = rq.get((med + 1) % 2);
            rq.get(med).gt.axis = (depth + 1) % 2;
            return rq.get(med);
        }
        else {
            if (rq.size() == 0) return null;
            rq.get(0).axis = depth % 2;
            return rq.get(0);
        }
    }

    // Traverses tree in the same manner as the 'put' method and
    // returns the query point value when found.
    public Value get(Point2D p) {
        kdNode n = root;
        int a = 0;

        while (n != null){
            if (n.point.equals(p)) return n.val;
            if (axisValue(p, a) < n.getSplit()) { n = n.lt; }
            else { n = n.gt; }
            a = (a + 1) % 2;
        }
        return null;
    }

    // Returns iterable points, using a queue to force
    // row major order.
    public Iterable<Point2D> points() {
        if (!treeMade) {
            root = makeTree();
        }
        Queue<kdNode> pointQ = new Queue<>();
        ArrayList<Point2D> points = new ArrayList<>();
        points.add(root.point);
        pointQ.enqueue(root);
        while (!pointQ.isEmpty()){
            kdNode n = pointQ.dequeue();

            if (n.lt != null) {
                pointQ.enqueue(n.lt);
                points.add(n.lt.point);
            }
            if (n.gt != null) {
                pointQ.enqueue(n.gt);
                points.add(n.gt.point);
            }
        }
        return points;
    }

    // Returns range of points, checking at each
    // branch for relative position of rectangle.
    public Iterable<Point2D> range(RectHV rect){
        if (!treeMade) {
            root = makeTree();
            treeMade = true;
        }

        Queue<kdNode> pointQ = new Queue<>();
        ArrayList<Point2D> points = new ArrayList<>();
        kdNode n;
        pointQ.enqueue(root);

        while (!pointQ.isEmpty()){
            n = pointQ.dequeue();
            if (rect.contains(n.point)) points.add(n.point);
            switch (compareRectAndSplit(rect, n)) {
                case -1: { enqueue(pointQ, n.lt); break;}
                case  0: { enqueue(pointQ, n.lt); enqueue(pointQ, n.gt); break;}
                case  1: { enqueue(pointQ, n.gt); break;}
            }
        }
        return points;
    }

    private void enqueue(Queue<kdNode> q, kdNode n){ if (n != null) q.enqueue(n); }

    private double axisValue(Point2D p, int a) { return (a == 0) ? p.x() : p.y(); }

    private int compareRectAndSplit(RectHV rect, kdNode n){
        double min = (n.axis == 0) ? rect.xmin() : rect.ymin();
        double max = (n.axis == 0) ? rect.xmax() : rect.ymax();
        if (max >= n.getSplit() && min < n.getSplit()) return 0;
        else return (max < n.getSplit()) ? -1 : 1;
    }

    private class kdNode {

        private final Point2D point;
        private final Value val;
        private int axis;
        private kdNode lt;
        private kdNode gt;

        private kdNode(Point2D point, Value val, int axis){
            this.point = point;
            this.val = val;
            this.axis = axis;
        }

        private double getSplit(){
            return (axis == 0) ? point.x() : point.y();
        }
    }


    public static void main(String[] args){
        String filename = args[0];
        In in = new In(filename);

        // initialize the two data structures with point from standard input
        PointST<Integer> brute = new PointST<Integer>();
        KdTreeST<Integer> kdtree = new KdTreeST<Integer>();
        Point2D[] shuffled = new Point2D[100000];
        for (int i = 0; !in.isEmpty(); i++) {
            double x = in.readDouble();
            double y = in.readDouble();
            Point2D p = new Point2D(x, y);

            shuffled[i] = p;
            //brute.put(p, i);
            kdtree.put(p, i);
        }
        StdRandom.shuffle(shuffled);
        long count = 0;

        if (!kdtree.treeMade) { kdtree.root = kdtree.makeTree(); kdtree.treeMade = true; }
        NanoWatch clock = new NanoWatch();
        clock.reset();
        timed: while (true) {
            StdRandom.shuffle(shuffled);
            clock.start();
            for (Point2D point : shuffled) {
                kdtree.nearest(point);
                count++;
                if (clock.readTime() > 60000000000L) break timed;
            }
            clock.stop();
        }
        StdOut.println(count / 60 + " operations/s.");
    }
}

