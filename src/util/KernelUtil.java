package util;

import dataStructure.Type;

public class KernelUtil {
	public static Type typeGenerator(String cmd) {
		int openParenPosition = cmd.indexOf("(");
		int closeParenPosition = cmd.indexOf(")");
		String typeName = null;
		int typeSize = 0;
		if(openParenPosition == -1 || closeParenPosition == -1) {
			typeName = cmd;
			typeSize = 20;
		}
		else {
			typeName = cmd.substring(0, openParenPosition);
			typeSize = Integer.parseInt(cmd.substring(openParenPosition+1, closeParenPosition));
		}
		return new Type(typeName, typeSize);
	}
}
