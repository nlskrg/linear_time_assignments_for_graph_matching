package comparison.distance.tree;

import comparison.distance.Distance;
import graph.AdjListRootedTree;
import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.properties.VertexArray;

/**
 * Objects of type T are mapped to nodes of a tree and their distance is 
 * defined by the weighted length of the path connecting them in the tree.
 * 
 * @author Nils Kriege
 *
 * @param <T>
 */
public abstract class TreeDistance<T> implements Distance<T> {

	protected AdjListRootedTree rt;
	protected VertexArray<Integer> depth; //depths of the tree nodes in the tree
	protected VertexArray<Double> weight; // weight of the edge leading to parent
	
	@Override
	public double compute(T g1, T g2) {
		AdjListRTreeVertex a = mapToNode(g1);
		AdjListRTreeVertex b = mapToNode(g2);
		
		double length = 0;
		while (depth.get(a)<depth.get(b)) {
			length += weight.get(b);
			b = b.getParent();
		}
		while (depth.get(b)<depth.get(a)) {
			length += weight.get(a);
			a = a.getParent();
		}

		while (a != b) {
			length += weight.get(a);
			length += weight.get(b);
			a = a.getParent();
			b = b.getParent();
		}
		
		return length;
	}
	
	public AdjListRootedTree getRootedTree() {
		return rt;
	}
	
	public int getDepth(AdjListRTreeVertex v) {
		return depth.get(v);
	}
	
	public double getWeight(AdjListRTreeVertex v) {
		return weight.get(v);
	}
	
	public abstract AdjListRTreeVertex mapToNode(T t);



}
