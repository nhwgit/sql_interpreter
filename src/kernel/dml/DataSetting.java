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

import dataStructure.Attribute;
import dataStructure.Pair;
import dataStructure.Table;
import dataStructure.Type;
import exception.ExceedingItemException;
import exception.InvalidSyntaxException;
import exception.InvalidTypeException;
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
				Matcher matcher = pattern.matcher(item[i]);
				String[] seperatedData;
				if (matcher.find())
					seperatedData = matcher.group(1).trim().split("[\\s,']+");
				else
					throw new InvalidSyntaxException();
				if (seperatedData.length > attributeType.size())
					throw new ExceedingItemException();
				for (int j = 0; j < attributeType.size(); j++) {
					String data = seperatedData[j];
					Attribute attribute = attributes.get(j);
					Type type = attributeType.get(j);
					if (data != null) {
						if (KernelUtil.isPrimaryKey(pKey, attributes.get(j).getField())) {
							if (insertUniqueKeyCheck(data, j, tableName) == false)
								throw new UniqueKeyViolatonException();
						} else {
							if (insertTypeCheck(type, data) == false)
								throw new InvalidTypeException();
						}
						br.write(data + "\t");
					} else {
						String typeName = type.getTypeName();
						boolean allowNull = attribute.getAllowNull();
						String nullValue = insertNullLogic(typeName, allowNull);
						br.write(nullValue + "\t");
					}
				}
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

		for(Attribute attr: attributes)
			attributeType.add(attr.getType());

		String setClause = item[1].trim();
		String whereClause = null;
		if (item.length >= 3)
			whereClause = item[2].trim();

		String[] setClauseParse = setClause.split("\\s+");
		if (!setClauseParse[0].equalsIgnoreCase("SET"))
			throw new InvalidSyntaxException();

		String whereFieldType = null;
		String whereFieldNewData = null;

		//where절 조건의 field가 몇 번째 field인지 찾는다.
		int updateIdx = -1;
		if (whereClause != null) {
			String[] whereClauseParse = whereClause.split("\\s+");
			whereFieldType = whereClauseParse[1];
			if (whereClauseParse[2].equals("="))
				whereFieldNewData = whereClauseParse[3].replaceAll("'","");
			else
				throw new InvalidSyntaxException();
			updateIdx = KernelUtil.findAttributeIndex(attributes, whereFieldType);
			if(updateIdx == -1)
				throw new WrongColumnNameException();
		}

		//set절 조건들의 field 위치와 데이터들을 포함한 setDataAsIdx를 구성한다.
		ArrayList<Pair<Integer, String>> setDataAsIdx = new ArrayList<Pair<Integer, String>>();
		String [] setData = new String[2]; // (name, data)
		for(int i=1; i<setClauseParse.length; i+=3) {
			setData[0] = setClauseParse[i];
			setData[1] = setClauseParse[i+2].replaceAll("'", "");
			setDataAsIdx.add(new Pair(KernelUtil.findAttributeIndex(attributes, setData[0]), setData[1]));
		}

		//setDataAsIdx에서 set이 가능한지 type 체크를 한다.
		for(Pair<Integer, String> pair : setDataAsIdx) {
			int columnIdx = pair.first;
			String columnName = pair.second;
			Type columnType = attributeType.get(updateIdx);
			if(insertTypeCheck(columnType, columnName) == false)
				throw new InvalidTypeException();
		}

		//데이터 업데이트
		try (
			BufferedReader br = new BufferedReader(new FileReader(tableName + ".txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tableName + "temp.txt"))) {
			String line = br.readLine(); // header
			bw.write(line + System.lineSeparator());
			while(true) {
				line = br.readLine();
				if(line == null) break;
				String [] parseLine = line.split("\\s+");
				for(Pair<Integer, String> pair: setDataAsIdx) {
					String name = pair.second;
					int idx = pair.first;
					if(isUpdateColumn(parseLine, whereFieldNewData, updateIdx)) {
						if (KernelUtil.isPrimaryKey(pKey, attributes.get(idx).getField())) {
							if (insertUniqueKeyCheck(name, idx, tableName) == false)
								throw new UniqueKeyViolatonException();
							//foreign key 관련
							List<String> derefTables = table.getDeRefTables();
							for(String derefTable: derefTables) {

							}
						}
						parseLine[idx] = name;
					}
				}
				StringBuilder sb = new StringBuilder();
				for(String parseData: parseLine)
					sb.append(parseData+"\t");
				bw.write(sb.toString() + System.lineSeparator());

				//원본 파일 삭제 및 임시 파일을 원본 파일로
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
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

	public static boolean insertUniqueKeyCheck(String data, int idx, String tableName) {
		try (BufferedReader br = new BufferedReader(new FileReader(tableName + ".txt"))) {
			br.readLine(); // 헤더 읽기
			String tuple;
			while (true) {
				tuple = br.readLine();
				if (tuple == null)
					break;
				String[] TupleParse = tuple.trim().split("\\s+");
				if (TupleParse[idx].equalsIgnoreCase(data))
					return false;
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
}
