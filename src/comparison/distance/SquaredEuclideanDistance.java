package comparison.distance;

public class SquaredEuclideanDistance implements Distance<double[]> {

	@Override
	public double compute(double[] g1, double[] g2) {
		double qsum = 0;
		for (int i=0; i<g1.length; i++) {
			qsum += Math.pow(g1[i] - g2[i], 2);
		}
		return qsum;
	}
	
	public static double compute(double x1, double x2, double y1, double y2) {
		double qsum = 0;
		qsum += Math.pow(x1 - y1, 2);
		qsum += Math.pow(x2 - y2, 2);
		return qsum;
	}

}
