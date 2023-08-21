package dataStructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import exception.InvalidSyntaxException;

public class ForeignKey implements java.io.Serializable{
	private static final long serialVersionUID = 2L;
	enum DeleteRule {
		SET_NULL,
		CASCADE
	}

	private String refTable = null;
	private List<String> refColumn = new ArrayList<String>();
	private DeleteRule dRule = DeleteRule.SET_NULL;

	public ForeignKey(String refTable, List<String> refColumn, String dRule) {
		this.refTable = refTable;
		Iterator<String> itr = refColumn.iterator();
		while(itr.hasNext())
			this.refColumn.add(itr.next());

		if(dRule.equalsIgnoreCase("SET NULL")) this.dRule = DeleteRule.SET_NULL;
		else if(dRule.equalsIgnoreCase("CASCADE")) this.dRule = DeleteRule.CASCADE;
		else throw new InvalidSyntaxException();
	}

	public String getRefTable() {
		return refTable;
	}

	public List<String> getRefColumn() {
		return refColumn;
	}

	public String getdRule() {
		if(dRule == DeleteRule.SET_NULL) return "SET NULL";
		else if(dRule == DeleteRule.CASCADE) return "CASCADE";
		return null;
	}
}
