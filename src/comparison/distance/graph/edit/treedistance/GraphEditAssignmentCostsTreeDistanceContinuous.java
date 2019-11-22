package comparison.distance.graph.edit.treedistance;

import java.util.ArrayList;
import java.util.HashMap;

import algorithm.clustering.BisectingKMeans;
import algorithm.clustering.KMeans.KMeansCluster;
import datastructure.Pair;
import graph.AdjListRootedTree;
import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.Graph.Vertex;
import graph.LGraph;
import graph.TreeTools;
import graph.AdjListGraph.AdjListVertex;
import graph.properties.VertexArray;

/**
 * Defines a tree metric on the nodes of graphs with continuous labels for approximating the graph edit distance.
 * The tree is generated using Bisecting KMeans.
 * @author Nils Kriege
 *
 */
public class GraphEditAssignmentCostsTreeDistanceContinuous extends GraphEditAssignmentCostsTreeDistance<double[],double[]> {

	HashMap<Vertex, AdjListRTreeVertex> mapToNode;
	int partitions;

	/**
	 * @param partitions
	 * @param lgs
	 */
	public GraphEditAssignmentCostsTreeDistanceContinuous(int partitions, ArrayList<LGraph<double[],double[]>> lgs) {
		this.partitions = partitions;
		mapToNode = new HashMap<>();
		generateTree(lgs);
	}
	

	@Override
	public AdjListRTreeVertex mapToNode(Vertex t) {
		return mapToNode.get(t);
	}

	@Override
	protected void generateTree(ArrayList<? extends LGraph<double[], double[]>> lgs) {

		
		// Bisecting K-Means
		// insert all elements into map
		HashMap<double[],Vertex> mapToVertex = new HashMap<>();
		ArrayList<double[]> points = new ArrayList<>();
		for (LGraph<double[], double[]> lg : lgs) {
			for (Vertex v : lg.getGraph().vertices()) {
				double[] point = lg.getVertexLabel().get(v);
				mapToVertex.put(point, v);
				points.add(point);
			}
		}
		BisectingKMeans km = new BisectingKMeans(3, partitions);
		Pair<AdjListRootedTree, VertexArray<KMeansCluster>> result = km.core(points);
		this.rt = result.getFirst();
		for (AdjListRTreeVertex v : rt.findLeaves()) {
			for (double[] point : result.getSecond().get(v).getPoints()) {
				this.mapToNode.put(mapToVertex.get(point), v);
			}
		}
		
		// add deletion and insertion dummy vertices
		AdjListRTreeVertex r = rt.getRoot();
		AdjListRTreeVertex dummy = rt.createChild(r);
		AdjListRTreeVertex del = rt.createChild(dummy);
		mapToNode.put(DELETION_DUMMY, del);
		AdjListRTreeVertex ins = rt.createChild(dummy);
		mapToNode.put(INSERTION_DUMMY, ins);
		
		depth = TreeTools.computeDepth(rt);
		weight = new VertexArray<>(rt);
		for (AdjListVertex v : rt.vertices()) {
			weight.set(v, 1d);
		}
		weight.set(dummy, 1d);
		weight.set(del, 0d);
		weight.set(ins, 0d);
		
	}
	

}
