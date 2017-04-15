/******************************************************************************
 *  Name: Simon Fink
 *
 *  Operating system: Windows 10 / Linux Mint
 *  Compiler: Javac
 *  Text editor / IDE: IntelliJ IDEA
 *  Hours to complete assignment (optional):
 ******************************************************************************/



/******************************************************************************
 *  Describe the Node data type you used to implement the
 *  2d-tree data structure.
 *****************************************************************************/

As simple as necessary. Just two pointers to left and right children,
an axis integer, and the stored point/values. The class also wraps a simple
method for getting the axis aligned splitting coordinate.

/******************************************************************************
 *  Describe your method for range search in a kd-tree.
 *****************************************************************************/

The method was fairly simple to implement without use of RectHV members. Three
concise checks are done at each level to determine where the given RectHV lies
in relation to the splitting axis. If it intersects, both sides are checked,
and if not, only the side it lies on is traversed. At each level, a 'contains'
check is made on each point.

/******************************************************************************
 *  Describe your method for nearest neighbor search in a kd-tree.
 *****************************************************************************/

This differs slightly from the assignment, but I made a couple changes for efficient
implementation. I have removed all slow rectangle checks, replacing them with
simple 1D distance checks (explore other side if axis is within the range of the
shortest distance in the $AXIS direction). In practice, this marks a solid improvement,
although it does require distanceOf instead of squaredDistanceOf to ensure accuracy.

This isn't actually a problem, at least on relatively modern architecture; I have tested
the difference between distance and squared distance calculations, both here and previously
in my TSP solver, and the differences are negligible.

/******************************************************************************
 *  Using the 64-bit memory cost model from the textbook and lecture,
 *  give the total memory usage in bytes of your 2d-tree data structure
 *  as a function of the number of points N. Use tilde notation to
 *  simplify your answer (i.e., keep the leading coefficient and discard
 *  lower-order terms).
 *
 *  Include the memory for all referenced objects (including
 *  Node, Point2D, and RectHV objects) except for Value objects
 *  (because the type is unknown). Also, include the memory for
 *  all referenced objects.
 *
 *  Justify your answer below.aa
 *
 *****************************************************************************/

bytes per Point2D:
16 byte object overhead + 4*2 bytes for doubles + 3*8 bytes for comparator references = 48 bytes

bytes per RectHV:
16 byte object overhead + 4*4 bytes for doubles = 32 bytes

bytes per KdTree of N points:   ~N*(16 OH + 3*8 refs + 4*1 int + V) = ~(44 + V)*N





/******************************************************************************
 *  How many nearest neighbor calculations can your brute-force
 *  implementation perform per second for input100K.txt (100,000 points)
 *  and input1M.txt (1 million points), where the query points are
 *  random points in the unit square? Explain how you determined the
 *  operations per second. (Do not count the time to read in the points
 *  or to build the 2d-tree.)
 *
 *  Repeat the question but with the 2d-tree implementation.
 *****************************************************************************/

This one I don't understand, at all. I decided to optimize my KD tree by balancing before performing
operations, which I assumed would result in some speedup. According to all of my checks, it SHOULD.
The max depth of my balanced tree on the input1M type is 21, versus the non balanced ~55. The average
depth, across all inputs, is consistently 70-80% that of the non balanced tree. I even checked
the average number of "hops" made from level to level; the non balanced tree consistently makes
more average jumps and checks than the balanced one.

Despite this... there is no noticeable performance difference whatsoever. The non balanced one actually appears
to have slightly BETTER performance in most cases. I don't understand, and I don't think I'm making
any mistakes, but maybe you can spot something dumb I did.

One thing I did do that resulted in noticeable speedup was to institute approximation cutoffs for neighbor
checks. This simply limits the number of other tree branches traversed, and helps especially with larger
input files.

Ops/sec determined by a system time terminated while loop, iterating continuously over a shuffled
list of points over 60s.

                       calls to nearest() per second
                     brute force               2d-tree (basic)          2d-tree (balanced w/ approximation)
                     -------------------------------------------------------------------------------------
input100K.txt           181                     1.42 * 10^6                     1.48 * 10^6

input1M.txt             15                      8.2 * 10^5                      9.1 * 10^5



/******************************************************************************
 *  Known bugs / limitations.
 *****************************************************************************/

My implementation is dependent on some amount of tuning to ensure accuracy of
neighbor calculations. The ideal SEARCH_CUTOFF constant is inversely proportional
to input size (except in the case of the 'circle' files, which require high/infinite
cutoffs for accuracy).

Recommended constants: 1-10k: ~40
Recommended constants: >10k: ~15
Recommended constants: >100k: ~5



/******************************************************************************
 *  Describe whatever help (if any) that you received.
 *  Don't include readings, lectures, and exercises, but do
 *  include any help from people (including classmates and friends) and
 *  attribute them by name.
 *****************************************************************************/


/******************************************************************************
 *  Describe any serious problems you encountered.
 *****************************************************************************/




/******************************************************************************
 *  List any other comments here. Feel free to provide any feedback
 *  on how much you learned from doing the assignment, and whether
 *  you enjoyed doing it.
 *****************************************************************************/
