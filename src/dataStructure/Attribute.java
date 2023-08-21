package dataStructure;

public class Attribute implements java.io.Serializable {
	private String field;
	private Type type;
	private boolean allowNull = true;
	private ForeignKey infoForeignKey = null;
	private static final long serialVersionUID = 3L;

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

	public ForeignKey getInfoForeignKey() {
		return infoForeignKey;
	}
}
