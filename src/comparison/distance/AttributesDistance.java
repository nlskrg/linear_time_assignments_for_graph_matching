package comparison.distance;

import graph.attributes.Attributes;

/**
 * Identity distance between nominal attributes plus the 
 * Euclidean distance between the real valued attributes.
 * 
 * @author Nils Kriege
 *
 */
public class AttributesDistance implements Distance<Attributes> {

	public static Distance<Object> id = new IdentityDistance();
	public static Distance<double[]> ed = new EuclideanDistance();
	
	@Override
	public double compute(Attributes g1, Attributes g2) {
		
		Object[] o1 = g1.getNominalAttributes();
		Object[] o2 = g2.getNominalAttributes();
		
		double dist = 0;
		
		for (int i=0; i<o1.length; i++) {
			dist += id.compute(o1[i], o2[i]);
		}
		
		dist += ed.compute(g1.getRealValuedAttributes(), g2.getRealValuedAttributes());
		
		return dist;
	}
	
	

}
