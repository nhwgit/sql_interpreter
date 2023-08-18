package dataStructure;

public class Type implements java.io.Serializable {
	private String typeName;
	private int typeSize;

	public Type(String typeName, int typeSize) {
		this.typeName = typeName;
		this.typeSize = typeSize;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public int getTypeSize() {
		return typeSize;
	}

	public void setTypeSize(int typeSize) {
		this.typeSize = typeSize;
	}
}
