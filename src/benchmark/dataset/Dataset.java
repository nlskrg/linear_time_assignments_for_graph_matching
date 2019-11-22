package benchmark.dataset;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import datastructure.FeatureVector;
import datastructure.SparseFeatureVector;

public abstract class Dataset<T> extends ArrayList<T> {
	
	private static final long serialVersionUID = 1L;
	String id;
	
	/**
	 * @param id a string to identify the dataset
	 */
	public Dataset(String id) {
		this.id = id;
	}

	public String getID() {
		return id;
	}
	
	/**
	 * Creates a subset of this dataset by sampling random elements.
	 * 
	 * @param size size of the subset
	 * @param balanced tries to balance the subset in terms of class labels
	 * @return a subset of this dataset
	 */
	public Dataset<T> createRandomSubset(int size, boolean balanced) {
		return createRandomSubset(size, balanced, newEmptyInstance(id));
	}
	
	protected <X extends Dataset<T>> X createRandomSubset(int size, boolean balanced, X r) {
		if (this.size() < size) throw new IllegalArgumentException("Subset size exceeds dataset size.");
		Random rng = new Random(4242424242l);
		if (balanced) {
			HashMap<String, ArrayList<T>> classPartition = getElementsByClass();
			while (r.size() < size) {
				for (String s : classPartition.keySet()) {
					ArrayList<T> set = classPartition.get(s);
					if (set.isEmpty()) continue;
					r.add(set.remove(rng.nextInt(set.size())));
					if (r.size() == size) break;
				}
			}
		} else {
			r.addAll(this);
			while (r.size()>size) {
				r.remove(rng.nextInt(r.size()));			
			}
		}
		return r;
	}
	
	/**
	 * Creates a subset of this dataset consisting of the first k elements only.
	 * 
	 * @param k
	 * @return a subset of this dataset
	 */
	public Dataset<T> createFirstKSubset(int k) {
		return createFirstKSubset(k, newEmptyInstance(id));
	}
	
	protected <X extends Dataset<T>> X createFirstKSubset(int k, X r) {
		for (int i=0; i<k; i++) {
			r.add(this.get(i));
		}
		return r;
	}
	
	public abstract String getClassLabel(T t);
	
	public abstract Dataset<T> newEmptyInstance(String id);
	
	/**
	 * Returns an array of class labels, where the i-th label corresponds to the i-th 
	 * graph in the dataset.
	 * 
	 * @return array of class labels
	 */
	public String[] getClassLabels() {
		String[] r = new String[this.size()];
		int i = 0;
		for (T t : this) {
			r[i++] = getClassLabel(t);
		}
		return r;
	}
	
	/**
	 * Returns the frequency distribution of class labels.
	 */
	public FeatureVector<String> getClassCounts() {
		SparseFeatureVector<String> r = new SparseFeatureVector<String>();
		for (String s : getClassLabels()) {
			r.increaseByOne(s);
		}
		return r;
	}
	
	/**
	 * Returns a HashMap containing an ArrayList of graphs for each
	 * class label.
	 */
	protected HashMap<String, ArrayList<T>> getElementsByClass() {
		HashMap<String, ArrayList<T>> byClass = new HashMap<String, ArrayList<T>>();
		for (T lg : this) {
			String s = getClassLabel(lg);
			ArrayList<T> lgs = byClass.get(s);
			if (lgs == null) {
				lgs = new ArrayList<T>();
				byClass.put(s, lgs);
			}
			lgs.add(lg);
		}
		return byClass;
	}

	



}
