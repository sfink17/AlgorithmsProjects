# 15 Puzzle Solver

This package includes several clients for testing and visualizing 15-solver solutions.
The algorithm utilized is a bidirectional best-first search, with board priority
determined by the Manhattan distance from each square to its target position.
Search space tree pruning is aided by a BiMax heuristic, which cuts all branches
if the difference between the actual distance and Manhattan distance from their
starting points, summed with the minimum priority of the reverse search, exceeds
the best solution found so far.
  
This project was completed in four days as part of the Princeton Algorithms
project assignment series. All clients within this package were provided
by Princeton under the GNU GPLv3 public licence. Implementation of the
API is entirely original.
