package kernel.dml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataStructure.Pair;
import dataStructure.table.Attribute;
import dataStructure.table.ForeignKey;
import dataStructure.table.Table;
import exception.InvalidSyntaxException;
import exception.NotAllowForeignKeyDelete;
import exception.WrongColumnNameException;
import util.FileUtil;
import util.KernelUtil;

public class Delete {

	Table table;
	String tableName;
	List<Attribute> tableAttrs;
    String whereClause;
    String whereFieldName;
    String whereFieldData;

    public void parsingAndInit(String cmd) {
    	String[] item = cmd.trim().split("\n|\r\n");
		String[] header = item[0].trim().split("\\s+");
    	tableName = header[1];
    	table = FileUtil.readObjectFromFile(new Table(), tableName + ".bin");
    	tableAttrs = table.getAttribute();
    	whereClause = item[1];
    }

	public void deleteCommand() {

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
			updateIdx = KernelUtil.findAttributeIndex(tableAttrs, whereFieldName);
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
					List<String> pKeyValues = findPrimaryKeyValue(tableName, updateIdx, pKeyIdx);
					// 2. 삭제하기
					for(Attribute attr: tableAttrs) {
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
			}
			bw.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileUtil.updateFile(tableName);
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

	private List<String> findPrimaryKeyValue(String tableName, int otherAttrIdx,
			List<Integer> pKeyIdx) {
		List<String> pKeyValue = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(tableName + ".txt"))) {
			br.readLine(); // 헤더 읽기
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				String[] parseLine = line.split("\\s+");
				if (parseLine[otherAttrIdx].equalsIgnoreCase(whereFieldData)) {
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

	private void deleteForeignkeyLogic(Pair<String, String> deRefInfo, List<String> oldData) {
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
							parseLine[deleteIdx] = InsertUtil.translateNullLogic(deleteDataType, true);
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
		FileUtil.updateFile(deRefTableName);
	}
}