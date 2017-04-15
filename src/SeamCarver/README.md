# Seam Carver

This is an implementation of a seam carving algorithm, a powerful image editing technique. 
Treating each pixel of an image as a node with a calculable weight, the image itself can be
modeled as a directed acyclic graph; resizing is done by repeatedly deleting the shortest path
from each end of the image.

Two heuristics are included for calculation of pixel weight:

1. **Gradient**:
 
    This estimates the weight, or 'energy' of a pixel by measuring
    the difference between its neighbors in the x and y directions.
    A simple heuristic that seems to work well enough in practice.

2.  **Laplacian**:

    A pseudo-laplacian metric that weights pixels by their quality
    as a 'sink' or a 'source'. Basically, pixels that are significantly
    darker or lighter than their neighbors are weighted heavily, while
    smoother transistions are weighted lightly. This appears marginally
    better than the gradient at successfully suppressing the least notable
    pixels, but currently produces slightly more artifacts.
  
Shortest path calculation is aided by a best-of-8 initial upper bound
calculation, taking advantage of a-priori knowledge of the graph structure.
  
This project was completed in one week as part of the Princeton Algorithms
project assignment series. All clients within this package were provided
by Princeton under the GNU GPLv3 public licence. Implementation of SeamCarver
API is entirely original.
