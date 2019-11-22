package benchmark.dataset;

import java.util.Collection;
import java.util.HashMap;

import graph.Graph;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.LGraph;
import graph.properties.EdgeArray;
import graph.properties.VertexArray;

public class Statistics {
	
	
	public static void printClassStatistics(String[] classes) {
		
		HashMap<String,Integer> classLabels = new HashMap<String,Integer>();

		for (String c : classes) {
			Integer counter = classLabels.get(c);
			classLabels.put(c, (counter == null ? 1 : counter + 1));
		}
		System.out.println("Class labels:      "+classLabels.size()+"  "+classLabels);
	}
	
	public static <IV,IE> void printLabeledGraphStatistics(Collection<? extends LGraph<?,?>> graphs) {
		
		printClassStatistics(LGDataset.getClassesArray(graphs));
		printGraphStatistics(LGraph.toGraphCollection(graphs));
		
		HashMap<Object,Integer> vertexLabel = new HashMap<Object,Integer>();
		HashMap<Object,Integer> edgeLabel = new HashMap<Object,Integer>();
		for (LGraph<?,?> lg : graphs) {
			Graph g = lg.getGraph();
			VertexArray<?> va = lg.getVertexLabel();
			for (Vertex v : g.vertices()) {
				Integer counter = vertexLabel.get(va.get(v));
				vertexLabel.put(va.get(v), (counter == null ? 1 : counter + 1));
			}
			EdgeArray<?> ea = lg.getEdgeLabel();
			for (Edge e : g.edges()) {
				Integer counter = edgeLabel.get(ea.get(e));
				edgeLabel.put(ea.get(e), (counter == null ? 1 : counter + 1));
			}
		}
		
		System.out.println("Vertex labels:     "+vertexLabel.size()+"  "+vertexLabel);
		System.out.println("Edge labels:       "+edgeLabel.size()+"  "+edgeLabel);
	}
	
	public static <IV,IE> void printLabeledGraphStatistics(AttrDataset graphs) {

		printLabeledGraphStatistics(graphs.getSDataset());
		
		// get attribute counts, first graph, first vertex/edge
		int vAttrCount = graphs.get(0).getVertexLabel().get(graphs.get(0).getGraph().getVertex(0)).getRealValuedAttributeCount();
		int eAttrCount = graphs.get(0).getEdgeLabel().get(graphs.get(0).getGraph().getEdge(0)).getRealValuedAttributeCount();

		System.out.println("Vertex attributes: "+vAttrCount);
		System.out.println("Edge attributes:   "+eAttrCount);
	}

	
	public static void printGraphStatistics(Collection<? extends Graph> graphs) {
		int n = graphs.size();
		long totalV = 0;
		long totalE = 0;
		long totalDegree = 0;
		int maxV = Integer.MIN_VALUE;
		int maxE = Integer.MIN_VALUE;
		int maxDegree = Integer.MIN_VALUE;
		HashMap<Integer,Integer> degreeDistribution = new HashMap<>();
		for (Graph g : graphs) {
			totalV += g.getVertexCount();
			totalE += g.getEdgeCount();
			maxV = Math.max(maxV, g.getVertexCount());
			maxE = Math.max(maxE, g.getEdgeCount());
			for (Vertex v : g.vertices()) {
				totalDegree += v.getDegree();
				maxDegree = Math.max(maxDegree, v.getDegree());
				Integer counter = degreeDistribution.get(v.getDegree());
				degreeDistribution.put(v.getDegree(), (counter == null ? 1 : counter + 1));
			}
		}

		System.out.println("Number of graphs:  "+n);
		System.out.println("Total |V|:         "+totalV);
		System.out.println("Total |E|:         "+totalE);
		System.out.println("Max. |V|:          "+maxV);
		System.out.println("Avg. |V|:          "+(double)totalV/n);
		System.out.println("Max. |E|:          "+maxE);
		System.out.println("Avg. |E|:          "+(double)totalE/n);
		System.out.println("Max. deg.:         "+maxDegree);
		System.out.println("Avg. deg.:         "+(double)totalDegree/totalV);
		System.out.println("Deg. dist.:        "+degreeDistribution);

	}


	public static void printGraphStatistics(Graph g) {
		long totalDegree = 0;
		int maxDegree = Integer.MIN_VALUE;
		for (Vertex v : g.vertices()) {
			totalDegree += v.getDegree();
			maxDegree = Math.max(maxDegree, v.getDegree());
		}
		int n = g.getVertexCount();
		int m = g.getEdgeCount();
		
		System.out.println("|V|: "+n);
		System.out.println("|E|: "+m);
		System.out.println("Density: "+((double)m)/(n*(n-1)));
		System.out.println("Max. deg.: "+maxDegree);
		System.out.println("Avg. deg.: "+(double)totalDegree/g.getVertexCount());
	}
	
	public static void printLabeledGraphStatistics(LGraph<?,?> lg) {
		Graph g = lg.getGraph();
		
		printGraphStatistics(g);
		
		HashMap<Object,Integer> vertexLabel = new HashMap<Object,Integer>();
		HashMap<Object,Integer> edgeLabel = new HashMap<Object,Integer>();
		VertexArray<?> va = lg.getVertexLabel();
		for (Vertex v : g.vertices()) {
			Integer counter = vertexLabel.get(va.get(v));
			vertexLabel.put(va.get(v), (counter == null ? 1 : counter + 1));
		}
		EdgeArray<?> ea = lg.getEdgeLabel();
		for (Edge e : g.edges()) {
			Integer counter = edgeLabel.get(ea.get(e));
			edgeLabel.put(ea.get(e), (counter == null ? 1 : counter + 1));
		}
		
		System.out.println("Vertex labels:       "+vertexLabel.size()+"  "+vertexLabel);
		System.out.println("Edge labels:         "+edgeLabel.size()+"  "+edgeLabel);
	}
	
}
