package comparison.distance.graph.edit.treedistance;

import java.util.ArrayList;

import comparison.distance.Distance;
import comparison.distance.graph.edit.GraphEditCosts;
import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.tree.TreeDistance;
import comparison.distance.tree.TreeDistanceAssignmentSolver;
import graph.Graph.Vertex;
import graph.LGraph;

/**
 * Approximates the graph edit distance in linear time by computing an optimal assignment between the vertices
 * and then deriving an edit path from that assignment.
 * The assignment is computed using the given tree metric, the cost of the derived edit path is computed
 * w.r.t. the given edit costs. 
 * 
 * @author Nils Kriege
 *
 * @param <V>
 * @param <E>
 */
public class GraphEditDistanceAssignmentLinearTreeDistance<V,E> implements Distance<LGraph<V,E>> {
	
	GraphEditCosts<V,E> graphEditCosts;
	TreeDistanceAssignmentSolver<Vertex> solver;

	public GraphEditDistanceAssignmentLinearTreeDistance(GraphEditCosts<V,E> graphEditCosts, TreeDistance<Vertex> assignmentCosts) {
		this.graphEditCosts = graphEditCosts;
//		this.solver = new TreeDistanceAssignmentSolver<Vertex>(assignmentCosts, new TreeDistanceAssignmentSolver.AmenableElementAssignment());
		this.solver = new TreeDistanceAssignmentSolver<Vertex>(assignmentCosts);
	}

	@Override
	public double compute(LGraph<V,E> lg1, LGraph<V,E> lg2) {

		int n = lg1.getGraph().getVertexCount();
		int m = lg2.getGraph().getVertexCount();
		ArrayList<Vertex> A = new ArrayList<Vertex>(n+m);
		ArrayList<Vertex> B = new ArrayList<Vertex>(n+m);
		for (Vertex v : lg1.getGraph().vertices()) {
			A.add(v);
		}
		for (Vertex v : lg2.getGraph().vertices()) {
			B.add(v);
		}
		// add dummy vertices for insertion and deletion
		for (int i=0; i<m; i++) {
			A.add(GraphEditAssignmentCostsTreeDistance.DELETION_DUMMY);
		}
		for (int i=0; i<n; i++) {
			B.add(GraphEditAssignmentCostsTreeDistance.INSERTION_DUMMY);
		}

		int[] assignment = solver.solve(A, B);
		
		return GraphEditDistanceAssignment.editCosts(lg1, lg2, graphEditCosts, assignment);
	}
	
	
	
}
