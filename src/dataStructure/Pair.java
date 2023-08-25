package dataStructure;

public class Pair<T, U> implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
    public T first;
    public U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
}