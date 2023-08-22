package dataStructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import exception.InvalidSyntaxException;

public class ForeignKey implements java.io.Serializable{
	private static final long serialVersionUID = 3L;
	enum Rule {
		SET_NULL,
		CASCADE,
		RESTRICT
	}

	private String refTable = null;
	private List<String> refColumn = new ArrayList<String>();
	private Rule deleteRule = Rule.RESTRICT;
	private Rule updateRule = Rule.RESTRICT;

	public ForeignKey(String refTable, List<String> refColumn, String deleteRule, String updateRule) {
		this.refTable = refTable;
		Iterator<String> itr = refColumn.iterator();
		while(itr.hasNext())
			this.refColumn.add(itr.next());

		this.deleteRule = parseRule(deleteRule);
		this.updateRule = parseRule(updateRule);

	}

	private Rule parseRule(String rule) {
		if(rule.equalsIgnoreCase("SET NULL")) return Rule.SET_NULL;
		else if(rule.equalsIgnoreCase("CASCADE")) return Rule.CASCADE;
		else if(rule.equalsIgnoreCase("RESTRICT")) return Rule.RESTRICT;
		else throw new InvalidSyntaxException();
	}

	public String getRefTable() {
		return refTable;
	}

	public List<String> getRefColumn() {
		return refColumn;
	}

	public String getDeleteRule() {
		if(deleteRule == Rule.SET_NULL) return "SET NULL";
		else if(deleteRule == Rule.CASCADE) return "CASCADE";
		else if(deleteRule == Rule.RESTRICT) return "RESTRICT";
		return null;
	}

	public String getUpdateRule() {
		if(updateRule == Rule.SET_NULL) return "SET NULL";
		else if(updateRule == Rule.CASCADE) return "CASCADE";
		else if(updateRule == Rule.RESTRICT) return "RESTRICT";
		return null;
	}
}
