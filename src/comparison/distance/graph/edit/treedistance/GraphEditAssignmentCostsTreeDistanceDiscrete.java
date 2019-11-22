package comparison.distance.graph.edit.treedistance;

import java.util.ArrayList;
import java.util.HashMap;

import algorithm.graph.isomorphism.labelrefinement.VertexLabelConverter;
import algorithm.graph.isomorphism.labelrefinement.WeisfeilerLehmanRefiner;
import concepts.TransformationTools;
import graph.AdjListRootedTree;
import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.Graph;
import graph.Graph.Vertex;
import graph.LGraph;
import graph.TreeTools;
import graph.AdjListGraph.AdjListVertex;
import graph.properties.VertexArray;

/**
 * Defines a tree metric on the nodes of graphs with discrete labels for approximating the graph edit distance.
 * The tree is generated using Weisfeiler-Lehman vertex coloring.
 * 
 * @author Nils Kriege
 *
 * @param <V>
 * @param <E>
 */
public class GraphEditAssignmentCostsTreeDistanceDiscrete<V,E> extends GraphEditAssignmentCostsTreeDistance<V,E> {

	HashMap<Vertex, AdjListRTreeVertex> mapToNode;
	int iterations;

	/**
	 * @param iterations iterations of color refinement
	 */
	public GraphEditAssignmentCostsTreeDistanceDiscrete(int iterations) {
		this.iterations = iterations;
	}

	/**
	 * @param iterations iterations of color refinement
	 * @param lgs dataset the tree will be generated for
	 */
	public GraphEditAssignmentCostsTreeDistanceDiscrete(int iterations, ArrayList<? extends LGraph<V,E>> lgs) {
		this.iterations = iterations;
		adjustToDataset(lgs);
	}
	
	/**
	 * @param lgs dataset the tree will be generated for
	 */
	public void adjustToDataset(ArrayList<? extends LGraph<V,E>> lgs) {
		mapToNode = new HashMap<>();
		this.rt = new AdjListRootedTree();
		generateTree(lgs);
	}
	

	@Override
	public AdjListRTreeVertex mapToNode(Vertex t) {
		return mapToNode.get(t);
	}

	@Override
	public void generateTree(ArrayList<? extends LGraph<V,E>> graphs) {
		
		// create the tree
		rt.createRoot();
		// assign all vertices to the root
		for (LGraph<V, E> lg : graphs) {
			for (Vertex v : lg.getGraph().vertices()) {
				mapToNode.put(v, rt.getRoot());
			}
		}
		
		// maps color i to the vertex in the tree
		ArrayList<AdjListRTreeVertex> colorToTreeVertex = new ArrayList<>(); 
		
		// create the Weisfeiler-Lehman hierarchy
		VertexLabelConverter<E> vlc = new VertexLabelConverter<E>();

		// assign integer label
		// using the same VertexLabelConverter assures that new labels (from refinement) will 
		// be assigned higher integer values, such that labels of different iterations can be
		// distinguished
		ArrayList<LGraph<Integer, E>> lgs = TransformationTools.transformAll(vlc, graphs);
		vlc.clearLabelMap(); 
		// adjust array list size to store tree vertices
		while (colorToTreeVertex.size() < vlc.getNextLabel()) colorToTreeVertex.add(null); 
		
		// iteration 0, level at depth 1
		createNextTreeLevel(lgs, colorToTreeVertex);
		
		WeisfeilerLehmanRefiner<E> wlr = new WeisfeilerLehmanRefiner<E>();
		for (int i=1; i<=iterations; i++) {
			// iteration i creates level at depth i+1
			
			// refinement
			ArrayList<LGraph<String, E>> refLgs = TransformationTools.transformAll(wlr, lgs);
			// compression
			lgs = TransformationTools.transformAll(vlc, refLgs);
			vlc.clearLabelMap();
			while (colorToTreeVertex.size() < vlc.getNextLabel()) colorToTreeVertex.add(null); 
			
			// create next tree level
			createNextTreeLevel(lgs, colorToTreeVertex);
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
	
	/**
	 * @param lgs dataset the tree is generated for
	 * @param colorToTreeVertex map of colors to the vertices in the tree
	 */
	private void createNextTreeLevel(ArrayList<LGraph<Integer, E>> lgs, ArrayList<AdjListRTreeVertex> colorToTreeVertex) {
		for (LGraph<Integer, E> lg : lgs) {
			Graph g = lg.getGraph();
			VertexArray<Integer> va = lg.getVertexLabel();
			for (Vertex v : g.vertices()) {
				Integer color = va.get(v);
				AdjListRTreeVertex tv = colorToTreeVertex.get(color);
				if (tv == null) {
					AdjListRTreeVertex p = mapToNode.get(v);
					tv = rt.createChild(p);
					colorToTreeVertex.set(color, tv);
				}
				mapToNode.put(v, tv);
			}
		}
	}


}
