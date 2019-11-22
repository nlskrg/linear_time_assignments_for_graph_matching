package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import benchmark.dataset.LGDataset;
import graph.AdjListGraph;
import graph.ExtendibleGraph;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.attributes.AttributedGraph;
import graph.attributes.Attributes;
import graph.properties.EdgeArray;
import graph.properties.VertexArray;

public class AttributedGraphSetReader {
	
	public static final HashMap<Object,Integer> GRAPH_SET_MAP = new HashMap<>();
	{
		GRAPH_SET_MAP.put("train", 1);
		GRAPH_SET_MAP.put("valid", 2);
		GRAPH_SET_MAP.put("test", 0);
	}
	
	/**
	 * Reads an entire data set
	 * @param prefix path/name of the data set
	 * @return the AttributedGraph data set
	 * @throws IOException
	 */
	public LGDataset<AttributedGraph> read(String prefix) throws IOException {
		return read(prefix, 1, Integer.MAX_VALUE);
	}
	
	/**
	 * Reads part of a data set.
	 * read(prefix, 1, 100) reads the first 100 graphs from a file.
	 * read(prefix, 101, 200) reads the next 100 graphs and so on.
	 * 
	 * @param prefix path/name of the data set
	 * @param start first graph to be read (1 for the first one in file)
	 * @param end the last graph to be read (end should be >= start)
	 * @return the AttributedGraph data set (only graphs no. start-end)
	 * @throws IOException
	 */
	public LGDataset<AttributedGraph> read(String prefix, int start, int end) throws IOException {
		LGDataset<AttributedGraph> ds = new LGDataset<AttributedGraph>(prefix);
		// TODO: is there a way to not have to go through the whole file each time?
		// files
		File fASparse = new File(prefix+"_A.txt");
		File fNodeLabels = new File(prefix+"_node_labels.txt");
		File fNodeAttributes = new File(prefix+"_node_attributes.txt");
		File fGraphIndicator = new File(prefix+"_graph_indicator.txt");
		File fGraphLabels = new File(prefix+"_graph_labels.txt");
		File fGraphSets = new File(prefix+"_graph_sets.txt");
		File fEdgeLabels = new File(prefix+"_edge_labels.txt");
		File fEdgeAttributes = new File(prefix+"_edge_attributes.txt");
		
		// configuration
		boolean hasNodeLabels = fNodeLabels.exists();
		boolean hasNodeAttributes = fNodeAttributes.exists();
		boolean hasEdgeLabels = fEdgeLabels.exists();
		boolean hasEdgeAttributes = fEdgeAttributes.exists();
		boolean hasGraphSets = fGraphSets.exists();
				
		// open files
		BufferedReader rASparse = null;
		BufferedReader rNodeLabels = null;
		BufferedReader rNodeAttributes = null;
		BufferedReader rGraphIndicator = null;
		BufferedReader rGraphLabels = null;
		BufferedReader rGraphSets = null;
		BufferedReader rEdgeLabels = null;
		BufferedReader rEdgeAttributes = null;
		rASparse = new BufferedReader(new FileReader(fASparse));
		rGraphIndicator = new BufferedReader(new FileReader(fGraphIndicator));
		rGraphLabels = new BufferedReader(new FileReader(fGraphLabels));
		if (hasNodeLabels)	rNodeLabels = new BufferedReader(new FileReader(fNodeLabels));
		if (hasNodeAttributes)	rNodeAttributes = new BufferedReader(new FileReader(fNodeAttributes));
		if (hasEdgeLabels) rEdgeLabels = new BufferedReader(new FileReader(fEdgeLabels));
		if (hasEdgeAttributes) rEdgeAttributes = new BufferedReader(new FileReader(fEdgeAttributes));
		if (hasGraphSets) rGraphSets = new BufferedReader(new FileReader(fGraphSets));
		
		rASparse.mark(1024);
		
		// read graphs
		int graphIndex = 1;
		int globalVertexIndex = 0;
		String lGraphLabel = null;
		while ((lGraphLabel = rGraphLabels.readLine()) != null) {
			
			if (lGraphLabel.equals("")) {
				System.out.println("Warning: Stopped reading due to line break");
				break;
			}
			
			// create graph
			ExtendibleGraph graph = new AdjListGraph();
			VertexArray<Attributes> va = new VertexArray<Attributes>(graph, true);
			EdgeArray<Attributes> ea = new EdgeArray<Attributes>(graph, true);
			int startVertexIndex = globalVertexIndex;
			
			// set class label
			graph.setProperty("class", lGraphLabel);
			graph.setProperty("index", graphIndex);
			
			
			// set graph set
			if (hasGraphSets) {
				graph.setProperty("set", rGraphSets.readLine());
			}
			
			// create vertices
			String lGraphIndicator = null;
			while ((lGraphIndicator = rGraphIndicator.readLine()) != null) {
				if (Integer.valueOf(lGraphIndicator) != graphIndex) {
					rGraphIndicator.reset();
					break;
				}
				Vertex v = graph.createVertex();
				String lNodeLabels = hasNodeLabels ? rNodeLabels.readLine() : "";
				String lNodeAttributes = hasNodeAttributes ? rNodeAttributes.readLine() : "";
				Attributes attr = new Attributes(lNodeLabels, lNodeAttributes);
				va.set(v, attr);
				globalVertexIndex++;
				rGraphIndicator.mark(1024);
			}

			// create edges
			String lASparse = null;
			while ((lASparse = rASparse.readLine()) != null) {
				String[] tokens = lASparse.split(",");
				int iU = Integer.valueOf(tokens[0].trim());
				int iV = Integer.valueOf(tokens[1].trim());
//				int iU = Double.valueOf(tokens[0].trim()).intValue();
//				int iV = Double.valueOf(tokens[1].trim()).intValue();
				if (iU > globalVertexIndex || iV > globalVertexIndex) {
					rASparse.reset();
					break;
				}
				Vertex u = graph.getVertex(iU-startVertexIndex-1);
				Vertex v = graph.getVertex(iV-startVertexIndex-1);
				if (!graph.hasEdge(u, v)) {
					Edge e = graph.createEdge(u, v);
					String lEdgeLabels = hasEdgeLabels ? rEdgeLabels.readLine() : "";
					String lEdgeAttributes = hasEdgeAttributes ? rEdgeAttributes.readLine() : "";
					Attributes attr = new Attributes(lEdgeLabels, lEdgeAttributes);
					ea.set(e, attr);
				} else {
					// skip lines
					if (hasEdgeLabels) rEdgeLabels.readLine();
					if (hasEdgeAttributes) rEdgeAttributes.readLine();
				}
				rASparse.mark(1024);
			}
			if(graphIndex>= start)
			{
				ds.add(new AttributedGraph(graph, va, ea));
			}
			if(graphIndex>= end)
			{
				break;
			}
			graphIndex++;
		}
		
		// close files
		rASparse.close();
		rGraphIndicator.close();
		rGraphLabels.close();
		if (hasNodeLabels) rNodeLabels.close();
		if (hasNodeAttributes) rNodeAttributes.close();
		if (hasEdgeLabels) rEdgeLabels.close();
		if (hasEdgeAttributes) rEdgeAttributes.close();
		
		return ds;		
	}
	
	public static void main(String[] args) throws IOException {
//		AttributedGraphSetReader agr = new AttributedGraphSetReader();
//		LGDataset<AttributedGraph> ds = agr.reader("BZR");
//		ds.printStatistics();
//		System.out.println("==========================");
//		Datasets.getSimpBZR().printStatistics();
		
		String[] datasets = {
			"datasets-MN/MUTAG",
			"datasets-MN/DD"			
		};
		
		AttributedGraphSetReader agr = new AttributedGraphSetReader();
		for (String prefix : datasets) {
			LGDataset<AttributedGraph> ds = agr.read(prefix);
			ds.printStatistics();
			System.out.println();
		}
	}
	
	
	public static void readAndApplyGraphSet(LGDataset<AttributedGraph> ds, String fileName) throws IOException {
		File fGraphSets = new File(fileName);
		BufferedReader rGraphSets = new BufferedReader(new FileReader(fGraphSets));

		// read graphs
		for (int i=0; i<ds.size(); i++) {
			// TODO there is no fixed setting for train/test
			ds.get(i).getGraph().setProperty("set", rGraphSets.readLine().equals("0") ? "train" : "test" );
		}

		rGraphSets.close();
	}
	
	
}
