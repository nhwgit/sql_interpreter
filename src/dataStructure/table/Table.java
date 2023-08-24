package dataStructure.table;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import exception.DuplicatedNameException;

public class Table implements java.io.Serializable {
	private String tableName;
	private List<String> primaryKey = new LinkedList<>();
	private List<Attribute> attributes = new LinkedList<>();
	private List<String> deRefTables = new LinkedList<>();
	private static final long serialVersionUID = 3L;

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void insertAttribute(Attribute tuple) {
		String newField = tuple.getField();
		Iterator<Attribute> itr = attributes.iterator();
		while(itr.hasNext()) {
			String field = itr.next().getField();
			if(field.equals(newField)) throw new DuplicatedNameException();
		}

		attributes.add(tuple);
	}

	public void setNullPrimaryKey() {
		primaryKey = null;
	}

	public void addPrimaryKey(String pri) {
		Iterator<String> itr = primaryKey.iterator();
		while(itr.hasNext()) {
			String name = itr.next();
			if(name.equals(pri)) throw new DuplicatedNameException();
		}
		primaryKey.add(pri);
	}

	public void addDeRefTables(String deRefTable) {
		Iterator<String> itr = deRefTables.iterator();
		while(itr.hasNext()) {
			String name = itr.next();
			if(name.equals(deRefTable)) throw new DuplicatedNameException();
		}
		deRefTables.add(deRefTable);
	}

	public List<String> getDeRefTables() {
		return deRefTables;
	}

	public List<Attribute> getAttribute() {
		return attributes;
	}

	public List<String> getPrimaryKey() {
		return primaryKey;
	}

	public void printTableInfo() {
		System.out.println("#################");
		Iterator<Attribute> itr = attributes.iterator();
		while(itr.hasNext()) {
			Attribute tuple = itr.next();
			Iterator<String> itr2 = primaryKey.iterator();
			while(itr2.hasNext())
				if(itr2.next().equals(tuple.getField())) System.out.println("primaryKey");
			System.out.println("field: " + tuple.getField());
			Type type = tuple.getType();
			System.out.println("type: " + type.getTypeName());
			System.out.println("type Size: " + type.getTypeName());
			System.out.println("allow null: " + tuple.getAllowNull());
			ForeignKey fk = tuple.getInfoForeignKey();
			System.out.println("#####Foreign key#####");
			if(fk != null) {
				System.out.println("refTable: "+fk.getRefTable());
				System.out.println("refColumn: "+fk.getRefColumn());
				System.out.println("refUpdateRule: "+fk.getUpdateRule());
				System.out.println("refDeleteRule: "+fk.getDeleteRule());
			}
			else {
				System.out.println("foreign key: None");
			}
			Iterator<String> itr3 = deRefTables.iterator();
			System.out.println("#################\n");
		}
		Iterator<String> itr3 = deRefTables.iterator();
		System.out.println("#####Dereference Table#####");
		while(itr3.hasNext())
			System.out.println("-"+itr3.next());
	}
}