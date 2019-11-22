package comparison.distance.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.Graph.Vertex;
import graph.properties.VertexArray;


/**
 * Solves the assignment problem under a tree metric in linear time.
 * 
 * @author Nils Kriege
 *
 * @param <T>
 */
public class TreeDistanceAssignmentSolver<T> {
	
	TreeDistance<T> dist;
	TiebreakStrategy<T> ea;
	VertexArray<ArrayList<Integer>> AL;
	VertexArray<ArrayList<Integer>> BL;
	VertexArray<Integer> degree; // the number of children with respect to the relevant subtree R
	VertexArray<Boolean> marked; // marks the vertices in R
	
	public TreeDistanceAssignmentSolver(TreeDistance<T> dist) {
		this(dist, new TiebreakStrategy.DefaultTiebreakStrategy<>());
	}
	
	public TreeDistanceAssignmentSolver(TreeDistance<T> dist, TiebreakStrategy<T> ea) {
		this.dist = dist;
		this.ea = ea;
		this.AL = new VertexArray<>(dist.getRootedTree());
		this.BL = new VertexArray<>(dist.getRootedTree());
		this.degree = new VertexArray<>(dist.getRootedTree());
		this.marked = new VertexArray<>(dist.getRootedTree());
		for (Vertex v : dist.getRootedTree().vertices()) {
			AL.set(v, new ArrayList<>());
			BL.set(v, new ArrayList<>());
			degree.set(v, 0);
			marked.set(v, false);
		}
	}
	
	/**
	 * Lists A and B must have the same cardinality 
	 * @param A
	 * @param B
	 * @return Minimum assignment costs between the to sets
	 */
	public double minimumCost(ArrayList<T> A, ArrayList<T> B) {
		int [] assignment = solve(A, B);
		double c = 0;
		for (int i=0; i<assignment.length; i++) {
			c += dist.compute(A.get(i), B.get(assignment[i]));
		}
		return c;		
	}
	
	
	/**
	 * Solves the assignment problem under the given tree metric for sets A and B.
	 * @param A
	 * @param B
	 * @return Assignment (with minimum costs) of elements of A to the elements of B
	 */
	public int[] solve(ArrayList<T> A, ArrayList<T> B) {

		int minDepth = Integer.MAX_VALUE;
		ArrayList<AdjListRTreeVertex> N = new ArrayList<>(); 
		
		// initialize
		for (int i=0; i<A.size(); i++) {
			AdjListRTreeVertex v = dist.mapToNode(A.get(i));
			if (!marked.get(v)) {
				N.add(v); // add v to R if not already present
				marked.set(v, true);
				minDepth = Math.min(minDepth, dist.getDepth(v));
			}
			AL.get(v).add(i);
		}
		
		for (int i=0; i<B.size(); i++) {
			AdjListRTreeVertex v = dist.mapToNode(B.get(i));
			if (!marked.get(v)) {
				N.add(v); // add v to R if not already present
				marked.set(v, true);
				minDepth = Math.min(minDepth, dist.getDepth(v));
			}
			BL.get(v).add(i);
		}
		
		// mark relevant subtree, compute degree
		LinkedList<AdjListRTreeVertex> roots = new LinkedList<>();		
		for (AdjListRTreeVertex v : N) {
			while (dist.getDepth(v) > minDepth) {
				v = v.getParent();
				degree.set(v, degree.get(v)+1);
				if (marked.get(v)) break;
				marked.set(v, true);
			}
			// root has degree 1 when it is found for the first time;
			// and degree 0 if it is in N
			if (dist.getDepth(v) == minDepth && degree.get(v)<=1) { 
				roots.add(v);
			}
		}
		
		while (roots.size() != 1) {
			LinkedList<AdjListRTreeVertex> parents = new LinkedList<>();
			for (AdjListRTreeVertex r : roots) {
				AdjListRTreeVertex p = r.getParent();
				if (!marked.get(p)) {
					parents.add(p);
					marked.set(p, true);
				}
				degree.set(p, degree.get(p)+1);
			}
			roots = parents;
			minDepth--;
		}
		AdjListRTreeVertex root = roots.getFirst(); // the only remaining element
		
		// find leaves
		LinkedList<AdjListRTreeVertex> leaves = new LinkedList<>();
		for (AdjListRTreeVertex v : N) {
			if (degree.get(v)==0) leaves.add(v);
		}
		leaves = ea.leafOrder(A,B,AL,BL,leaves);
				
	    // compute matching
		int[] assignment = new int[A.size()];
		Arrays.fill(assignment, -1);
		
		while (!leaves.isEmpty()) {
			AdjListRTreeVertex l = leaves.poll();
			
			ArrayList<Integer> eA = AL.get(l);
			ArrayList<Integer> eB = BL.get(l);
			// pair elements
			ea.pairElements(A, B, eA, eB, l, assignment);

			// propagate unmatched elements to neighbor
			if (l != root) {
				AdjListRTreeVertex p = l.getParent();
				AL.get(p).addAll(eA);
				BL.get(p).addAll(eB);
				eA.clear();
				eB.clear();
				
				int newDegree = degree.get(p)-1;
				degree.set(p, newDegree);
				if (newDegree == 0) leaves.add(p);
			}
			marked.set(l, false);
		}

		return assignment;
	}
		
}
