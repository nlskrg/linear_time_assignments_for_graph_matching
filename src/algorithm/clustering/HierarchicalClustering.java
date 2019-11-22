package algorithm.clustering;

import java.util.ArrayList;
import java.util.HashMap;

import datastructure.Pair;
import graph.AdjListRootedTree;
import graph.AdjListRootedTree.AdjListRTreeVertex;

public interface HierarchicalClustering<T> {
	public Pair<AdjListRootedTree, HashMap<T, AdjListRTreeVertex>> run(ArrayList<T> points);	
}
