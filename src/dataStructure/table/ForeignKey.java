package dataStructure.table;

import exception.InvalidSyntaxException;

public class ForeignKey implements java.io.Serializable{
	private static final long serialVersionUID = 4L;
	enum Rule {
		SET_NULL,
		CASCADE,
		RESTRICT
	}

	private String refTable = null;
	private String refColumn = null;
	private Rule deleteRule = Rule.RESTRICT;
	private Rule updateRule = Rule.RESTRICT;

	public ForeignKey(String refTable, String refColumn, String deleteRule, String updateRule) {
		this.refTable = refTable;
		this.refColumn = refColumn;

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

	public String getRefColumn() {
		return refColumn;
	}

	public void setRefColumn(String refColumn) {
		this.refColumn = refColumn;
	}

	public void setRefTable(String refTable) {
		this.refTable = refTable;
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
