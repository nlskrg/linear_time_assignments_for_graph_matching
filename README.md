# Optimal Assignments in Linear Time for Graph Matching
Source code for the paper [Computing Optimal Assignments in Linear Time for Approximate Graph Matching](https://arxiv.org/abs/1901.10356), Nils M. Kriege, Pierre-Louis Giscard, Franka Bause, Richard C. Wilson, ICDM 2019.

## Usage
The algorithms contained in this package for computing the graph edit distance can be executed via a command line interface. Run the shell script `linGED` to see a list of all available kernels and parameters.

### Example
The following command uses the linear time method to approximate the graph edit distance between all pairs of graphs in the dataset Letter-low:
```
./linGED -d Letter-low lin
```
The pairwise distances are written to the file `Letter-low_lin_ged.txt`.

## Building from source
Run `ant` to build `ged.jar` from source. 

## Data sets
The repository contains the datasets Letter-low and AIDS only. Further data sets are available from the website [Benchmark Data Sets for Graph Kernels](http://graphkernels.cs.tu-dortmund.de). Please note that our linear time implementation does not support graphs having both discrete and continous labels.

## Terms and conditions
When using our code please cite our ICDM 2019 paper.

## Contact information
If you have any questions, please contact [Nils Kriege](https://ls11-www.cs.tu-dortmund.de/staff/kriege).
