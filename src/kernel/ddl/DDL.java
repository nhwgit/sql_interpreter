package kernel.ddl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import dataStructure.Pair;
import dataStructure.table.Attribute;
import dataStructure.table.ForeignKey;
import dataStructure.table.Table;
import dataStructure.table.Type;
import exception.DeRefAlreadyExistenceException;
import exception.DisableForeignKeyGenerateException;
import exception.FileAlreadyExistenceException;
import exception.InvalidSyntaxException;
import exception.NotAllowAlterModifyException;
import exception.NotExistPrimaryKeyException;
import exception.NotSupportedTypeException;
import exception.TooManyPrimarykeyException;
import exception.WrongColumnNameException;
import util.FileUtil;
import util.KernelUtil;

public class DDL {
	public static void createCommand(String cmd) {
		int headerSize = cmd.indexOf("(");
		String[] header = cmd.substring(0, headerSize).split(" ");
		String type = header[1].toUpperCase();
		String objectName = header[2];
		File table = new File(objectName + ".bin");
		if (table.exists())
			throw new FileAlreadyExistenceException();
		switch (type) {
		case "TABLE":
			Table tableInfo = createTableLogic(cmd.substring(headerSize + 1), objectName);
			FileUtil.writeObjectToFile(tableInfo, objectName + ".bin");
			try (FileWriter fr = new FileWriter(objectName + ".txt")) {
				List<Attribute> attributes = tableInfo.getAttribute();
				Iterator<Attribute> itr = attributes.iterator();
				while (itr.hasNext()) {
					Attribute attr = itr.next();
					fr.write(attr.getField() + '\t');
				}
				fr.write("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case "INDEX":
			createIndexLogic();
			break;
		default:
			throw new NotSupportedTypeException();
		}
	}

	public static void alterCommand(String cmd) {
		String[] item = cmd.trim().split("\n|\r\n");
		String header = item[0];
		String action = item[1];

		String[] headerParse = header.trim().split("\\s+");

		String type = headerParse[1];
		String objectName = headerParse[2];

		switch (type) {
		case "TABLE":
			Table tableInfo = alterTableLogic(action, objectName + ".bin");
			FileUtil.writeObjectToFile(tableInfo, objectName + ".bin");
			break;
		case "INDEX":
			break;
		}
	}

	public static void dropCommand(String cmd) {
		String[] item = cmd.trim().split("\\s+");
		String type = item[1];
		String FileName = item[2];
		System.out.println(FileName);
		Path filePath = Paths.get(FileName + ".bin");
		switch (type) {
		case "TABLE":
			try {
				Files.delete(filePath);
			} catch (NoSuchFileException e) {
				System.out.println("삭제하려는 파일이 없습니다.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		case "INDEX":
		default:
			throw new NotSupportedTypeException();
		}
	}

	private static Table createTableLogic(String cmd, String tableName) {
		Table table = new Table();
		table.setTableName(tableName);
		String[] columns = cmd.split(",");
		for (String column : columns)
			newColumnRegisterLogic(table, column);
		return table;
	}

	private static Table alterTableLogic(String cmd, String fileName) {
		int index = cmd.indexOf(" ");
		String type = cmd.substring(0, index).toUpperCase();
		Table table = FileUtil.readObjectFromFile(new Table(), fileName);
		switch (type) {
		case "ADD":
			alterAddLogic(table, cmd); // 칼럼 추가
			break;
		case "RENAME":
			String judge = cmd.substring(index + 1, index + 3);
			if (judge.equalsIgnoreCase("TO"))
				alterRenameToLogic(table, cmd, fileName); // 테이블명 변경
			else
				alterRenameLogic(table, cmd); // 칼럼명 변경
			break;
		case "MODIFY":
			alterModifyLogic(table, cmd); // 데이터타입 변경
			break;
		case "DROP":
			alterDropLogic(table, cmd); // 칼럼 삭제
			break;
		default:
			throw new InvalidSyntaxException();
		}
		return table;
	}

	private static Table alterAddLogic(Table table, String cmd) {
		String[] cmdParse = cmd.trim().split("\\s+");
		StringBuilder tmp = new StringBuilder();
		for (int i = 2; i < cmdParse.length; i++)
			tmp.append(cmdParse[i] + ' ');
		String registerCmd = (tmp.toString()).trim();
		newColumnRegisterLogic(table, registerCmd);
		return table;
	}

	private static void alterRenameLogic(Table table, String cmd) {
		String[] cmdParse = cmd.trim().split("\\s+");
		String existName = cmdParse[2];
		String newName = cmdParse[4];
		List<Attribute> tableInfo = table.getAttribute();
		for (Attribute t : tableInfo) {
			if (t.getField().equals(existName)) {
				t.setField(newName);
				List<String> pms = table.getPrimaryKey();
				for (String pm : pms) {
					if (pm.equals(existName))
						pm = newName;
				}
			}
		}
	}

	private static void alterModifyLogic(Table table, String cmd) {
		String[] cmdParse = cmd.trim().split("\\s+");
		String columnName = cmdParse[2];
		String newType = cmdParse[3];
		Type type = null;

		//테이블에 이미 데이터가 있을 경우 타입 변경 불허
		try (BufferedReader br = new BufferedReader(new FileReader(table.getTableName() + ".txt"))) {
			br.readLine();
			if(br.readLine() != null) throw new NotAllowAlterModifyException();
		} catch(IOException e) {
			e.printStackTrace();
		}

		List<Attribute> tableInfo = table.getAttribute();
		for (Attribute t : tableInfo) {
			if (t.getField().equals(columnName)) {
				type = KernelUtil.typeGenerator(newType);
				t.setType(type);
			}
		}
		if (type == null)
			throw new WrongColumnNameException();
	}

	private static void alterDropLogic(Table table, String cmd) {
		String[] cmdParse = cmd.trim().split("\\s+");
		String columnName = cmdParse[2];
		List<Attribute> tableInfo = table.getAttribute();
		Iterator<Attribute> itr = tableInfo.iterator();
		while (itr.hasNext()) {
			Attribute element = itr.next();
			if (element.getField().equals(columnName)) {
				itr.remove();
				List<String> primaryKeys = table.getPrimaryKey();
				Iterator<String> itr2 = primaryKeys.iterator();
				while (itr2.hasNext()) {
					if (itr2.next().equals(columnName))
						table.setNullPrimaryKey();
				}
			}
		}
	}

	private static void alterRenameToLogic(Table table, String cmd, String fileName) { //테이블 명 변경
		String[] cmdParse = cmd.trim().split("\\s+");
		String newName = cmdParse[2];
		String oldName = table.getTableName();
		List<Attribute> attributes = table.getAttribute();

		try {
			Path oldFile = Paths.get(fileName);
			Path newFile = Paths.get(newName + ".bin");
			Files.move(oldFile, newFile);
			if (table.getPrimaryKey().size() >= 1) { // 기본키가 존재할 때
				updateAlterRenameToTable(table, oldName, newName);
			}
			table.setTableName(newName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void newColumnRegisterLogic(Table table, String cmd) {
		String[] item = cmd.trim().split("\\s+|\\);");
		String field = null;
		Type type = null;
		boolean allowNull = true;
		ForeignKey infoForeignKey = null;
		Attribute attribute = new Attribute();
		try {
			field = item[0];
			type = KernelUtil.typeGenerator(item[1]);

			for (int i = 2; i < item.length; i++) { // not null, pk, fk등
				if (i < item.length - 1) {
					String command = (item[i] + " " + item[i + 1]).toUpperCase().trim();
					switch (command) {
					case "NOT NULL":
						allowNull = false;
						i++;
						break;
					case "PRIMARY KEY":
						if (table.getPrimaryKey().size() >= 1)
							throw new DeRefAlreadyExistenceException();
						table.addPrimaryKey(field);
						i++;
						break;
					case "FOREIGN KEY":
						i += 2;
						if (item[i].equalsIgnoreCase("REFERENCES")) {
							i++;
							String refTableName = item[i];
							Table refTable = FileUtil.readObjectFromFile(new Table(), refTableName + ".bin");
							List<String> refColumnList = refTable.getPrimaryKey();
							String refColumn = null;
							if (refColumnList == null)
								throw new DisableForeignKeyGenerateException();
							else if(refColumnList.size() >=2) {
								throw new TooManyPrimarykeyException();
							}
							else {
								refColumn = refColumnList.get(0);
							}
							String deleteRule = "RESTRICT";
							String updateRule = "RESTRICT";
							i++;
							// 아래 코드 중복된거 간결화 예정
							while (i < item.length-1) {
								if ((item[i] + " " + item[i + 1]).equalsIgnoreCase("ON DELETE")) {
									i += 2;
									if (item[i].equalsIgnoreCase("CASCADE")) {
										deleteRule = "CASCADE";
										i++;
									} else if ((item[i] + " " + item[i + 1]).equalsIgnoreCase("SET NULL")) {
										deleteRule = "SET NULL";
										i += 2;
									} else
										throw new InvalidSyntaxException();
								} else if (i <= item.length
										&& (item[i] + " " + item[i + 1]).equalsIgnoreCase("ON UPDATE")) {
									i += 2;
									if (item[i].equalsIgnoreCase("CASCADE")) {
										updateRule = "CASCADE";
										i++;
									} else if ((item[i] + " " + item[i + 1]).equalsIgnoreCase("SET NULL")) {
										updateRule = "SET NULL";
										i += 2;
									} else
										throw new InvalidSyntaxException();
								} else
									break;
							}

							Pair<String, String> deRefTableContent = new Pair<String, String>(table.getTableName(),
									field);

							List<Integer> refTablePKeyIdx = refTable.getPrimaryKeyIdx();
							int refTablePKeyCount = refTablePKeyIdx.size();
							if(refTablePKeyCount > 1) throw new TooManyPrimarykeyException();
							else if(refTablePKeyCount == 0) throw new NotExistPrimaryKeyException();
							else { // 기본키가 하나인 경우 그 기본키를 참조하게(일단은 이렇게 구현함)
								int deRefInfoAddAttrIdx =  refTablePKeyIdx.get(0);
								List<Attribute> attrs = refTable.getAttribute();
								Attribute attr = attrs.get(deRefInfoAddAttrIdx);
								attr.addDeRefInfos(deRefTableContent);
							}
							FileUtil.writeObjectToFile(refTable, refTableName + ".bin");
							infoForeignKey = new ForeignKey(refTableName, refColumn, deleteRule, updateRule);
						}
						break;
					default:
						throw new InvalidSyntaxException();
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		attribute.setField(field);
		attribute.setType(type);
		attribute.setAllowNull(allowNull);
		attribute.setInfoForeignKey(infoForeignKey);

		table.insertAttribute(attribute);

	}
	private static void updateAlterRenameToTable(Table table, String oldName, String newName) {
		List<Attribute> attributes = table.getAttribute();
		for(Attribute attr: attributes) {
			for(Pair<String, String> deRef : attr.getDeRefsInfo()) {
				String deRefTableName = deRef.first;
				Table deRefTable = FileUtil.readObjectFromFile(new Table(), deRefTableName + ".bin");
				// DeRef의 칼럼명 기반으로 순회
				List<Attribute> deRefAttrs = deRefTable.getAttribute();
				for(Attribute deRefAttr: deRefAttrs) {
					ForeignKey deRefTableForeignKey = deRefAttr.getInfoForeignKey();
					if(deRefTableForeignKey.getRefColumn().equalsIgnoreCase(oldName))
						deRefTableForeignKey.setRefColumn(newName);
				}
			}
		}
	}

	private static void createIndexLogic() {

	}
}