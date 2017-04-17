package KDTree;

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;

import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Created by Simon on 4/4/2017.
 */
public class PointST<Value> {

    private final TreeMap<Point2D, Value> points;
    public PointST(){
        this.points = new TreeMap<>();
    }

    public boolean isEmpty()              { return points.size() == 0; }
    public int size()                     { return points.size(); }
    public void put(Point2D p, Value val) { if (p == null || val == null) throw new NullPointerException(); points.put(p, val); }
    public Value get(Point2D p)           { if (p == null) throw new NullPointerException(); return points.get(p); }
    public boolean contains(Point2D p)    { if (p == null) throw new NullPointerException(); return points.containsKey(p); }
    public Iterable<Point2D> points()     { return points.keySet(); }

    // Java8 is kind of nifty.
    public Iterable<Point2D> range(RectHV rect) {
        return points.keySet().parallelStream().filter(rect::contains).collect(Collectors.toList());
    }
    public Point2D nearest(Point2D p) {
        if (p == null) throw new NullPointerException();
        Point2D c = null;
        double min = Double.MAX_VALUE;
        for (Point2D point : points.keySet()){
            double squared = point.distanceSquaredTo(p);
            if (squared < min) {
                min = squared;
                c = point;
            }
        }
        return c;
    }
}
