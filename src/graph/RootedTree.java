package graph;

public interface RootedTree extends Graph {
	
	public TreeVertex getRoot();
	
	public interface TreeVertex extends Graph.Vertex {
		public TreeVertex getParent();
		public Iterable<? extends TreeVertex> children();
	}
	
}
