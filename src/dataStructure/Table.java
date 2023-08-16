package dataStructure;

import java.util.LinkedList;
import java.util.List;

public class Table implements java.io.Serializable {
	private String primaryKey = null;
	private List<Tuple> tuples = new LinkedList<>();

	public void insertTuple(Tuple tuple) {
		tuples.add(tuple);
	}

	public void setPrimaryKey(String pri) {
		primaryKey = pri;
	}
}
