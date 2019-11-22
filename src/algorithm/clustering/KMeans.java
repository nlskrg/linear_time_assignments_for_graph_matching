package algorithm.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class KMeans {
	
	Random rng = new Random();

	public static class KMeansCluster {
		ArrayList<double[]> points;
		double[] mean;

		public KMeansCluster() {
			points = new ArrayList<>();
		}
		
		public KMeansCluster(double[] mean) {
			this();
			this.mean = mean;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();

			for (double[] point : points) {
				sb.append(Arrays.toString(point));
				sb.append(" ");
			}
			return sb.toString();
		}
		
		public ArrayList<double[]> getPoints() {
			return points;
		}
		
		public int size() {
			return points.size();
		}
		
		public void updateMean() {
			int dim = points.get(0).length;
			mean = new double[dim];
			for (double[] p : points) {
				for (int i=0; i<dim; i++) {
					mean[i] += p[i];
				}
			}
			for (int i=0; i<dim; i++) {
				mean[i] /= points.size();
			}
		}
	}
	
	public ArrayList<KMeansCluster> run(ArrayList<double[]> elements, int k) {
		ArrayList<KMeansCluster> clusters = initialize(elements, k);
		
		while (reassign(clusters)) {
			update(clusters, elements);
		}
		
		return clusters;
		
	}
	
	/**
	 * Forgy method
	 */
	private ArrayList<KMeansCluster> initialize(ArrayList<double[]> elements, int k) {
		ArrayList<KMeansCluster> clusters = new ArrayList<>();
		ArrayList<double[]> elementsCopy = new ArrayList<>(elements);
		for (int i=0; i<k; i++) {
			int randomIndex = rng.nextInt(elementsCopy.size());
			double[] randomPoint = elementsCopy.get(randomIndex);
			clusters.add(new KMeansCluster(randomPoint));
			// swap with last element and remove
			int lastIndex = elementsCopy.size()-1;
			elementsCopy.set(randomIndex, elementsCopy.get(lastIndex));
			elementsCopy.remove(lastIndex);
		}
		clusters.get(0).points.addAll(elements);
		return clusters;
	}
	
	/**
	 * 
	 * @param clusters
	 * @return true if changed
	 */
	private boolean reassign(ArrayList<KMeansCluster> clusters) {
		ArrayList<ArrayList<double[]>> newClusters = new ArrayList<>(clusters.size());
		for (int i=0; i<clusters.size(); i++) {
			newClusters.add(new ArrayList<>());
		}

		boolean changed = false;
		for (int i=0; i<clusters.size(); i++) {
			KMeansCluster cluster = clusters.get(i);
			for (double[] e : cluster.points) {
				double minDist = Double.POSITIVE_INFINITY;
				int nearestClusterIndex = -1;
				for (int j=0; j<clusters.size(); j++) {
					KMeansCluster c = clusters.get(j);
					double dist = distance(e, c.mean);
					if (dist<minDist) {
						minDist = dist;
						nearestClusterIndex = j;
					}
				}
				newClusters.get(nearestClusterIndex).add(e);
				if (i != nearestClusterIndex) {
					changed = true;
				}
			}
		}
		
		for (int i=0; i<clusters.size(); i++) {
			clusters.get(i).points = newClusters.get(i);
		}

		return changed;
	}
	
	public static double distance(double[] e1, double[] e2) {
		assert e1.length == e2.length : "Point with different number of components";
		double dist = 0;
		for (int i=0; i<e1.length; i++) {
			double diff = e1[i]-e2[i];
			dist += diff*diff;
		}
		return dist;

	}
	
	private void update(ArrayList<KMeansCluster> clusters, ArrayList<double[]> elements) {
		for (KMeansCluster c : clusters) {
			if (c.points.isEmpty()) {
				// select random point
				int randomIndex = rng.nextInt(elements.size());
				double[] randomPoint = elements.get(randomIndex);
				// TODO copy should be unnecessary
				// no copy is done in other situations; mean is never changed!? 
				c.mean = Arrays.copyOf(randomPoint, randomPoint.length);
			} else {
				c.updateMean();
			}
		}	
	}
	
	/*
	public static void main(String[] args) {
		Random rng = new Random();
		ArrayList<double[]> points = new ArrayList<>();
		for (int i=0;i<2; i++) {
			points.add(randomPoint(2, rng, 0));
		}
		for (int i=0;i<8; i++) {
			points.add(randomPoint(2, rng, 15));
		}

		BisectingKMeans km = new BisectingKMeans(3, 3);
		Pair<AdjListRootedTree, VertexArray<KMeansCluster>> r = km.core(points);


		LGraph<KMeansCluster,String> lg = new LGraph<KMeansCluster,String>(r.getFirst(), r.getSecond(),new EdgeArray<>(r.getFirst()));
		
		GraphFrame.createGraphFrame(lg);
	}*/
	
	public static double[] randomPoint(int dim, Random rng, double offset) {
		double[] d = new double[dim];
		for (int i=0; i<dim; i++) {
			d[i] = rng.nextInt(10);
		}
		d[0] += offset;
		return d;
	}

}
