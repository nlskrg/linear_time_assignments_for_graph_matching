package comparison.distance.graph.edit;

import graph.LGraph;
import graph.Graph.Edge;
import graph.Graph.Vertex;

/**
 * Defines the costs for approximating the graph edit distance by assignments;
 * the extended cost matrix incorporates the number edges only without performing
 * an assignment as {@link GraphEditAssignmentCostsExtended}. When there are no
 * edge relabeling costs, this is equivalent to {@link GraphEditAssignmentCostsExtended}
 * but more efficient
 * 
 * @author Nils Kriege
 *
 * @param <V>
 * @param <E>
 */
public class GraphEditAssignmentCostsExtendedSimple<V, E> implements GraphEditAssignmentCosts<V, E> {
	
	GraphEditCosts<V,E> gec;
	
	public GraphEditAssignmentCostsExtendedSimple(GraphEditCosts<V,E> gec) {
		this.gec = gec;
	}
	
	public GraphEditCosts<V, E> getGraphEditCosts() {
		return gec;
	}	
	
	public double vertexSubstitution(Vertex vEdit, LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget) {
		double r = gec.vertexRelabeling(vEdit, lgEdit, vTarget, lgTarget);

		// add lower bound for edge costs obtained from assignment
		int n = vEdit.getDegree();
		int m = vTarget.getDegree();
		
		if (n>m) {
			r += (n-m)*getGraphEditCosts().edgeDeletion(null, lgEdit);
		}
		if (n<m) {
			r += (m-n)*getGraphEditCosts().edgeInsertion(lgEdit, null, lgTarget);
		}
		
		return r;
	}
	
	public double vertexDeletion(Vertex v, LGraph<V, E> lg) {
		double r = gec.vertexDeletion(v, lg);
		// add costs for all edge deletions
		for (Edge e : v.edges()) {
			r += getGraphEditCosts().edgeDeletion(e, lg);
		}
		return r;
	}
	
	public double vertexInsertion(LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget) {
		double r = gec.vertexInsertion(lgEdit, vTarget, lgTarget);
		// add costs for all edge insertions
		for (Edge eTarget : vTarget.edges()) {
			r += getGraphEditCosts().edgeInsertion(lgEdit, eTarget, lgTarget);
		}
		return r;
	}
	
}
