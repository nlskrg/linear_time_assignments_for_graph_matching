package graph;

import java.util.HashSet;
import java.util.LinkedList;

import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.properties.VertexArray;

public class TreeTools {

	/**
	 * Computes the lowest common ancestor in time O(max(depth(a),depth(b)-depth(LCA(a,b)))
	 * using the depth information provided.
	 */
	public static AdjListRTreeVertex getLCA(AdjListRTreeVertex a, AdjListRTreeVertex b, VertexArray<Integer> depth) {
		
		while (depth.get(a)<depth.get(b)) {
			b = b.getParent();
		}
		while (depth.get(b)<depth.get(a)) {
			a = a.getParent();
		}

		while (a != b) {
			a = a.getParent();
			b = b.getParent();
		}
			
		return b;
	}

	
	/**
	 * Computes the lowest common ancestor in time O(depth(a)).
	 */
	// TODO do not use this method; it is more efficient when having depth information!
	public static AdjListRTreeVertex getLCA(AdjListRTreeVertex a, AdjListRTreeVertex b) {
		HashSet<AdjListRTreeVertex> vertices = new HashSet<>();
		
		vertices.add(a);
		while (a != a.getParent()) {
			a = a.getParent();
			vertices.add(a);
		}
		
		while (!vertices.contains(b)) {
			b = b.getParent();
		}
		
		return b;
	}


	public static VertexArray<Integer> computeDepth(AdjListRootedTree rt) {
		VertexArray<Integer> depth = new VertexArray<>(rt);
		AdjListRTreeVertex r = rt.getRoot();
		int currentDepth = 0;
		LinkedList<AdjListRTreeVertex> level = new LinkedList<>();
		LinkedList<AdjListRTreeVertex> nextLevel = new LinkedList<>();
		level.add(r);
		while (!level.isEmpty()) {
			while (!level.isEmpty()) {
				r = level.poll();
				depth.set(r, currentDepth);
				nextLevel.addAll(r.children());
			}
			level = nextLevel;
			nextLevel = new LinkedList<>();
			currentDepth++;
		}
		
		return depth;
	}

}
