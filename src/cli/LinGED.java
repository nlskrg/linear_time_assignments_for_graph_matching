package cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import algorithm.assignment.AssignmentSolver;
import benchmark.dataset.AttrDataset;
import comparison.distance.AttributesDistance;
import comparison.distance.Distance;
import comparison.distance.graph.edit.GraphEditAssignmentCosts;
import comparison.distance.graph.edit.GraphEditAssignmentCostsExtended;
import comparison.distance.graph.edit.GraphEditCosts;
import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.treedistance.GraphEditAssignmentCostsTreeDistanceContinuous;
import comparison.distance.graph.edit.treedistance.GraphEditAssignmentCostsTreeDistanceDiscrete;
import comparison.distance.graph.edit.treedistance.GraphEditDistanceAssignmentLinearTreeDistance;
import comparison.distance.tree.TreeDistance;
import graph.LGraph;
import graph.Graph.Vertex;
import graph.attributes.AttributedGraph;
import graph.attributes.Attributes;

public class LinGED {
	static class CommandMain extends KCommon.CommandMain {

		@Parameter(names = { "-D","--datadir" }, description = "Directory containing the data files", converter = FileConverter.class)
		File dataDir = new File("data");

		@Parameter(names = { "-G","--geddir" }, description = "Directory where the output is stored", converter = FileConverter.class)
		File gedDir = new File(".");

		@Parameter(names = { "-d", "--datasets" }, description = "List of datasets")
		List<String> datasets;

		@Parameter(names = { "-a", "--all" }, description = "Compute GED for all datasets in the data directory")
		private boolean all;
		
		@Parameter(names = { "-vc", "--vertexcosts" }, description = "Vertex insertion/deletion cost")
		int vc = 1;
		
		@Parameter(names = { "-ec", "--edgecosts" }, description = "Edge insertion/deletion cost")
		int ec = 1;

	}

	public static abstract class GedConfig<V, E> {
		abstract Distance<LGraph<V, E>> getDistance(double tauVertex, double tauEdge, AttrDataset ds);
	}

	@Parameters(commandDescription = "Approximate pairwise graph edit distances using assignments under a tree distance. For graphs with discrete labels "
			+ "a Weisfeiler-Lehman tree is used and Bisecting k-means clsutering for continous labels.")
	public static class CommandLinear extends GedConfig<Attributes, Attributes> {
		@Parameter(names = { "-p","--partitions" }, description = "partitions, i.e., the number of clusters for datasets with real-valued attributes")
		int partitions = 300;
		@Parameter(names = { "-i", "--iterations" }, description = "iterations, i.e., the number of Weisfeiler-Lehman refinement steps for datasets with discrete labels")
		int iterations = 7;
		Distance<LGraph<Attributes, Attributes>> getDistance(double tauVertex, double tauEdge, AttrDataset ds)
		{
			AttributesDistance attrDist = new AttributesDistance();
			GraphEditCosts<Attributes, Attributes> gec = new GraphEditCosts<>(tauVertex, tauVertex, tauEdge, tauEdge, attrDist, attrDist);
			
			AttributedGraph ag = ds.get(0);
    		Attributes attr = ag.getVertexLabel().get(ag.getGraph().getVertex(0));
    		if (!attr.hasRealValuedAttributes()) {
    			TreeDistance<Vertex> td = new GraphEditAssignmentCostsTreeDistanceDiscrete<Attributes, Attributes>(iterations, ds);
    			return new GraphEditDistanceAssignmentLinearTreeDistance<>(gec, td);
    		} else if (!attr.hasNominalAttributes() && attr.hasRealValuedAttributes()) {
    			TreeDistance<Vertex> td = new GraphEditAssignmentCostsTreeDistanceContinuous(partitions, ds.getRealValuedDataset());
    			return new GraphEditDistanceAssignmentLinearTreeDistance<>(gec, td);
    		} else {
    			throw new IllegalArgumentException("Not yet supported!");

    		}
		}
	}
	
	@Parameters(commandDescription = "Approximate pairwise graph edit distances using the Hungarian algorithm")
	public static class CommandBP extends GedConfig<Attributes, Attributes> {

		Distance<LGraph<Attributes, Attributes>> getDistance(double tauVertex, double tauEdge, AttrDataset ds)
		{
			AttributesDistance attrDist = new AttributesDistance();
			GraphEditCosts<Attributes, Attributes> gec = new GraphEditCosts<>(tauVertex, tauVertex, tauEdge, tauEdge, attrDist, attrDist);
			GraphEditAssignmentCosts<Attributes, Attributes> geac = new GraphEditAssignmentCostsExtended<>(gec);
			return new GraphEditDistanceAssignment<Attributes,Attributes>(new AssignmentSolver.Hungarian(), gec, geac);
		}

	}
	
	@Parameters(commandDescription = "Approximate pairwise graph edit distances using a greedy approach")
	public static class CommandGreedy extends GedConfig<Attributes, Attributes> {

		Distance<LGraph<Attributes, Attributes>> getDistance(double tauVertex, double tauEdge, AttrDataset ds)
		{
			AttributesDistance attrDist = new AttributesDistance();
			GraphEditCosts<Attributes, Attributes> gec = new GraphEditCosts<>(tauVertex, tauVertex, tauEdge, tauEdge, attrDist, attrDist);
			GraphEditAssignmentCosts<Attributes, Attributes> geac = new GraphEditAssignmentCostsExtended<>(gec);
			return new GraphEditDistanceAssignment<>(new AssignmentSolver.GreedyBasic(), gec, geac);
		}

	}

	static CommandMain cm = new CommandMain();
	static CommandLinear lin = new CommandLinear();
	static CommandBP bp = new CommandBP();
	static CommandGreedy gr = new CommandGreedy();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] argsString) throws IOException, InterruptedException {

		JCommander jc = new JCommander(cm);
		jc.addCommand("lin", lin);
		jc.addCommand("bp", bp);
		jc.addCommand("gr", gr);
		jc.getMainParameter();
		jc.setProgramName("GraphEditDistance");

		jc.parse(argsString);

		if (cm.help || jc.getParsedCommand() == null) {
			jc.usage();
			System.exit(0);
		}

		if (!cm.all && cm.datasets == null) {
			throw new ParameterException("No datasets specified.");
		}

		GedConfig kc = null;

		switch (jc.getParsedCommand()) {
		case "lin":
			kc = lin;
			break;
		case "bp":
			kc = bp;
			break;
		case "gr":
			kc = gr;
			break;
		}

		if (cm.all) {
			cm.datasets = KCommon.getDatasets(cm.dataDir);
		}
		
		for (String dName : cm.datasets) {
			AttrDataset ds = KCommon.load(dName, cm.dataDir);
			
			Distance dist = kc.getDistance(cm.vc,cm.ec,ds);
			System.out.println("Method:   " + jc.getParsedCommand());
			System.out.println("Dataset:  " + ds.getID());
			String fileName = cm.gedDir.getAbsolutePath() + "/" + ds.getID() + "_" + jc.getParsedCommand() + "_ged.txt";
			FileWriter fw = new FileWriter(fileName, false);
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < ds.size(); i++) {
				for (int j = i+1; j < ds.size(); j++) {
					bw.append(i + "\t" + j + "\t" + dist.compute(ds.get(i), ds.get(j)) + "\n");
				}
			}
			bw.close();
			System.out.println();
		}
	}
}
