package comparison.distance.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import algorithm.graph.isomorphism.WLAmenableTest;
import algorithm.graph.isomorphism.WLAmenableTest.GraphType;
import graph.ConnectivityTools;
import graph.Graph;
import graph.LGraph;
import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.Graph.Vertex;
import graph.properties.VertexArray;

/**
 * Interface for tie breaking strategies. Implementing classes may determine
 * the order of leaves and how elements associated with the same leaf are
 * paired.
 * 
 * @author kriege
 *
 * @param <T>
 */
public interface TiebreakStrategy<T> {
	
	public LinkedList<AdjListRTreeVertex> leafOrder(ArrayList<T> A, ArrayList<T> B, VertexArray<ArrayList<Integer>> AL,	VertexArray<ArrayList<Integer>> BL, LinkedList<AdjListRTreeVertex> leaves);
	
	public void pairElements(ArrayList<T> A, ArrayList<T> B, ArrayList<Integer> eA, ArrayList<Integer> eB, AdjListRTreeVertex v, int[] assignment);


	/**
	 * Pairs the elements according to their given order.
	 * 
	 * @author kriege
	 *
	 * @param <T>
	 */
	public static class DefaultTiebreakStrategy<T> implements TiebreakStrategy<T> {
	
		@Override
		public void pairElements(ArrayList<T> A, ArrayList<T> B, ArrayList<Integer> eA, ArrayList<Integer> eB, AdjListRTreeVertex v,
				int[] assignment) {
	
			while (!eA.isEmpty() && !eB.isEmpty()) {
				Integer a = eA.remove(eA.size()-1);
				Integer b = eB.remove(eB.size()-1);
				assignment[a] = b;
			}
		}
	
		@Override
		public LinkedList<AdjListRTreeVertex> leafOrder(ArrayList<T> A, ArrayList<T> B,
				VertexArray<ArrayList<Integer>> AL, VertexArray<ArrayList<Integer>> BL,
				LinkedList<AdjListRTreeVertex> leaves) {
			return leaves;
		}
	}
	
	/**
	 * Pairs the elements in random order.
	 * 
	 * @author kriege
	 *
	 * @param <T>
	 */
	public static class RandomTiebreakStrategy<T> implements TiebreakStrategy<T> {
		
		Random rng = new Random(31434442);
	
		@Override
		public void pairElements(ArrayList<T> A, ArrayList<T> B, ArrayList<Integer> eA, ArrayList<Integer> eB, AdjListRTreeVertex v,
				int[] assignment) {
	
			while (!eA.isEmpty() && !eB.isEmpty()) {
				Integer a = eA.remove(rng.nextInt(eA.size()));
				Integer b = eB.remove(rng.nextInt(eB.size()));
				assignment[a] = b;
			}
			
		}
	
		@Override
		public LinkedList<AdjListRTreeVertex> leafOrder(ArrayList<T> A, ArrayList<T> B,
				VertexArray<ArrayList<Integer>> AL, VertexArray<ArrayList<Integer>> BL,
				LinkedList<AdjListRTreeVertex> leaves) {
			return leaves;
		}
	}
	
	/**
	 * For WL-amenable graphs and a sufficient large number of refinement steps, this 
	 * procedure guarantees that the the graph edit distance is zero if and only if
	 * the two input graphs are isomorphic.
	 * 
	 * Implements the procedure described in the proof of Theorem 9 in the paper by 
	 * Arvind et al. https://arxiv.org/pdf/1502.01255.pdf
	 * 
	 * @author kriege
	 */
	public static class AmenableTiebreakStrategy implements TiebreakStrategy<Vertex> {
	
		Graph G;
		Graph H;
		HashMap<AdjListRTreeVertex,ArrayList<Vertex>> parentCell;
		
		public AmenableTiebreakStrategy(Graph G, Graph H) {
			this.G = G;
			this.H = H;
		}
		
		@Override
		public void pairElements(ArrayList<Vertex> A, ArrayList<Vertex> B, ArrayList<Integer> eA, ArrayList<Integer> eB, AdjListRTreeVertex l,
				int[] assignment) {
			
			if (eA.isEmpty() && eB.isEmpty()) return;
			
			ArrayList<Vertex> dominantCell = parentCell.get(l); 

			// if there is a dominant cell (parent in antiisotripic component)
			// build consistent mapping
			if (dominantCell != null) {
				Iterator<Integer> it = eA.iterator();
				while (it.hasNext()) {
					Integer a = it.next();
					Vertex v = A.get(a);
					// find matched neighbors in dominant cell
					ArrayList<Vertex> matchedNeighbors = new ArrayList<>();
					HashSet<Vertex> matchedPartners = new HashSet<>();
					for (Vertex w : v.neighbors()) {
						int w2 = assignment[A.indexOf(w)];
						if (w2 != -1) {
							// check whether the neighbor is in dominant cell
							if (dominantCell.contains(w)) {
								matchedNeighbors.add(w);
								matchedPartners.add(B.get(w2));
							}
						}
					}
					if (!matchedNeighbors.isEmpty()) {
						// find best partner
						int maxCount = Integer.MIN_VALUE;
						Integer maxCandId = -1;
						for (Integer b : eB) {
							Vertex v2 = B.get(b);
							int countMatches = 0;
							for (Vertex w2 : v2.neighbors()) {
								if (matchedPartners.contains(w2)) {
									countMatches++;
								}
							}
							if (countMatches>maxCount) {
								maxCount = countMatches;
								maxCandId = b;
							}
						}
						it.remove(); // removes a from eA
						eB.remove(maxCandId);
						assignment[a] = maxCandId;
						
					}
				}
			}
			
			// prepare date structure
			HashSet<Vertex> eAv = new HashSet<>();
			for (Integer i : eA) {
				eAv.add(A.get(i));
			}
			HashSet<Vertex> eBv = new HashSet<>();
			for (Integer i : eB) {
				eBv.add(B.get(i));
			}
			// is complement of a matching graph?
			// this test is sufficient if we assume the graph is 
			// one of the 5 possible types of CR amenable graphs;
			// the complement of a matching graph on 2 vertices is empty!
			boolean isMatchingComplement = eA.size() > 2; 
			for (Vertex av : eAv) {
				// count number of neighbors is in eAv
				int count = 0;
				for (Vertex avn : av.neighbors()) {
					if (eAv.contains(avn)) count++;
				}
				// in a matching complement the vertex is not adjacent 
				// to itself and one other vertex
				if (count != eAv.size() - 2) { 
					isMatchingComplement = false;
					break;
				}
			}
			
			while (!eA.isEmpty() && !eB.isEmpty()) {
				// 1. pick elements of A and B
				Integer a = eA.remove(eA.size()-1);
				eAv.remove(A.get(a));
				Integer b = eB.remove(eB.size()-1);
				eBv.remove(B.get(b));
				assignment[a] = b;
				
	
				// 2. assign neighbors
				while (true) {
					Vertex av = A.get(a);
					Vertex bv = B.get(assignment[a]);
					
					Vertex avCand = pick(av, eAv, isMatchingComplement);
					Vertex bvCand = pick(bv, eBv, isMatchingComplement);
					if (avCand == null || bvCand == null) {
						break;
					}
					a = A.indexOf(avCand);
					eA.remove((Integer)a); // object, not index
					eAv.remove(avCand);
					b = B.indexOf(bvCand);
					eB.remove((Integer)b);
					eBv.remove(bvCand);
					assignment[a] = b;
					
				}
			}
		}
		
		public Vertex pick(Vertex av, HashSet<Vertex> eAv, boolean isMatchingComplement) {
			if (!isMatchingComplement) {
				for (Vertex avn : av.neighbors())
					if (eAv.contains(avn)) {
						return avn;
					}
			} else {
				HashSet<Vertex> c = (HashSet<Vertex>)eAv.clone();
				for (Vertex avn : av.neighbors())
					c.remove(avn);
				if (c.isEmpty()) {
					return null;
				} else {
					return c.iterator().next();
				}
			}
			return null;				
		}
	
		@Override
		public LinkedList<AdjListRTreeVertex> leafOrder(ArrayList<Vertex> A, ArrayList<Vertex> B,
				VertexArray<ArrayList<Integer>> AL, VertexArray<ArrayList<Integer>> BL,
				LinkedList<AdjListRTreeVertex> leaves) {
			
			parentCell = new HashMap<>();
			
			LinkedList<AdjListRTreeVertex> result = new LinkedList<>();
			
			ArrayList<ArrayList<Vertex>> cells = new ArrayList<>(leaves.size());
			for (AdjListRTreeVertex l : leaves) {
				ArrayList<Vertex> cell = new ArrayList<>(AL.get(l).size());
				for (Integer i : AL.get(l)) {
					cell.add(A.get(i));
				}
				cells.add(cell);
			}
			
			LGraph<GraphType, GraphType> cLG = WLAmenableTest.createCellGraph(G, cells);
			Graph cG = cLG.getGraph();
			
			
			for (LinkedList<Vertex> cc : ConnectivityTools.connectedComponents(cG)) {
			
				
				Vertex heterogeneaus = null;
				int minCardinality = Integer.MAX_VALUE;
				Vertex minCardinalityVertex = null;
				for (Vertex v : cc) {
					int vCardinality = cells.get(v.getIndex()).size();
					if (vCardinality < minCardinality) {
						minCardinality = vCardinality;
						minCardinalityVertex = v;
					}
					if (cLG.getVertexLabel().get(v) != GraphType.EMPTY && 
							cLG.getVertexLabel().get(v) != GraphType.COMPLETE) {
						heterogeneaus = v;
					}
				}
				
				Vertex root = heterogeneaus != null ? heterogeneaus : minCardinalityVertex;
				
				
				VertexArray<Vertex> parent = new VertexArray<>(cG);
				
				LinkedList<Vertex> level = new LinkedList<>();
				LinkedList<Vertex> nextLevel = new LinkedList<>();
				level.add(root);
				// the root is its own parent; this allows to distinguish the root
				// from unvisited vertices
				parent.set(root, root);  
				while(!level.isEmpty()) {
					while(!level.isEmpty()) {
						Vertex v = level.pop();
						result.add(leaves.get(v.getIndex()));
						for (Vertex w : v.neighbors()) {
							if (parent.get(w) != null) {
								if (parent.get(v) == w) continue;
								// TODO we have a tree -- how to handle this?
								continue;
							}
							parent.set(w, v);
							// set parent Cell
							parentCell.put(leaves.get(w.getIndex()), cells.get(v.getIndex()));
							nextLevel.add(w);
						}
					}
					level = nextLevel;
					nextLevel = new LinkedList<>();
				}
			}
			
			return result;
			
		}
	}
}
