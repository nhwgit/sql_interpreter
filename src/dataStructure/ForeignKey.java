package dataStructure;

public class ForeignKey{
	enum DeleteRule {
		SET_NULL,
		CASCADE
	}

	private String refTable = null;
	private String refColumn = null;
	private DeleteRule dRule = DeleteRule.SET_NULL;

	public ForeignKey(String refTable, String refColumn, String dRule) {
		this.refTable = refTable;
		this.refColumn = refColumn;
		if(dRule.equalsIgnoreCase("SET NULL")) this.dRule = DeleteRule.SET_NULL;
		else if(dRule.equalsIgnoreCase("CASCADE")) this.dRule = DeleteRule.CASCADE;
		else; // 예외처리
	}
}
