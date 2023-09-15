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

import dataStructure.Pair;
import dataStructure.table.Attribute;
import dataStructure.table.ForeignKey;
import dataStructure.table.Table;
import dataStructure.table.Type;
import exception.InvalidSyntaxException;
import exception.InvalidTypeException;
import exception.NotAllowForeignKeyUpdate;
import exception.UniqueKeyViolatonException;
import exception.WrongColumnNameException;
import util.FileUtil;
import util.KernelUtil;

public class Update {
	String tableName;
	Table table;
	List<String> tablePKey;
	List<Attribute> tableAttrs;
	String updateClause;
    String setClause;
    String whereClause;
    String whereFieldName;
    String whereFieldData;
    List<Pair<Integer, String>> setDataAsIdx; //(name, data)

    public void parsingAndInit(String cmd) {
    	Pattern withWherePattern = Pattern.compile("UPDATE\\s+(.*)\\s+SET\\s+(.*)\\s+WHERE\\s+(.*)");
		Pattern withoutWherePattern = Pattern.compile("UPDATE\\s+(.*)\\s+SET\\s+(.*)");
        Matcher withWhereMatcher = withWherePattern.matcher(cmd);
        Matcher withoutWhereMatcher = withoutWherePattern.matcher(cmd);

        if(withWhereMatcher.find() ) {
        	updateClause = withWhereMatcher.group(1);
            setClause = withWhereMatcher.group(2);
            whereClause = withWhereMatcher.group(3);
        }
        else if(withoutWhereMatcher.find()) {
        	updateClause = withoutWhereMatcher.group(1);
            setClause = withoutWhereMatcher.group(2);
        }
        else throw new InvalidSyntaxException();

        String[] header = updateClause.trim().split("\\s+");
		tableName = header[0];
		table = FileUtil.readObjectFromFile(new Table(), tableName + ".bin");
		tablePKey = table.getPrimaryKey();
		tableAttrs = table.getAttribute();

		setDataAsIdx = new ArrayList<>();
    }

    public void updateCommand() {
		List<Type> attributeType = new ArrayList<>();
		List<ForeignKey> infoForeignKey = new ArrayList<>();

		for (Attribute attr : tableAttrs) {
			attributeType.add(attr.getType());
			infoForeignKey.add(attr.getInfoForeignKey());
		}
		String[] setClauseParse = setClause.split("\\s+");

		// where절 조건의 field가 몇 번째 field인지 찾는다
		int updateIdx = -1;
		if (whereClause != null) {
			extractWhereClause();
			updateIdx = KernelUtil.findAttributeIndex(tableAttrs, whereFieldName);
			if (updateIdx == -1)
				throw new WrongColumnNameException();
		}

		// set절 조건들의 field 위치와 데이터들을 포함한 setDataAsIdx를 구성한다.
		initsetDataAsIdx(setClauseParse);

		// setDataAsIdx에서 set이 가능한지 type 체크를 한다.
		for (Pair<Integer, String> pair : setDataAsIdx) {
			String columnData = pair.second;
			Type columnType = attributeType.get(updateIdx);
			if (InsertUtil.insertTypeCheck(columnType, columnData) == false)
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
					Attribute attr = tableAttrs.get(idx); // 추가됨
					List<Pair<String, String>> deRefTablesInfo = attr.getDeRefsInfo(); // 추가됨
					if (isUpdateColumn(parseLine, updateIdx)) {
						if (KernelUtil.isPrimaryKey(tablePKey, attr.getField())) {
							if (InsertUtil.insertPrimaryKeyCheck(table, name) == false)
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
		FileUtil.updateFile(tableName);
	}

	private void initsetDataAsIdx(String [] setClauseParse) {
		String[] setData = new String[2]; // (name, data)
		for (int i = 0; i < setClauseParse.length; i += 3) {
			setData[0] = setClauseParse[i];
			setData[1] = setClauseParse[i + 2].replaceAll("'", "");
			setDataAsIdx.add(new Pair(KernelUtil.findAttributeIndex(tableAttrs, setData[0]), setData[1]));
		}

	}

	private void extractWhereClause() {
		String[] whereClauseParse = whereClause.split("\\s+");
		whereFieldName = whereClauseParse[0];
		if (whereClauseParse[1].equals("="))
			whereFieldData = whereClauseParse[2].replaceAll("'", "");
		else
			throw new InvalidSyntaxException();
	}

	private boolean isUpdateColumn(String[] parseLine, int updateIdx) {
		if (whereFieldData == null)
			return true;
		if (parseLine[updateIdx].equals(whereFieldData))
			return true;
		return false;
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
			updateData = InsertUtil.translateNullLogic(updateDataType, true);

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
		FileUtil.updateFile(deRefTableName);
	}
}
