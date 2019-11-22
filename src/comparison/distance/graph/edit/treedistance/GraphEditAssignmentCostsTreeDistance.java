package comparison.distance.graph.edit.treedistance;

import java.util.ArrayList;

import comparison.distance.graph.edit.GraphEditAssignmentCosts;
import comparison.distance.tree.TreeDistance;
import graph.AdjListGraph;
import graph.AdjListRootedTree;
import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.Graph.Vertex;
import graph.LGraph;

/**
 * Defines the costs for approximating the graph edit distance by assignments using a tree distance.
 * 
 * @author Nils Kriege
 *
 * @param <V>
 * @param <E>
 */
public abstract class GraphEditAssignmentCostsTreeDistance<V,E> extends TreeDistance<Vertex> implements GraphEditAssignmentCosts<V, E> {

	public static Vertex DELETION_DUMMY = new AdjListGraph().createVertex();
	public static Vertex INSERTION_DUMMY = new AdjListGraph().createVertex();
	

	protected GraphEditAssignmentCostsTreeDistance() {
		
	}
	
	public GraphEditAssignmentCostsTreeDistance(ArrayList<LGraph<V,E>> lgs) {
		this.rt = new AdjListRootedTree();
		generateTree(lgs);
	}
	
	@Override
	public double vertexSubstitution(Vertex vEdit, LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget) {
		return compute(vEdit, vTarget);
	}

	@Override
	public double vertexDeletion(Vertex v, LGraph<V, E> lg) {
		return compute(v, DELETION_DUMMY);
	}

	@Override
	public double vertexInsertion(LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget) {
		return compute(INSERTION_DUMMY, vTarget);
	}

	@Override
	public abstract AdjListRTreeVertex mapToNode(Vertex t);
	
	protected abstract void generateTree(ArrayList<? extends LGraph<V,E>> lgs);

}
