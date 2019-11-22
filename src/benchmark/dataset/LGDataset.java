package benchmark.dataset;

import java.util.Collection;

import datastructure.NTuple;
import graph.LGraph;


/**
 * Base class for datasets of labeled graphs of the same type.
 * 
 * Note: A LabeledGraph g added to a dataset should be annotated by a class 
 * label returned by g.getProperty("class").
 * 
 * @author kriege
 *
 * @param <LG> labeled graph type
 */
public class LGDataset<LG extends LGraph<?, ?>> extends Dataset<LG> {
	
	private static final long serialVersionUID = 1L;
	
	public LGDataset(String id) {
		super(id);
	}
	
	@Override
	public String getClassLabel(LG lg) {
		return getClassString(lg);
	}
	
	/**
	 * Creates a subset of this dataset containing only graphs with
	 * at most maxVertexCount vertices.
	 * 
	 * @param maxVertexCount threshold value.
	 * @return a subset of this dataset
	 */
	public LGDataset<LG> createSmallGraphSubset(int maxVertexCount) {
		return createSmallGraphSubset(maxVertexCount, new LGDataset<LG>(id));
	}
	
	protected <T extends LGDataset<LG>> T createSmallGraphSubset(int maxVertexCount, T r) {
		for (LG lg : this) {
			if (lg.getGraph().getVertexCount() <= maxVertexCount) {
				r.add(lg);
			}
		}
		return r;
	}
	
	public void createRandomTrainingTestSplit(double trainFraction, boolean balanced) {
		
		// add all to training
		for (LG lg : this) {
			lg.getGraph().setProperty("set", "train");
		}
		
		// create and update test set
		Dataset<LG> test = this.createRandomSubset((int)(1d-trainFraction)*size(), balanced);
		for (LG lg : test) {
			lg.getGraph().setProperty("set", "test");
		}
	}
	
	public void createRandomTrainingValidationTestSets(double trainFraction, double validationFraction, boolean balanced) {
		int trainSize = (int)(trainFraction*size());
		int validationSize = (int)(validationFraction*size());
		LGDataset<LG> test = new LGDataset<LG>(id);
		test.addAll(this);
		Dataset<LG> train=test.createRandomSubset(trainSize, balanced);
		test.removeAll(train);
		Dataset<LG> validation=test.createRandomSubset(validationSize, balanced);
		test.removeAll(validation);
		for (LG lg : train) { lg.getGraph().setProperty("set", "train"); }
		for (LG lg : validation) { lg.getGraph().setProperty("set", "valid"); }
		for (LG lg : test) { lg.getGraph().setProperty("set", "test"); }
	}
	
	public NTuple<LGDataset<LG>> getTrainingValidationTestSets() {
		LGDataset<LG> train = new LGDataset<LG>(id);
		LGDataset<LG> validation = new LGDataset<LG>(id);
		LGDataset<LG> test = new LGDataset<LG>(id);
		for (LG lg : this) {
			if (lg.getGraph().getProperty("set").equals("train")) {
				train.add(lg);
			} else if (lg.getGraph().getProperty("set").equals("valid")) {
				validation.add(lg);
			} else if (lg.getGraph().getProperty("set").equals("test")) {
				test.add(lg);
			}
		}
		return new NTuple<>(train, validation, test);
	}
	
	public LGDataset<LG> getTrainingSet() {
		return getTrainingValidationTestSets().get(0);
	}
	
	public LGDataset<LG> getValidationSet() {
		return getTrainingValidationTestSets().get(1);
	}
	
	public LGDataset<LG> getTestSet() {
		return getTrainingValidationTestSets().get(2);
	}
	
	public void printStatistics() {
		System.out.println("ID: " +getID());
		Statistics.printLabeledGraphStatistics(this);
	}

	public static String[] getClassesArray(Collection<? extends LGraph<?,?>> lgs) {
		String[] r = new String[lgs.size()];
		int i = 0;
		for (LGraph<?, ?> lg : lgs) {
			r[i++] = getClassString(lg);
		}
		return r;
	}
	
	public static String getClassString(LGraph<?, ?> lg) {
		Object set = lg.getGraph().getProperty("class");
//		if (set instanceof Number) {
//			Number s =  (Number)set;
//			return (s.intValue() == 1) ? "+1" : "-1";
//		}
//		String result = (set.equals("1") || set.equals("+1")) ? "+1" : "-1";
//		return result;
		return String.valueOf(set);
	}

	@Override
	public Dataset<LG> newEmptyInstance(String id) {
		return new LGDataset<>(id);
	}
	

}
