package kernel.dml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataStructure.Pair;
import dataStructure.table.Attribute;
import dataStructure.table.ForeignKey;
import dataStructure.table.Table;
import dataStructure.table.Type;
import exception.ExceedingItemException;
import exception.InvalidSyntaxException;
import exception.InvalidTypeException;
import exception.NotAllowForeignKeyDelete;
import exception.NotAllowForeignKeyUpdate;
import exception.NotAllowNullException;
import exception.UniqueKeyViolatonException;
import exception.WrongColumnNameException;
import exception.WrongSyntaxException;
import util.FileUtil;
import util.KernelUtil;

public class DataSetting {
	public static void insertCommand(String cmd) {
		String regex = "\\s*VALUES\\s*";
		Pattern pattern = Pattern.compile(regex);

        String [] wholeClause = pattern.split(cmd);

        String insertClause = wholeClause[0];
        String [] insertClauseParse = insertClause.split("\\s+");
        if(!(insertClauseParse[0]+' '+insertClauseParse[1]).equalsIgnoreCase("INSERT INTO"))
        	throw new WrongSyntaxException();

		String[] header = insertClause.trim().split("\\s+");
		String tableName = header[2];

		Table table = FileUtil.readObjectFromFile(new Table(), tableName + ".bin");
		List<String> pKey = table.getPrimaryKey();
		List<Type> attributeType = new ArrayList<Type>();
		List<Attribute> attributes = table.getAttribute();

		for (Attribute attr : attributes) {
			attributeType.add(attr.getType());
		}

		regex = "\\(([^)]+)\\)"; // 괄호 안의 데이터만 가져온다.
		pattern = Pattern.compile(regex);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(tableName + ".txt", true))) {
			for (int i = 1; i < wholeClause.length; i++) {
				StringBuilder sb = new StringBuilder();
				Matcher matcher = pattern.matcher(wholeClause[i]);
				String[] seperatedData;
				if (matcher.find())
					seperatedData = matcher.group(1).trim().split("[\\s,']+");
				else
					throw new InvalidSyntaxException();
				if (seperatedData.length > attributes.size())
					throw new ExceedingItemException();

				for (int j = 0; j < attributes.size(); j++) {
					String data = seperatedData[j];
					Attribute attribute = attributes.get(j);
					Type type = attributeType.get(j);

					// primary key들 체크

					if (data != null) {
						System.out.println(type.getTypeName()+"###"+data);
						if (insertTypeCheck(type, data) == false)
							throw new InvalidTypeException();
						if (KernelUtil.isPrimaryKey(pKey, attributes.get(j).getField())) {
							if (insertPrimaryKeyCheck(table, pKey, data) == false)
								throw new UniqueKeyViolatonException();
						}
						sb.append(data + "\t");
					} else {
						String typeName = type.getTypeName();
						boolean allowNull = attribute.getAllowNull();
						String nullValue = translateNullLogic(typeName, allowNull);
						sb.append(nullValue + "\t");
					}
				}
				bw.write(sb.toString());
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void updateCommand(String cmd) {
		String[] item = cmd.trim().split("\n|\r\n");
		String[] header = item[0].trim().split("\\s+");
		String tableName = header[1];

		Table table = FileUtil.readObjectFromFile(new Table(), tableName + ".bin");
		List<String> pKey = table.getPrimaryKey();
		List<Attribute> attributes = table.getAttribute();
		List<Type> attributeType = new ArrayList<Type>();
		List<ForeignKey> infoForeignKey = new ArrayList<ForeignKey>();

		//List<Pair<String, String>> deRefTablesInfo = table.getDeRefsInfo();
		for (Attribute attr : attributes) {
			attributeType.add(attr.getType());
			infoForeignKey.add(attr.getInfoForeignKey());
		}

		String setClause = item[1].trim();
		String whereClause = null;
		if (item.length >= 3)
			whereClause = item[2].trim();

		String[] setClauseParse = setClause.split("\\s+");
		if (!setClauseParse[0].equalsIgnoreCase("SET"))
			throw new InvalidSyntaxException();

		String whereFieldName = null;
		String whereFieldData = null;

		// where절 조건의 field가 몇 번째 field인지 찾는다.
		int updateIdx = -1;
		if (whereClause != null) {
			String[] whereClauseParse = whereClause.split("\\s+");
			whereFieldName = whereClauseParse[1];
			if (whereClauseParse[2].equals("="))
				whereFieldData = whereClauseParse[3].replaceAll("'", "");
			else
				throw new InvalidSyntaxException();
			updateIdx = KernelUtil.findAttributeIndex(attributes, whereFieldName);
			if (updateIdx == -1)
				throw new WrongColumnNameException();
		}

		// set절 조건들의 field 위치와 데이터들을 포함한 setDataAsIdx를 구성한다.
		ArrayList<Pair<Integer, String>> setDataAsIdx = new ArrayList<Pair<Integer, String>>();
		String[] setData = new String[2]; // (name, data)
		for (int i = 1; i < setClauseParse.length; i += 3) {
			setData[0] = setClauseParse[i];
			setData[1] = setClauseParse[i + 2].replaceAll("'", "");
			setDataAsIdx.add(new Pair(KernelUtil.findAttributeIndex(attributes, setData[0]), setData[1]));
		}

		// setDataAsIdx에서 set이 가능한지 type 체크를 한다.
		for (Pair<Integer, String> pair : setDataAsIdx) {
			int columnIdx = pair.first;
			String columnData = pair.second;
			Type columnType = attributeType.get(updateIdx);
			if (insertTypeCheck(columnType, columnData) == false)
				throw new InvalidTypeException();
		}

		// 데이터 업데이트
		try (BufferedReader br = new BufferedReader(new FileReader(tableName + ".txt"));
				BufferedWriter bw = new BufferedWriter(new FileWriter(tableName + "temp.txt"))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine(); // header
			bw.write(line + System.lineSeparator());
			while (true) {
				line = br.readLine();
				if (line == null)
					break;
				String[] parseLine = line.split("\\s+");
				for (Pair<Integer, String> pair : setDataAsIdx) {
					String name = pair.second;
					int idx = pair.first;
					Attribute attr = attributes.get(idx); // 추가됨
					List<Pair<String, String>> deRefTablesInfo = attr.getDeRefsInfo(); // 추가됨
					if (isUpdateColumn(parseLine, whereFieldData, updateIdx)) {
						if (KernelUtil.isPrimaryKey(pKey, attributes.get(idx).getField())) {
							if (insertPrimaryKeyCheck(table, pKey, name) == false)
								throw new UniqueKeyViolatonException();
							// 외래키 처리
							for (Pair<String, String> deRefTable : deRefTablesInfo) {
								updateForeignkeyLogic(attr, deRefTable, parseLine[idx], name); // 역 참조 테이블 하나씩 업데이트
							}
						}
						parseLine[idx] = name;
					}
				}

				// 현재 테이블 업데이트
				for (String parseData : parseLine)
					sb.append(parseData + "\t");
				sb.append("\n");
				// deRef 테이블 업데이트

			}
			bw.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 원본 파일 삭제 및 임시 파일을 원본 파일로
		File inputFile = new File(tableName + ".txt");
		File tempFile = new File(tableName + "temp.txt");
		inputFile.delete();
		tempFile.renameTo(inputFile);
	}

	public static void deleteCommand(String cmd) {
		String[] item = cmd.trim().split("\n|\r\n");
		String[] header = item[0].trim().split("\\s+");
		String tableName = header[1];

		Table table = FileUtil.readObjectFromFile(new Table(), tableName + ".bin");
		List<Attribute> attributes = table.getAttribute();
		//List<Pair<String, String>> deRefTablesInfo = table.getDeRefsInfo();

		String whereClause = item[1];

		String whereFieldName = null;
		String whereFieldData = null;

		// where절 조건의 field가 몇 번째 field인지 찾는다.
		int updateIdx = -1;
		if (whereClause != null) {
			String[] whereClauseParse = whereClause.split("\\s+");
			whereFieldName = whereClauseParse[1];
			if (whereClauseParse[2].equals("="))
				whereFieldData = whereClauseParse[3].replaceAll("'", "");
			else
				throw new InvalidSyntaxException();
			updateIdx = KernelUtil.findAttributeIndex(attributes, whereFieldName);
			if (updateIdx == -1)
				throw new WrongColumnNameException();
		}

		// 데이터 업데이트
		try (BufferedReader br = new BufferedReader(new FileReader(tableName + ".txt"));
				BufferedWriter bw = new BufferedWriter(new FileWriter(tableName + "temp.txt"))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine(); // header
			bw.write(line + System.lineSeparator());
			while (true) {
				line = br.readLine();
				if (line == null)
					break;
				String[] parseLine = line.split("\\s+");
				if (parseLine[updateIdx].equals(whereFieldData)) {
					// 외래키 삭제 루틴
					// 1. 삭제할 값 구하기
					List<Integer> pKeyIdx = findPrimaryKeyIdx(table);
					List<String> pKeyValues = findPrimaryKeyValue(tableName, whereFieldData, updateIdx, pKeyIdx);
					// 2. 삭제하기
					for(Attribute attr: attributes) {
						for (Pair<String, String> deRefTable : attr.getDeRefsInfo()) {
							deleteForeignkeyLogic(deRefTable, pKeyValues);
						}
					}
					// string builder에 현재 칼럼 추가하지 않고 continue
					continue;
				}
				// 현재 테이블 업데이트
				for (String parseData : parseLine)
					sb.append(parseData + "\t");
				sb.append("\n");

				// deRef 테이블 업데이트

			}
			bw.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		File inputFile = new File(tableName + ".txt");
		File tempFile = new File(tableName + "temp.txt");
		inputFile.delete();
		tempFile.renameTo(inputFile);
	}

	private static String translateNullLogic(String typeName, boolean allowNull) {
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

	private static boolean insertTypeCheck(Type type, String data) {
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

	private static boolean insertPrimaryKeyCheck(Table table, List<String> primaryKey, String insertData) {
		String tableName = table.getTableName();
		List<Attribute> attrs = table.getAttribute();
		List<Integer> primaryKeyIdx = new ArrayList<Integer>();
		String[] insertDataParse = insertData.split("\\s+");

		// 기본키의 인덱스 추출
		for (String pKey : primaryKey) {
			for (int i = 0; i < attrs.size(); i++) {
				if (pKey.equalsIgnoreCase(attrs.get(i).getField())) {
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
				if (duplicatedDataCount == primaryKey.size())
					return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private static String[] parseSetData(String parseData) {
		String[] setData = new String[2]; // (name, data)
		String regex = "\\b(\\w+\\s+\\w+\\s+\\w+)\\b";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(parseData);

		int idx = 0;
		while (matcher.find()) {
			String extractedWord = matcher.group(1);
			String[] extractedWordParse = extractedWord.trim().split("\\s+");
			if (!extractedWordParse[1].equals("="))
				throw new InvalidSyntaxException();
			setData[0] = extractedWordParse[0];
			setData[1] = extractedWordParse[2].replaceAll("\"", "");

			idx++;
		}
		return setData;
	}

	private static void parseWhereClause(String whereClause, String pWhereFieldName, String pWhereFieldNewData) {
		String[] whereClauseParse = whereClause.split("\\s+");
		pWhereFieldName = whereClauseParse[1];
		System.out.println(pWhereFieldName);
		if (whereClauseParse[2].equals("="))
			pWhereFieldNewData = whereClauseParse[3].replaceAll("'", "");
		else
			throw new InvalidSyntaxException();
	}

	public static boolean isUpdateColumn(String[] parseLine, String whereName, int updateIdx) {
		if (whereName == null)
			return true;
		if (parseLine[updateIdx].equals(whereName))
			return true;
		return false;
	}

	private static List<Integer> findPrimaryKeyIdx(Table table) {
		List<Attribute> attr = table.getAttribute();
		List<String> pKeys = table.getPrimaryKey();
		List<Integer> idx = new ArrayList<Integer>();

		for (int i = 0; i < attr.size(); i++) {
			String attrName = attr.get(i).getField();
			for (String pKey : pKeys) {
				if (attrName.equalsIgnoreCase(pKey)) {
					idx.add(i);
					continue;
				}
			}
		}
		return idx;
	}

	private static List<String> findPrimaryKeyValue(String tableName, String otherAttrValue, int otherAttrIdx,
			List<Integer> pKeyIdx) {
		List<String> pKeyValue = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(tableName + ".txt"))) {
			br.readLine(); // 헤더 읽기
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				String[] parseLine = line.split("\\s+");
				if (parseLine[otherAttrIdx].equalsIgnoreCase(otherAttrValue)) {
					for (Integer idx : pKeyIdx) {
						pKeyValue.add(parseLine[idx]);
					}
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return pKeyValue;
	}

	private static void updateForeignkeyLogic(Attribute attr, Pair<String, String> deRefInfo, String oldData,
			String updateData) { // 역 참조 테이블 하나씩 업데이트
		String deRefTableName = deRefInfo.first;
		String deRefColumnName = deRefInfo.second;
		Table deRefTable = FileUtil.readObjectFromFile(new Table(), deRefTableName + ".bin");
		String updateDataType = attr.getType().getTypeName();

		String fieldName = attr.getField();
		List<Attribute> deRefAttributes = deRefTable.getAttribute();

		// 몇 번째 attr을 업데이트 해야하는지 알아내기
		int updateIdx = -1;
		for (int i = 0; i < deRefAttributes.size(); i++) {
			if (deRefAttributes.get(i).getField().equalsIgnoreCase(deRefColumnName))
				updateIdx = i;
		}

		// fk참조 => restrict, set null, cascade에 따라 updateData 갱신하기
		ForeignKey infoForeignKey = deRefAttributes.get(updateIdx).getInfoForeignKey();
		String updateRule = infoForeignKey.getUpdateRule();
		if (updateRule.equalsIgnoreCase("restrict"))
			throw new NotAllowForeignKeyUpdate();
		else if (updateRule.equalsIgnoreCase("set null"))
			updateData = translateNullLogic(updateDataType, true);

		// 업데이트
		try (BufferedReader br = new BufferedReader(new FileReader(deRefTableName + ".txt"));
				BufferedWriter bw = new BufferedWriter(new FileWriter(deRefTableName + "temp.txt"))) {
			String line = br.readLine(); // header
			bw.write(line + System.lineSeparator());
			while (true) {
				line = br.readLine();
				if (line == null)
					break;
				String[] parseLine = line.split("\\s+");
				String updateCandidateData = parseLine[updateIdx];
				if (updateCandidateData.equalsIgnoreCase(oldData))
					parseLine[updateIdx] = updateData;
				StringBuilder sb = new StringBuilder();
				for (String parseData : parseLine)
					sb.append(parseData + "\t");
				bw.write(sb.toString() + System.lineSeparator());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 원본 파일 삭제 및 임시 파일을 원본 파일로
		File inputFile = new File(deRefTableName + ".txt");
		File tempFile = new File(deRefTableName + "temp.txt");
		inputFile.delete();
		tempFile.renameTo(inputFile);
	}

	private static void deleteForeignkeyLogic(Pair<String, String> deRefInfo, List<String> oldData) {
		String deRefTableName = deRefInfo.first;
		String deRefColumnName = deRefInfo.second;
		Table deRefTable = FileUtil.readObjectFromFile(new Table(), deRefTableName + ".bin");
		StringBuilder sb = new StringBuilder();

		List<Attribute> deRefAttributes = deRefTable.getAttribute();

		// 몇 번째 attr을 삭제 해야하는지 알아내기
		int deleteIdx = -1;
		for (int i = 0; i < deRefAttributes.size(); i++) {
			if (deRefAttributes.get(i).getField().equalsIgnoreCase(deRefColumnName))
				deleteIdx = i;
		}

		String deleteDataType = deRefAttributes.get(deleteIdx).getType().getTypeName();

		// on delete 값에 따른 처리
		ForeignKey infoForeignKey = deRefAttributes.get(deleteIdx).getInfoForeignKey();
		String deleteRule = infoForeignKey.getDeleteRule();
		if (deleteRule.equalsIgnoreCase("restrict"))
			throw new NotAllowForeignKeyDelete();

		try (BufferedReader br = new BufferedReader(new FileReader(deRefTableName + ".txt"));
				BufferedWriter bw = new BufferedWriter(new FileWriter(deRefTableName + "temp.txt"))) {
			String line = br.readLine(); // header
			bw.write(line + System.lineSeparator());
			while (true) {
				line = br.readLine();
				if (line == null)
					break;
				String[] parseLine = line.split("\\s+");
				String deleteCandidateData = parseLine[deleteIdx];
				for (String data : oldData) {
					if (deleteCandidateData.equalsIgnoreCase(data)) {
						if (deleteRule.equalsIgnoreCase("set null"))
							parseLine[deleteIdx] = translateNullLogic(deleteDataType, true);
						else if (deleteRule.equals("cascade"))
							continue;
					}
				}

				for (String parseData : parseLine)
					sb.append(parseData + "\t");
				sb.append(System.lineSeparator());
			}
			bw.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 원본 파일 삭제 및 임시 파일을 원본 파일로
		File inputFile = new File(deRefTableName + ".txt");
		File tempFile = new File(deRefTableName + "temp.txt");
		inputFile.delete();
		tempFile.renameTo(inputFile);
	}
}