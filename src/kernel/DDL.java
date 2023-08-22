package kernel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import dataStructure.Attribute;
import dataStructure.ForeignKey;
import dataStructure.Table;
import dataStructure.Type;
import exception.DeRefAlreadExistenceException;
import exception.DisableForeignKeyGenerateException;
import exception.FileAlreadyExistenceException;
import exception.InvalidSyntaxException;
import exception.NotSupportedTypeException;
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

	private static void alterRenameToLogic(Table table, String cmd, String fileName) {
		String[] cmdParse = cmd.trim().split("\\s+");
		String newName = cmdParse[2];
		try {
			Path oldFile = Paths.get(fileName);
			Path newFile = Paths.get(newName + ".bin");
			Files.move(oldFile, newFile);
			if (table.getPrimaryKey().size() >= 1) {
				for (String deRef : table.getDeRefTables()) {
					Table deRefTable = FileUtil.readObjectFromFile(new Table(), deRef + ".bin");
					for (String deRefPrimary : deRefTable.getPrimaryKey()) {
						if (deRefPrimary.equals(table.getTableName()))
							;
						deRefPrimary = newName;
					}
				}
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
		int typeSize = 0;
		boolean allowNull = true;
		ForeignKey infoForeignKey = null;
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
						if (table.getPrimaryKey().size() >= 1 && table.getDeRefTables().size() >= 1)
							throw new DeRefAlreadExistenceException();
						table.addPrimaryKey(field);
						i++;
						break;
					case "FOREIGN KEY":
						i += 2;
						if (item[i].equalsIgnoreCase("REFERENCES")) {
							i++;
							String refTableName = item[i];
							Table refTable = FileUtil.readObjectFromFile(new Table(), refTableName + ".bin");
							FileUtil.writeObjectToFile(refTable, refTableName + ".bin");
							List<String> refColumn = refTable.getPrimaryKey();
							if (refColumn == null)
								throw new DisableForeignKeyGenerateException();
							String deleteRule = "RESTRICT";
							String updateRule = "RESTRICT";
							i++;
							//아래 코드 중복 처리 예정
							if (i <= item.length && (item[i] + " " + item[i + 1]).equalsIgnoreCase("ON DELETE")) {
								i += 2;
								if (item[i].equalsIgnoreCase("CASCADE")) {
									deleteRule = "CASCADE";
									i++;
								} else if ((item[i] + " " + item[i + 1]).equalsIgnoreCase("SET NULL")) {
									deleteRule = "SET NULL";
									i += 2;
								} else
									throw new InvalidSyntaxException();
							}
							else if (i <= item.length && (item[i] + " " + item[i + 1]).equalsIgnoreCase("ON UPDATE")) {
								i += 2;
								if (item[i].equalsIgnoreCase("CASCADE")) {
									updateRule = "CASCADE";
									i++;
								} else if ((item[i] + " " + item[i + 1]).equalsIgnoreCase("SET NULL")) {
									updateRule = "SET NULL";
									i += 2;
								} else
									throw new InvalidSyntaxException();
							}
						}
						break;
					default:
						System.out.println(command);
						throw new InvalidSyntaxException();
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		table.insertAttribute(new Attribute(field, type, allowNull, infoForeignKey));
	}

	private static void createIndexLogic() {

	}
}