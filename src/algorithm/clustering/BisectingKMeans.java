package algorithm.clustering;

import java.util.ArrayList;
import java.util.HashMap;

import algorithm.clustering.KMeans.KMeansCluster;
import comparison.distance.SquaredEuclideanDistance;
import datastructure.Pair;
import graph.AdjListRootedTree;
import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.properties.VertexArray;

public class BisectingKMeans implements HierarchicalClustering<double[]>{
	
	KMeans kmeans;
	int iter;
	private int k;
	/**
	* @param k the number of clusters, i.e., leafs in the hierarchy
	* */
	public BisectingKMeans(int iter, int k) {
		this.kmeans = new KMeans();
		this.iter = iter;
		this.k = k;
	}
	
	/**
	 * @param points the data points
	 * @return the hierarchical clustering
	 */
	public Pair<AdjListRootedTree, VertexArray<KMeansCluster>> core(ArrayList<double[]> points) {
		AdjListRootedTree rt = new AdjListRootedTree(); 
		VertexArray<KMeansCluster> va = new VertexArray<>(rt, 2*k-1, true);
		
		// create root cluster
		AdjListRTreeVertex root = rt.createRoot();
		KMeansCluster rootCluster = new KMeansCluster();
		rootCluster.points = points;
		rootCluster.updateMean();
		va.set(root, rootCluster);
		
		ArrayList<AdjListRTreeVertex> clusterVertices = new ArrayList<>(k);
		clusterVertices.add(root);
		
		while (clusterVertices.size()<k) {
			
			// (1) pick a cluster to split: largest one
			int largestClusterIndex = 0;
			KMeansCluster largestCluster = va.get(clusterVertices.get(largestClusterIndex));
			for (int i=1; i<clusterVertices.size(); i++) {
				KMeansCluster cluster = va.get(clusterVertices.get(i));
				if (cluster.size()>largestCluster.size()) {
					largestCluster = cluster;
					largestClusterIndex = i;
				}
			}
			
			// (2) bisection
			ArrayList<KMeansCluster> bestResult = null;
			double bestOverallDissimilarity = Double.POSITIVE_INFINITY;
			for(int i=0; i<iter; i++) {
				ArrayList<KMeansCluster> result = kmeans.run(largestCluster.points, 2);
				double overallDissimilarity = getOverallDissimilarity(result);
				// TODO use different criteria, e.g., getNegativeSquaredEuclideanDistanceBetweenMeans
				if (overallDissimilarity<bestOverallDissimilarity) {
					bestOverallDissimilarity = overallDissimilarity;
					bestResult = result;
				}
			}
			
			// (3) insert bisection
			AdjListRTreeVertex parent = clusterVertices.get(largestClusterIndex);
			AdjListRTreeVertex c1 = rt.createChild(parent);
			AdjListRTreeVertex c2 = rt.createChild(parent);
			va.set(c1, bestResult.get(0));
			va.set(c2, bestResult.get(1));
			clusterVertices.set(largestClusterIndex,c1);
			clusterVertices.add(c2);
		}
		
		return new Pair<AdjListRootedTree, VertexArray<KMeansCluster>>(rt, va);
	}
	
	/**
	 * Measures the quality of a clustering.
	 * @param clusters the clusters
	 * @return the sum of squared distances between the points and the mean of their cluster
	 */
	public static double getOverallDissimilarity(ArrayList<KMeansCluster> clusters) {
		double ssd = 0;
		for(KMeansCluster cluster : clusters) {
			for(double[] p : cluster.points) {
				double dist = KMeans.distance(p, cluster.mean);
				ssd += dist*dist;
			}
		}
		return ssd; 
	}
	
	public static double getNegativeSquaredEuclideanDistanceBetweenMeans(ArrayList<KMeansCluster> clusters) {
		return new SquaredEuclideanDistance().compute(clusters.get(0).mean, clusters.get(1).mean)*-1;
		
	}

	@Override
	public Pair<AdjListRootedTree, HashMap<double[], AdjListRTreeVertex>> run(ArrayList<double[]> points) {
		Pair<AdjListRootedTree, VertexArray<KMeansCluster>> result =  core(points);
		HashMap<double[], AdjListRTreeVertex> map = fillMap(result, points);
		return new Pair<AdjListRootedTree, HashMap<double[], AdjListRTreeVertex>>(result.getFirst(), map);
	}

	/**
	 * fills the map point -> AdjListRTreeVertex
	 * @param result the result of the kmeans clustering
	 * @param points the points that were clustered
	 */
	private HashMap<double[], AdjListRTreeVertex> fillMap(Pair<AdjListRootedTree, VertexArray<KMeansCluster>> result,
			ArrayList<double[]> points) {
		HashMap<double[], AdjListRTreeVertex> map = new HashMap<double[], AdjListRTreeVertex>();
		AdjListRootedTree rt = result.getFirst();
		for (AdjListRTreeVertex v : rt.findLeaves()) {
			for (double[] point : result.getSecond().get(v).getPoints()) {
				map.put(point, v);
			}
		}
		return map;
	}

}
