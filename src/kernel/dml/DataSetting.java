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
import exception.NotAllowForeignKeyUpdate;
import exception.NotAllowNullException;
import exception.UniqueKeyViolatonException;
import exception.WrongColumnNameException;
import util.FileUtil;
import util.KernelUtil;

public class DataSetting {
	public static void insertCommand(String cmd) {
		String[] item = cmd.trim().split("\n|\r\n");
		String[] header = item[0].trim().split("\\s+");
		String tableName = header[2];

		Table table = FileUtil.readObjectFromFile(new Table(), tableName + ".bin");
		List<String> pKey = table.getPrimaryKey();
		List<Type> attributeType = new ArrayList();
		List<Attribute> attributes = table.getAttribute();

		for (Attribute attr : attributes) {
			attributeType.add(attr.getType());
		}
		String regex = "\\(([^)]+)\\)"; // 괄호 안의 데이터만 가져온다.
		Pattern pattern = Pattern.compile(regex);

		try (BufferedWriter br = new BufferedWriter(new FileWriter(tableName + ".txt", true))) {
			for (int i = 1; i < item.length; i++) {
				StringBuilder sb = new StringBuilder();
				Matcher matcher = pattern.matcher(item[i]);
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
						if (KernelUtil.isPrimaryKey(pKey, attributes.get(j).getField())) {
							if (insertPrimaryKeyCheck(data, j, tableName) == false)
								throw new UniqueKeyViolatonException();
						} else {
							if (insertTypeCheck(type, data) == false) {
								throw new InvalidTypeException();
							}
						}
						sb.append(data + "\t");
					} else {
						String typeName = type.getTypeName();
						boolean allowNull = attribute.getAllowNull();
						String nullValue = insertNullLogic(typeName, allowNull);
						sb.append(nullValue + "\t");
					}
				}
				br.write(sb.toString());
				br.newLine();
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
		String whereFieldNewData = null;

		// where절 조건의 field가 몇 번째 field인지 찾는다.
		int updateIdx = -1;
		if (whereClause != null) {
			String[] whereClauseParse = whereClause.split("\\s+");
			whereFieldName = whereClauseParse[1];
			if (whereClauseParse[2].equals("="))
				whereFieldNewData = whereClauseParse[3].replaceAll("'", "");
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
			String columnName = pair.second;
			Type columnType = attributeType.get(updateIdx);
			if (insertTypeCheck(columnType, columnName) == false)
				throw new InvalidTypeException();
		}

		// 데이터 업데이트
		try (BufferedReader br = new BufferedReader(new FileReader(tableName + ".txt"));
				BufferedWriter bw = new BufferedWriter(new FileWriter(tableName + "temp.txt"))) {
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
					if (isUpdateColumn(parseLine, whereFieldNewData, updateIdx)) {
						if (KernelUtil.isPrimaryKey(pKey, attributes.get(idx).getField())) {
							if (insertPrimaryKeyCheck(name, idx, tableName) == false)
								throw new UniqueKeyViolatonException();
							// 외래키 처리
							List<Pair<String, String>> deRefTables = table.getDeRefInfos();
							for(Pair<String, String> deRefTable: deRefTables)
								updateForeignkeyLogic(attributes.get(idx), deRefTable, parseLine[idx], name);
						}
						parseLine[idx] = name;
					}
				}

				//현재 테이블 업데이트
				StringBuilder sb = new StringBuilder();
				for (String parseData : parseLine)
					sb.append(parseData + "\t");
				bw.write(sb.toString() + System.lineSeparator());

				//deRef 테이블 업데이트

			}
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

	}

	public static String insertNullLogic(String typeName, boolean allowNull) {
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

	public static boolean insertTypeCheck(Type type, String data) {
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

	public static boolean insertPrimaryKeyCheck(Table table, List<String> primaryKey, String insertData) {
		String tableName = table.getTableName();
		List<Attribute> attrs = table.getAttribute();
		List<Integer> primaryKeyIdx = new ArrayList<Integer>();
		String [] insertDataParse = insertData.split("\\s+");

		// 기본키의 인덱스 추출
		for(String pKey: primaryKey) {
			for(int i=0; i<attrs.size(); i++) {
				if(pKey.equalsIgnoreCase(attrs.get(i).getField())) {
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
				for(Integer idx: primaryKeyIdx) {
					if(tupleParse[idx].equals(insertDataParse[idx])) duplicatedDataCount++;
				}
				if(duplicatedDataCount == primaryKey.size()) return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static String[] parseSetData(String parseData) {
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

	public static boolean isUpdateColumn(String[] parseLine, String whereName, int updateIdx) {
		if (whereName == null)
			return true;
		if (parseLine[updateIdx].equals(whereName))
			return true;
		return false;
	}

	public static void updateForeignkeyLogic(Attribute attr, Pair<String, String> deRefInfo, String oldData, String updateData) {
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
			updateData = insertNullLogic(updateDataType, true);

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
}