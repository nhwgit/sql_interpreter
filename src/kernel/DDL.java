package kernel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import dataStructure.ForeignKey;
import dataStructure.Table;
import dataStructure.Tuple;
import dataStructure.Type;
import exception.DisableForeignKeyGenerateException;
import exception.FileAlreadyExistenceException;
import exception.InvalidSyntaxException;
import exception.NotSupportedTypeException;
import exception.WrongColumnNameException;
import util.FileUtil;
import util.KernelUtil;

public class DDL {
	public void createCommand(String cmd) {
		int headerSize = cmd.indexOf("(");
		String [] header = cmd.substring(0, headerSize).split(" ");
		String type = header[1].toUpperCase();
		String objectName = header[2];
		File table = new File(objectName+".bin");
		if(table.exists()) throw new FileAlreadyExistenceException();
		switch(type) {
		case "TABLE":
			Table tableInfo = createTableLogic(cmd.substring(headerSize+1));
			FileUtil.writeObjectToFile(tableInfo, objectName+".bin");
			break;
		case "INDEX":
			createIndexLogic();
			break;
		default:
			throw new NotSupportedTypeException();
		}
	}

	public void alterCommand(String cmd) {
		String [] item = cmd.trim().split("\n|\r\n");
		String header = item[0];
		String action = item[1];

		String [] headerParse = header.trim().split("\\s+");

		String type = headerParse[1];
		String objectName = headerParse[2];

		switch(type) {
		case "TABLE":
			Table tableInfo = alterTableLogic(action, objectName+".bin");
			FileUtil.writeObjectToFile(tableInfo, objectName+".bin");
			break;
		case "INDEX":
			break;
		}
	}

	public void dropCommand(String cmd) {
		String [] item = cmd.trim().split("\\s+");
		String type = item[1];
		String FileName = item[2];
		System.out.println(FileName);
		Path filePath = Paths.get(FileName+".bin");
		switch(type) {
			case "TABLE":
				try {
					Files.delete(filePath);
				} catch(NoSuchFileException e) {
					System.out.println("삭제하려는 파일이 없습니다.");
				} catch(IOException e) {
					e.printStackTrace();
				}
			case "INDEX":
			default:
				throw new NotSupportedTypeException();
		}
	}

	private Table createTableLogic(String cmd) {
		Table table = new Table();
		String [] columns = cmd.split(",");
		for(String column:columns)
				newColumnRegisterLogic(table, column);
		return table;
	}

	private Table alterTableLogic(String cmd, String fileName) {
		int index = cmd.indexOf(" ");
		String type = cmd.substring(0, index).toUpperCase();
		Table table = FileUtil.readObjectFromFile(new Table(), fileName);
		switch(type) {
		case "ADD":
			alterAddLogic(table, cmd);
			break;
		case "RENAME":
			String judge = cmd.substring(index+1, index+3);
			if(judge.equalsIgnoreCase("TO")) alterRenameToLogic(table, cmd, fileName);
			else alterRenameLogic(table, cmd);;
			break;
		case "MODIFY":
			alterModifyLogic(table, cmd);
			break;
		case "DROP":
			alterDropLogic(table, cmd);
			break;
		}
		return table;
	}

	private Table alterAddLogic(Table table, String cmd) {
		String [] cmdParse = cmd.trim().split("\\s+");
		StringBuilder tmp = new StringBuilder();
		for(int i=2; i<cmdParse.length; i++)
			tmp.append(cmdParse[i] + ' ');
		String registerCmd = (tmp.toString()).trim();
		newColumnRegisterLogic(table, registerCmd);
		return table;
	}

	private void alterRenameLogic(Table table, String cmd) {
		String [] cmdParse = cmd.trim().split("\\s+");
		String existName = cmdParse[2];
		String newName = cmdParse[4];;
		List<Tuple> tableInfo = table.getTuple();
		for(Tuple t : tableInfo) {
			if(t.getField().equals(existName))
				t.setField(newName);
		}
	}

	private void alterModifyLogic(Table table, String cmd) {
		String [] cmdParse = cmd.trim().split("\\s+");
		String columnName = cmdParse[2];
		String newType = cmdParse[3];
		Type type = null;
		List<Tuple> tableInfo = table.getTuple();
		for(Tuple t : tableInfo) {
			if(t.getField().equals(columnName)) {
				type = KernelUtil.typeGenerator(newType);
				t.setType(type);
			}
		}
		if(type == null) throw new WrongColumnNameException();
	}

	private void alterDropLogic(Table table, String cmd) {
		String [] cmdParse = cmd.trim().split("\\s+");
		String columnName = cmdParse[2];
		List<Tuple> tableInfo = table.getTuple();
		Iterator<Tuple> iterator = tableInfo.iterator();
		while(iterator.hasNext()) {
			Tuple element = iterator.next();
			if(element.getField().equals(columnName))
				iterator.remove();
		}
	}

	private void alterRenameToLogic(Table table, String cmd, String fileName) {
		String [] cmdParse = cmd.trim().split("\\s+");
		String newName = cmdParse[2];
		try {
			Path oldFile = Paths.get(fileName);
			Path newFile = Paths.get(newName+".bin");
			Files.move(oldFile, newFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void newColumnRegisterLogic(Table table, String cmd) {
		String [] item = cmd.trim().split("\\s+|\\);");
		String field = null;
		Type type = null;
		int typeSize = 0;
		boolean allowNull = true;
		ForeignKey infoForeignKey = null;
		try {
			field = item[0];
			type = KernelUtil.typeGenerator(item[1]);

			for(int i=2; i<item.length; i++) { // not null, pk, fk등
				if(i < item.length-1) {
					String command = (item[i] + " " + item[i+1]).toUpperCase().trim();
					switch(command) {
					case "NOT NULL":
						allowNull = false; i++;
						break;
					case "PRIMARY KEY":
						table.setPrimaryKey(field); i++;
						break;
					case "FOREIGN KEY":
						i+=2;
						if(item[i].equalsIgnoreCase("REFERENCES")); {
							i++;
							String refTableName = item[i];
							Table refTable = FileUtil.readObjectFromFile(new Table(), refTableName+".bin");
							String refColumn = refTable.getPrimaryKey();
							if(refColumn==null) throw new DisableForeignKeyGenerateException();
							String deleteRule = "SET NULL";
							i++;
							if(i<=item.length && (item[i]+" "+ item[i+1]).equalsIgnoreCase("ON DELETE")) {
								i+=2;
								if(item[i].equalsIgnoreCase("CASCADE")) {
									deleteRule = "CASCADE"; i++;
								}
								else if((item[i] + " "+ item[i+1]).equalsIgnoreCase("SET NULL")) {
									deleteRule = "SET NULL";
									i+=2;
								}
								else throw new InvalidSyntaxException();
							}
							infoForeignKey = new ForeignKey(refTableName, refColumn, deleteRule);
						}
						break;
					default:
						System.out.println(command);
						throw new InvalidSyntaxException();
					};
				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		table.insertTuple(new Tuple(field, type, allowNull, infoForeignKey));
	}

	private void createIndexLogic() {

	}
}