package graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Note: Use createRoot()/createChild() instead of createVertex()/createEdge() to 
 * assure consistency!
 */
public class AdjListRootedTree extends AdjListGraph implements RootedTree {
	
	AdjListRTreeVertex root;
	
	public AdjListRootedTree() {
		super();
	}

	public AdjListRootedTree(int n) {
		super(n, n-1);
	}

	public AdjListRTreeVertex getRoot() {
		return root;
	}
	
	public ArrayList<AdjListRTreeVertex> findLeaves() {
		ArrayList<AdjListRTreeVertex> leaves = new ArrayList<>();
		Queue<AdjListRTreeVertex> Q  = new LinkedList<>();
		Q.offer(getRoot());
		while (!Q.isEmpty()) {
			AdjListRTreeVertex v = Q.poll();
			if (v.isLeaf()) {
				leaves.add(v);
			} else {
				Q.addAll(v.children());
			}
		}
		return leaves;
	}

	
	public void setRoot(AdjListRTreeVertex root) {
		this.root = root;
	}

	public AdjListRTreeVertex createVertex() {
		AdjListRTreeVertex v = new AdjListRTreeVertex(vertices.size());
		vertices.add(v);
		
		notifyVertexCreated(v);
		return v;
	}
	
	public AdjListRTreeVertex createRoot() {
		if (root != null) throw new RuntimeException("Root already exists");
		root = createVertex();
		root.setParent(root);
		return root;
	}
	
	public AdjListRTreeVertex createChild(AdjListRTreeVertex p) {
		AdjListRTreeVertex c = createVertex();
		c.setParent(p);
		p.addChild(c);
		createEdge(p, c);
		return c;
	}
	
	public void setParent(AdjListRTreeVertex child, AdjListRTreeVertex newParent) {
		assert child != root;
		AdjListRTreeVertex currentParent = child.getParent();
		AdjListEdge e = getEdge(child, currentParent);
		
		e.setOppositeVertex(child, newParent);
		currentParent.children.remove(child);
		newParent.addChild(child);	
		child.setParent(newParent);
	}
	
	// TODO make superclass generic and remove this
	public AdjListRTreeVertex getVertex(int index) {
		return (AdjListRTreeVertex)vertices.get(index);
	}
	
	public class AdjListRTreeVertex extends AdjListVertex implements TreeVertex {
		
		AdjListRTreeVertex parent;
		ArrayList<AdjListRTreeVertex> children;
		
		private AdjListRTreeVertex(int index) {
			super(index);
			children = new ArrayList<AdjListRTreeVertex>();
		}
		
		private AdjListRTreeVertex(int index, AdjListRTreeVertex parent) {
			this(index);
			this.parent = parent;
		}

		public AdjListRTreeVertex getParent() {
			return parent;
		}
		
		protected void setParent(AdjListRTreeVertex parent) {
			this.parent = parent;
		}

		public ArrayList<AdjListRTreeVertex> children() {
			return children;
		}
		
		public boolean isLeaf() {
			return children.isEmpty();
		}
		
		private void addChild(AdjListRTreeVertex child) {
			this.children.add(child);
		}

		
		// TODO make superclass generic and remove this
		public Iterable<AdjListRTreeVertex> neighbors() {
			return ImplementationHelper.createNeighborIterator(this);
		}
		
	}
	

}
