package util;

import java.util.List;

import dataStructure.Attribute;
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

	public static boolean isPrimaryKey(List<String> infoPrimaryKey, String attrName) {
		for(String s: infoPrimaryKey) {
			if(s.equalsIgnoreCase(attrName)) return true;
		}
		return false;
	}

	public static int getFieldIdx(String header, String findData) {
		String [] headerParse = header.trim().split("\\s+");
		for(int i=0; i<headerParse.length; i++) {
			if(headerParse[i].equalsIgnoreCase(findData)) return i;
		}
		return -1;
	}

	public static int findAttributeIndex(List<Attribute> attributes, String findField) {
		for(int i=0; i<attributes.size(); i++) {
			if(attributes.get(i).getField().equals(findField))
				return i;
		}
		return -1;
	}
}
