package SeamCarver;

import edu.princeton.cs.algs4.*;
import KDTree.NanoWatch;

import java.awt.*;

/**
 * Created by Simon on 4/7/2017.
 */

    // This utilizes the expected implementation, for the most part.
    // I tested several different implementations, and frankly, none of them
    // held up as well as I wanted to. Bidirectional searches' overhead is too great,
    // a common issue that is relevant for many of my ideas for this project. Ultimately,
    // I used a quick four-quadrant heuristic to get upper bounds for each direction,
    // taking the minimum of paths reached from local minima reached from quadrants spanning
    // each end of the search area.

public class SeamCarver {

    private Picture picture;
    private boolean transposed = false;

    private int height;
    private int width;
    private NanoWatch clock = new NanoWatch();
    private int[][] color;
    private int[][] energy;
    private int[] upperBoundPath;
    private final int INFINITY = Integer.MAX_VALUE;
    private final boolean USE_LAPLACIAN = false;

    public SeamCarver(Picture picture) {
        this.picture = picture;
        this.width = picture.width();
        this.height = picture.height();
        this.energy = new int[height][width];
        this.color = new int[height][width];

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                color[i][j] = picture.get(j, i).getRGB();
            }
        }
        if (USE_LAPLACIAN) ppLaplace();
        else ppGradient();
    }

    public     int width()  { return (!transposed) ? width : height; }
    public     int height() { return (!transposed) ? height : width; }

    public  double energy(int x, int y) {
        return (!transposed) ? energy[y][x] : energy[x][y];
    }

    // Returns picture, forgoing transposition of the underlying matrix
    // in favor of live transposition upon picture instantiation.

    public Picture picture() {
        int h = (transposed) ? width : height;
        int w = (transposed) ? height : width;
        Picture pic = new Picture(w, h);

        if (!transposed) {
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    pic.set(j, i, new Color(color[i][j]));
                }
            }
        }
        else for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                pic.set(j, i, new Color(color[j][i]));
            }
        }
            return pic;
    }


    public int[] findHorizontalSeam() {
        return (!transposed) ? findHSeam() : findVSeam();
    }
    public int[] findVerticalSeam() {
        return (!transposed) ? findVSeam() : findHSeam();
    }

    // Finds horizontal seam. Utilizes simple topological sort,
    // incorporating tree pruning taking advantage of 'a priori'
    // knowledge of path structure.

    public   int[] findHSeam() {
        int hUpperBound = (height > 50) ? getHUpperBound() : INFINITY;
        int[][] adj = new int[height][width];
        int[][] dist = new int[height][width];
        boolean[][] marked = new boolean[height][width];
        int[][] stepPointer = new int[height][width];

        for (int i = 0; i < height; i++) {
            stepPointer[i][0] = i;
            dist[i][0] = energy[i][0];
        }

        int step = 0;
        int count = height;
        while (step < width - 1) {
            int temp = 0;
            for (int i = 0; i < count; i++) {
                int pointer = stepPointer[i][step];
                int path = dist[pointer][step];
                for (int j = -1; j < 2; j++) {
                    if (pointer + j < 0 || pointer + j >= height) continue;
                    int distTo = path + energy[pointer + j][step + 1];
                    if (distTo <= hUpperBound) {
                        if (!marked[pointer + j][step + 1]) {
                            dist[pointer + j][step + 1] = distTo;
                            adj[pointer + j][step + 1] = pointer;
                            marked[pointer + j][step + 1] = true;
                            stepPointer[temp++][step + 1] = pointer + j;
                        } else if (dist[pointer + j][step + 1] > distTo) {
                            dist[pointer + j][step + 1] = distTo;
                            adj[pointer + j][step + 1] = pointer;
                        }

                    }
                }
            }
            count = temp;
            step++;
        }
        int min = INFINITY;
        int place = 0;
        for (int i = 0; i < count; i++) {
            int pointer = stepPointer[i][width - 1];
            if (dist[pointer][width - 1] < min) {
                place = i;
                min = dist[pointer][width - 1];
            }
        }
        int[] seam = new int[width];
        for (int i = width - 1; i > -1; i--) {
            seam[i] = place;
            place = adj[place][i];
        }
        return seam;
    }

    // Gets horizontal upper bound for pruning.
    private int getHUpperBound(){
        int[] quads = {height / 8, 3 * height / 8, 5 * height / 8, 7 * height / 8};
        int boundMin = INFINITY;
        for (int quad : quads) {
            int boundL = 0;
            int boundR = 0;
            int rowL = quad;
            int rowR = quad;

            for (int i = 0; i < width - 1; i++) {
                int min = INFINITY;
                int place = rowL;
                for (int j = -1; j < 2; j++) {
                    if (rowL + j < 0 || rowL + j >= height) continue;
                    if (energy[rowL + j][i + 1] < min) {
                        place = rowL + j;
                        min = energy[rowL + j][i + 1];
                    }
                }
                rowL = place;
                boundL += min;
            }
            if (boundL < boundMin) { boundMin = boundL;  }

            for (int i = width - 1; i > 0; i--){
                int min = INFINITY;
                int place = rowR;
                for (int j = -1; j < 2; j++){
                    if (rowR + j < 0 || rowR + j >= height) continue;
                    if (energy[rowR + j][i - 1] < min){
                        place = rowR + j;
                        min = energy[quad + j][i - 1];
                    }
                }
                quad = place;
                boundR += min;
            }
            if (boundR < boundMin) { boundMin = boundR; }
        }
        return boundMin;
    }

    // Finds vertical seam. This method could probably
    // be consolidated with the horizontal method, but
    // frankly I am lazy, and this was easier.

    public   int[] findVSeam() {
        int vUpperBound = (width > 50) ? getVUpperBound() : INFINITY;
        int[][] adj = new int[height][width];
        int[][] dist = new int[height][width];
        boolean[][] marked = new boolean[height][width];
        int[][] stepPointer = new int[height][width];

        for (int i = 0; i < width; i++) {
            stepPointer[0][i] = i;
            dist[0][i] = energy[0][i];
        }

        int step = 0;
        int count = width;
        while (step < height - 1) {
            int temp = 0;
            for (int i = 0; i < count; i++) {
                int pointer = stepPointer[step][i];
                int path = dist[step][pointer];
                for (int j = -1; j < 2; j++) {
                    if (pointer + j < 0 || pointer + j >= width) continue;
                    int distTo = path + energy[step + 1][pointer + j];
                    if (distTo <= vUpperBound) {
                        if (!marked[step + 1][pointer + j]) {
                            dist[step + 1][pointer + j] = distTo;
                            adj[step + 1][pointer + j] = pointer;
                            marked[step + 1][pointer + j] = true;
                            stepPointer[step+1][temp++] = pointer + j;
                        } else if (dist[step + 1][pointer + j] > distTo) {
                            dist[step + 1][pointer + j] = distTo;
                            adj[step + 1][pointer + j] = pointer;
                        }

                    }
                }
            }
            count = temp;
            step++;
        }
        int min = INFINITY;
        int place = 0;
        for (int i : stepPointer[height - 1]) {
            if (dist[height - 1][i] < min) {
                place = i;
                min = dist[height - 1][i];
            }
        }
        int[] seam = new int[height];
        for (int i = height - 1; i > -1; i--) {
            seam[i] = place;
            place = adj[i][place];
        }
        return seam;
    }

    // Gets a vertical upper bound, for pruning.
    private int getVUpperBound(){
        int[] quads = {width / 8, 3 * width / 8, 5 * width / 8, 7 * width / 8};
        int boundMin = INFINITY;
        for (int quad : quads) {
            int boundL = 0;
            int boundR = 0;
            int colL = quad;
            int colR = quad;

            for (int i = 0; i < height - 1; i++) {
                int min = INFINITY;
                int place = colL;
                for (int j = -1; j < 2; j++) {
                    if (colL + j < 0 || colL + j >= width) continue;
                    if (energy[i + 1][colL + j] < min) {
                        place = colL + j;
                        min = energy[i + 1][colL + j];
                    }
                }
                colL = place;
                boundL += min;
            }
            if (boundL < boundMin) { boundMin = boundL; }

            for (int i = height - 1; i > 0; i--) {
                int min = INFINITY;
                int place = colR;
                for (int j = -1; j < 2; j++) {
                        if (colR + j < 0 || colR + j >= width) continue;
                    if (energy[i - 1][colR + j] < min) {
                        place = colR + j;
                        min = energy[i - 1][colR + j];
                    }
                }
                colR = place;
                boundR += min;
            }
            if (boundR < boundMin) { boundMin = boundR; }
        }
        return boundMin;
    }


    // Horizontal seam removal triggers a transposition,
    // as the energy/color matrices are recorded in row
    // major order.
    public    void removeHorizontalSeam(int[] seam) {
        if (!transposed) transpose();
        removeSeam(seam);
    }
    public    void removeVerticalSeam(int[] seam) {
        if (transposed) transpose();
        removeSeam(seam);
    }

    // Vertical seam removal.

    private void removeSeam(int[] seam){
        for (int i = 0; i < height; i++){
            if (seam[i] != width - 1) {
                System.arraycopy(color[i], seam[i] + 1, color[i], seam[i], width - seam[i] - 1);
                System.arraycopy(energy[i], seam[i] + 1, energy[i], seam[i], width - seam[i] - 1);
            }
        }
        width--;
        if (USE_LAPLACIAN) {
            for (int i = 0; i < height; i++) {
                reLaplace(seam[i] - 1, i);
                reLaplace(seam[i], i);
            }
        }
        else {
            for (int i = 0; i < height; i++) {
                reGradient(seam[i] - 1, i);
                reGradient(seam[i], i);
            }
        }
    }

    // This is called only when horizontal seams are removed;
    // if a vertical seam is removed, it is done before the
    // horizontal, as to minimize transpositions.

    private void transpose() {
        int[][] tPic = new int[width][height];
        int[][] tNrg = new int[width][height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tPic[j][i] = color[i][j];
                tNrg[j][i] = energy[i][j];
            }
        }
        int c = height;
        transposed = !transposed;
        height = width;
        width = c;
        color = tPic;
        energy = tNrg;
    }

    // I want to work on this a bit more, possibly for future portfolios.
    // This method calculates energy by a pseudo-Laplacian metric - essentially,
    // each pixel is compared by its summed second derivative in each dimension,
    // making "sources" and "sinks," e.g. local minimum and maximum colors, stand
    // out particularly. In practice, I actually think this looks better than the
    // gradient in many cases, although there are several unseemly artifacts that
    // I need to deal with in extreme cases.


    private void ppLaplace() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++){
                Color self = picture.get(j, i);
                Color up = (i != 0) ? picture.get(j, i - 1) : picture.get(j, height - 1);
                Color down = (i != height - 1) ? picture.get(j, i + 1) : picture.get(j, 0);
                Color left = (j != 0) ? picture.get(j - 1, i) : picture.get(width - 1, i);
                Color right = (j != width - 1) ? picture.get(j + 1, i) : picture.get(0, i);

                int ddR = Math.abs(2 * self.getRed() - up.getRed() - down.getRed()) +
                        Math.abs(2 * self.getRed() - left.getRed() - right.getRed());
                int ddB = Math.abs(2 * self.getBlue() - up.getBlue() - down.getBlue()) +
                        Math.abs(2 * self.getBlue() - left.getBlue() - right.getBlue());
                int ddG = Math.abs(2 * self.getGreen() - up.getGreen() - down.getGreen()) +
                        Math.abs(2 * self.getGreen() - left.getGreen() - right.getGreen());

                energy[i][j] = ddR + ddB + ddG;
            }
        }
    }

    private void reLaplace(int col, int row) {
        if (!isValid(col, row)) return;
        Color self = new Color(color[row][col]);
        Color up = (row != 0) ? new Color(color[row - 1][col]) : new Color(color[height - 1][col]);
        Color down = (row != height - 1) ? new Color(color[row + 1][col]) : new Color(color[0][col]);
        Color left = (col != 0) ? new Color(color[row][col - 1]) : new Color(color[row][width - 1]);
        Color right = (col != width - 1) ? new Color(color[row][col + 1]) : new Color(color[row][0]);

        int ddR = Math.abs(2 * self.getRed() - up.getRed() - down.getRed()) +
                Math.abs(2 * self.getRed() - left.getRed() - right.getRed());
        int ddB = Math.abs(2 * self.getBlue() - up.getBlue() - down.getBlue()) +
                Math.abs(2 * self.getBlue() - left.getBlue() - right.getBlue());
        int ddG = Math.abs(2 * self.getGreen() - up.getGreen() - down.getGreen()) +
                Math.abs(2 * self.getGreen() - left.getGreen() - right.getGreen());

        energy[row][col] = ddR + ddB + ddG;
    }

    private void ppGradient() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++){
                Color up = (i != 0) ? picture.get(j, i - 1) : picture.get(j, height - 1);
                Color down = (i != height - 1) ? picture.get(j, i + 1) : picture.get(j, 0);
                Color left = (j != 0) ? picture.get(j - 1, i) : picture.get(width - 1, i);
                Color right = (j != width - 1) ? picture.get(j + 1, i) : picture.get(0, i);
                double gX = Math.pow(right.getRed() - left.getRed(), 2) + Math.pow(right.getGreen() - left.getGreen(), 2)
                        + Math.pow(right.getBlue() - left.getBlue(), 2);
                double gY = Math.pow(up.getRed() - down.getRed(), 2) + Math.pow(up.getGreen() - down.getGreen(), 2)
                        + Math.pow(up.getBlue() - down.getBlue(), 2);

                energy[i][j] = (int) Math.sqrt(gX + gY);
            }
        }
    }

    private void reGradient(int col, int row) {
        if (!isValid(col, row)) return;
        Color up = (row != 0) ? new Color(color[row - 1][col]) : new Color(color[height - 1][col]);
        Color down = (row != height - 1) ? new Color(color[row + 1][col]) : new Color(color[0][col]);
        Color left = (col != 0) ? new Color(color[row][col - 1]) : new Color(color[row][width - 1]);
        Color right = (col != width - 1) ? new Color(color[row][col + 1]) : new Color(color[row][0]);
        double gX = Math.pow(right.getRed() - left.getRed(), 2) + Math.pow(right.getGreen() - left.getGreen(), 2)
                + Math.pow(right.getBlue() - left.getBlue(), 2);
        double gY = Math.pow(up.getRed() - down.getRed(), 2) + Math.pow(up.getGreen() - down.getGreen(), 2)
                + Math.pow(up.getBlue() - down.getBlue(), 2);
        energy[row][col] = (int) Math.sqrt(gX + gY);
    }

    private boolean isValid(int col, int row){
        return !(col < 0 || row < 0 || col >= width || row >= height);
    }

    public static void main(String[] args) {
        SeamCarver sc = new SeamCarver(new Picture("res/p6data/test1000V.png"));
        long t = System.currentTimeMillis();
        int count = 0;
        while (System.currentTimeMillis() - t < 5000) {
            sc.findVerticalSeam();
            count++;
        }
        StdOut.println(count / 5);
    }
}
