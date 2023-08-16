package dataStructure;

import java.util.Iterator;
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

	public void printTableInfo() {
		if(primaryKey != null) System.out.println("primaryKey: "+ primaryKey);
		Iterator<Tuple> itr = tuples.iterator();
		while(itr.hasNext()) {
			Tuple tuple = itr.next();
			System.out.println("field: " + tuple.getField());
			System.out.println("type: " + tuple.getType());
			System.out.println("type Size: " + tuple.getTypeSize());
			System.out.println("allowNull: " + tuple.getAllowNull());
			ForeignKey fk = tuple.getInfoForeignKey();
			System.out.println("#####Foreign key#####")
			if(fk != null) {
				System.out.println("refTable: "+fk.getRefTable());
				System.out.println("refColumn: "+fk.getRefColumn());
				System.out.println("refDRule: "+fk.getdRule());
			}
			else {
				System.out.println("foreign key: None")
			}
		}
	}
}
