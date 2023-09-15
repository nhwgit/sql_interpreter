package kernel.dml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataStructure.table.Attribute;
import dataStructure.table.Table;
import dataStructure.table.Type;
import exception.NotAllowNullException;

public class InsertUtil {
	static String translateNullLogic(String typeName, boolean allowNull) {
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

	static boolean insertTypeCheck(Type type, String data) {
		String typeName = type.getTypeName();
		if (typeName.equalsIgnoreCase("NUMBER")) {
			try {
				Integer.parseInt(data);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		} else if (typeName.equalsIgnoreCase("VARCHAR")) {
			if (data.length() > type.getTypeSize())
				return false;
			else
				return true;
		}
		return false;
	}

	static boolean insertPrimaryKeyCheck(Table table, String insertData) {
		List<Integer> primaryKeyIdx = new ArrayList<>();
		String[] insertDataParse = insertData.split("\\s+");

		List<String> tablePKey = table.getPrimaryKey();
		String tableName = table.getTableName();
		List<Attribute> tableAttrs = table.getAttribute();

		// 기본키의 인덱스 추출
		for (String pKey : tablePKey) {
			for (int i = 0; i < tableAttrs.size(); i++) {
				if (pKey.equalsIgnoreCase(tableAttrs.get(i).getField())) {
					primaryKeyIdx.add(i);
					break;
				}
			}
		}

		try (BufferedReader br = new BufferedReader(new FileReader(tableName + ".txt"))) {
			br.readLine(); // 헤더 읽기
			String tuple;
			while (true) { // 한 줄씩 기본키와 겹치는지 체크
				tuple = br.readLine();
				if (tuple == null)
					break;
				String[] tupleParse = tuple.trim().split("\\s+");
				int duplicatedDataCount = 0; // 기본키중 몇 개가 겹치는지 => 전부 겹치면 false
				for (Integer idx : primaryKeyIdx) {
					if (tupleParse[idx].equals(insertDataParse[idx]))
						duplicatedDataCount++;
				}
				if (duplicatedDataCount == tablePKey.size())
					return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

}
