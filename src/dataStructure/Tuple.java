package dataStructure;

public class Tuple implements java.io.Serializable {
	private String field;
	private String type;
	private int typeSize = 0;
	private boolean allowNull = true;
	private ForeignKey infoForeignKey = null;

	public Tuple(String field, String type, int typeSize, boolean allowNull, ForeignKey infoForeignKey) {
		this.field = field;
		this.type = type;
		this.typeSize = typeSize;
		this.allowNull = allowNull;
	}
}
