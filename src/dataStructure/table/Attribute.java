package dataStructure.table;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dataStructure.Pair;
import exception.DuplicatedNameException;

public class Attribute implements java.io.Serializable {
	private String field;
	private Type type;
	private boolean allowNull = true;
	private ForeignKey infoForeignKey = null;
	private List<Pair<String, String>> deRefInfos = new LinkedList<>(); // (테이블명, 칼럼명)
	private static final long serialVersionUID = 4L;

	public Attribute() {
		this.allowNull = true;
	}

	public Attribute(String field, Type type, boolean allowNull, ForeignKey infoForeignKey) {
		this.field = field;
		this.type = type;
		this.allowNull = allowNull;
		this.infoForeignKey = infoForeignKey;
	}

	public String getField() {
		return field;
	}

	public Type getType() {
		return type;
	}

	public boolean getAllowNull() {
		return allowNull;
	}

	public void setField(String field) {
		this.field = field;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
	}

	public void setInfoForeignKey(ForeignKey infoForeignKey) {
		this.infoForeignKey = infoForeignKey;
	}

	public ForeignKey getInfoForeignKey() {
		return infoForeignKey;
	}

	public void addDeRefInfos(Pair<String, String> deRefTable) {
		Iterator<Pair<String, String>> itr = deRefInfos.iterator();
		while(itr.hasNext()) {
			Pair<String, String> pair = itr.next();
			String tableName = pair.first;
			if(tableName.equals(deRefTable)) throw new DuplicatedNameException();
		}
		deRefInfos.add(deRefTable);
	}

	public List<Pair<String, String>> getDeRefsInfo() {
		return deRefInfos;
	}
}
