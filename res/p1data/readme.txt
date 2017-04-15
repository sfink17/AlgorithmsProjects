/******************************************************************************
 *  Name: Simon Fink
 *
 *  Operating system: Windows 10/Linux Mint
 *  Compiler: Javac
 *  Text editor / IDE: IntelliJ Idea
 *  Hours to complete assignment (optional):
 ******************************************************************************/


/******************************************************************************
 *  Describe how you implemented Percolation.java. How did you check
 *  whether the system percolates?
 *****************************************************************************/
My Percolation class relies on an N*N size status array of all nodes. Each node's
status is stored as an integer representation of one of three binary codes:

0 0 1 = Open
0 1 0 = Connected to top
1 0 0 = Connected to bottom

Whenever the open() method is called, the node checks each surrounding open node,
finding the root node of their parents, 'or'-ing their status with its own, and then
calling union() with itself and the neighbor in question. After this, the final root
node of the resultant component is 'or'-ed with the original node, now holding the
combined status of all components. This allows the program to perform its operations
in constant time, and avoids the backwash characteristic of the standard method.


/******************************************************************************
 *  Using Percolation with QuickFindUF.java,  fill in the table below such that
 *  the N values are multiples of each other.

 *  Give a formula (using tilde notation) for the running time (in seconds) of
 *  PercolationStats.java as a function of both N and T. Be sure to give both
 *  the coefficient and exponent of the leading term. Your coefficients should
 *  be based on empirical data and rounded to two significant digits, such as
 *  5.3*10^-8 * N^5.0 T^1.5.
 *****************************************************************************/


There seems to be a tremendous amount of variance at low N's, presumably
because of non-negligible, widely varying overhead that dominates over the cost
of operations. That said, larger N's take way too long for my laptop to run.
Getting good data for this problem is therefore pretty difficult.

Also, the question asks for how the algorithm scales with N, despite the fact that
the number of UF nodes scales with N^2. I coded a growth modeler into my stats
class that remedies this, and also does the calculations for me, so I'll just
post what it says. Keep in mind, for both QF and WQU, the "N" I'm using is
total number of nodes, not length of the grid.

running time as a function of N and T:  ~ 1.6*10^-9 * N^2 * T

/******************************************************************************
 *  Repeat the previous question, but use WeightedQuickUnionUF.java.
 *****************************************************************************/

This one was weird. I'm getting a lot of consistency for ratios for specific values
of N, but not a lot of convergence. Past a certain point, it SEEMS to be moving closer
and closer to linear complexity, but I can't be sure. Also, my coefficients are all
over the place.

running time as a function of N and T:  ~1.5*10^-7 * N^1.15 * T

/******************************************************************************
 * BONUS: UF and AmortizedUF
 *****************************************************************************/
Not sure I understand this entirely, but here's the data I gathered for
path compression.
Their method had half-path compression, with the find method
reassigning the parent of each traversed node to its grandparent.
Mine was the most conventional means of path compression, using a recursive
find method that attaches each child to the root node as it traverses.

Amortized (mine with path compression):
running time as a function of N and T:  ~2*10^-7 * N^1.08 * T

UF (theirs with path compression):
running time as a function of N and T:  ~10^-7 * N^1.12 * T

UF runs extremely slightly better than WQUUF in all cases. My analysis
implies that the traditional amortized (mine) will run better than both...
eventually. My coefficients are not to be trusted, and individual tests
still have UF outperforming mine beyond N = 5.5*10^7.

Moral of the story: Complexity is the end goal in theory, but doesn't
show the whole picture in practice. Sometimes a beautiful proof of
a best-case complexity algorithm will turn out to be useless experimentally,
yielding minimal and even worse average benefit.

/**********************************************************************
 *  How much memory (in bytes) does a Percolation object use to store
 *  an N-by-N grid? Use the 64-bit memory cost model from Section 1.4
 *  of the textbook and use tilde notation to simplify your answer.
 *  Briefly justify your answers.
 *
 *  Include the memory for all referenced objects (deep memory).
 **********************************************************************/

I'm using three 1D arrays:  one array of ints for the bitwise status grid,
and the two encapsulated by the UF object for parent/rank checks. These
take up 3*24 bytes in overhead (16 for the object + 4 for size int + 4 padding),
+ 3*4*N^2, for a total of 3*(24 + 4*N^2) = ~12N^2 bytes.


/******************************************************************************
 *  Known bugs / limitations.
 *****************************************************************************/
None I think.

/******************************************************************************
 *  Describe whatever help (if any) that you received.
 *  Don't include readings, lectures, and exercises, but do
 *  include any help from people (including classmates and friends) and
 *  attribute them by name.
 *****************************************************************************/
I did get the idea for the status grid from this dude's blog:
http://www.sigmainfy.com/blog/avoid-backwash-in-percolation.html

I tried to get as little outside help as possible, but that couldn't be helped.

/******************************************************************************
 *  Describe any serious problems you encountered.
 *****************************************************************************/

Backwash backwash backwash.

/******************************************************************************
 *  List any other comments here. Feel free to provide any feedback
 *  on how much you learned from doing the assignment, and whether
 *  you enjoyed doing it.
 *****************************************************************************/
I really like this class. This is the basis for the kind of thing I want to
pursue in grad school. Looking forward to actually coding algorithms rather than
just clients.