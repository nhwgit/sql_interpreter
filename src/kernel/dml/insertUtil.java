package kernel.dml;

import exception.NotAllowNullException;

public class insertUtil {
	public static String translateNullLogic(String typeName, boolean allowNull) {
		String ret = null;
		if (allowNull == true) {
			if (typeName.equalsIgnoreCase("NUMBER"))
				ret = "0";
			else if (typeName.equalsIgnoreCase("VARCHAR"))
				ret = "null";
		} else
			throw new NotAllowNullException();
		return ret;
	}
}
