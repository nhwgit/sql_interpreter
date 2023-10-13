package dataStructure;

public class AttributeInfo {
	private String tableName; // 테이블 혹은 별칭 이름
	private String attrName;
	private String typeName;

	AttributeInfo(String tableName, String attrName, String typeName) {
		this.tableName = tableName;
		this.attrName = attrName;
		this.typeName = typeName;
	}

	String getTableName() {
		return tableName;
	}

	String getAttrName() {
		return attrName;
	}

	String getTypeName() {
		return typeName;
	}
}
