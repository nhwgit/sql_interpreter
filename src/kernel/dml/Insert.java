package kernel.dml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataStructure.table.Attribute;
import dataStructure.table.Table;
import dataStructure.table.Type;
import exception.ExceedingItemException;
import exception.InvalidSyntaxException;
import exception.InvalidTypeException;
import exception.UniqueKeyViolatonException;
import util.FileUtil;
import util.KernelUtil;

public class Insert {

	String insertClause;
	List<String> valueClause = new ArrayList<>();
	String tableName;
	Table table;
	List<String> tablePKey;
	List<Attribute> tableAttrs;

	public void parsingAndInit(String cmd) {
		String regex = "\\s*VALUES\\s*";
		Pattern pattern = Pattern.compile(regex);
		String[] wholeClause = pattern.split(cmd);

		insertClause = wholeClause[0];

		String[] insertClauseParse = insertClause.split("\\s+");
		if (!(insertClauseParse[0] + ' ' + insertClauseParse[1]).equalsIgnoreCase("INSERT INTO"))
			throw new InvalidSyntaxException();

		tableName = insertClauseParse[2];
		for(int i=1; i<wholeClause.length; i++) {
			valueClause.add(wholeClause[i]);
		}

		table = FileUtil.readObjectFromFile(new Table(), tableName + ".bin");
		tablePKey = table.getPrimaryKey();
		tableAttrs = table.getAttribute();
	}

	public void insertCommand(String cmd) {
		String regex = "\\(([^)]+)\\)"; // 괄호 안의 데이터만 가져온다.
		Pattern pattern = Pattern.compile(regex);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(tableName + ".txt", true))) {
			for (int i = 0; i < valueClause.size(); i++) {
				StringBuilder sb = new StringBuilder();
				Matcher matcher = pattern.matcher(valueClause.get(i));
				String[] seperatedData; // 데이터 하나 추출
				if (matcher.find())
					seperatedData = matcher.group(1).trim().split("[\\s,']+");
				else
					throw new InvalidSyntaxException();
				if (seperatedData.length > tableAttrs.size())
					throw new ExceedingItemException();

				for (int j = 0; j < tableAttrs.size(); j++) {
					String data = seperatedData[j];
					Attribute attribute = tableAttrs.get(j);
					sb.append(appendColumn(attribute, data));
				}
				bw.write(sb.toString());
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean insertTypeCheck(Type type, String data) {
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

	private boolean insertPrimaryKeyCheck(String insertData) {
		List<Integer> primaryKeyIdx = new ArrayList<>();
		String[] insertDataParse = insertData.split("\\s+");

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

	private String appendColumn(Attribute attr, String data) {
		Type type = attr.getType();
		if (data != null) {
			if (insertTypeCheck(type, data) == false)
				throw new InvalidTypeException();
			if (KernelUtil.isPrimaryKey(tablePKey, attr.getField())) {
				if (insertPrimaryKeyCheck(data) == false)
					throw new UniqueKeyViolatonException();
			}
			return data + "\t";
		} else {
			String typeName = type.getTypeName();
			boolean allowNull = attr.getAllowNull();
			String nullValue = insertUtil.translateNullLogic(typeName, allowNull);
			return nullValue + "\t";
		}
	}
}
