package concepts;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Provides convenient methods to apply transformations to collections.
 * 
 * @author kriege
 *
 */
public class TransformationTools {
	
	/**
	 * Transforms the objects in the given set by the specified transformation.
	 * @param <I> input object type
	 * @param <O> output object type
	 * @param trans
	 * @param list
	 * @return
	 */
	public static <I,O> ArrayList<O> transformAll(Transformation<? super I,O> trans, Collection<I> list) {
		ArrayList<O> transformedSet = new ArrayList<O>(list.size());
		for (I element : list) {
			transformedSet.add(trans.transform(element));
		}
		return transformedSet;
	}
	
	/**
	 * Transforms the objects in the given set by the specified transformation.
	 * @param <I> input object type
	 * @param <O> output object type
	 * @param trans
	 * @param list
	 * @return
	 */
	public static <I,O> ArrayList<O> transformAll(Transformation<? super I,O> trans, I[] list) {
		ArrayList<O> transformedSet = new ArrayList<O>(list.length);
		for (I element : list) {
			transformedSet.add(trans.transform(element));
		}
		return transformedSet;
	}
}
