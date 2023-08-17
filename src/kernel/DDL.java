package kernel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import dataStructure.ForeignKey;
import dataStructure.Table;
import dataStructure.Tuple;
import exception.FileAlreadyExistenceException;
import exception.InvalidSyntaxException;
import exception.NotSupportedTypeException;
import util.file;

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
			file.writeObjectToFile(tableInfo, objectName+"bin");
			break;
		case "INDEX":
			createIndexLogic();
			break;
		default:
			throw new NotSupportedTypeException();
		}
	}

	public void alterCommand(String cmd) {
		String [] item = cmd.trim().split("\n | \r\n");
		String header = item[0];
		String action = item[1];

		String [] headerParse = header.trim().split("\\s+");

		String type = headerParse[1];
		String fileName = headerParse[2];

		switch(type) {
		case "TABLE":
			alterTableLogic(action, fileName+".bin");
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
		try {
			for(String column:columns) {
				System.out.println(column);
				String [] item = column.trim().split("\\s+");
				String field = null;
				String type = null;
				int typeSize = 0;
				boolean allowNull = true;
				ForeignKey infoForeignKey = null;
				field = item[0];

				int openParenPosition = item[1].indexOf("(");
				int closeParenPosition = item[1].indexOf(")");
				if(openParenPosition == -1 || closeParenPosition == -1) {
					type = item[1];
					typeSize = 20;
				}
				else {
					type = item[1].substring(0, openParenPosition);
					typeSize = Integer.parseInt(item[1].substring(openParenPosition+1, closeParenPosition));
				}
				for(int i=2; i<item.length; i++) { // not null, pk, fk등
					if(i < item.length-1) {
						String command = item[i] + " " + item[i+1].toUpperCase().trim();
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
								int openParenPosition2 = item[i].indexOf("(");
								int closeParenPosition2 = item[i].indexOf(")");
								String refTable = item[i].substring(openParenPosition2);
								String refColumn = item[i].substring(openParenPosition2+1, closeParenPosition2);
								String deleteRule = "SET NULL";
								i++;
								if(i<=item.length && (item[i]+" "+ item[i+1]).equalsIgnoreCase("ON DELETE")) {
									i+=2;
									if(item[i].equalsIgnoreCase("CASCADE"))
										deleteRule = "CASCADE";
									else if((item[i] + " "+ item[i+1]).equalsIgnoreCase("SET NULL"))
										deleteRule = "SET NULL";
								}
								infoForeignKey = new ForeignKey(refTable, refColumn, deleteRule);
							}
							break;
						case ")": break;
						default: throw new InvalidSyntaxException();

						};
					}
				}
				table.insertTuple(new Tuple(field, type, typeSize, allowNull, infoForeignKey));
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return table;
	}

	private Table alterTableLogic(String cmd, String fileName) {
		int index = cmd.indexOf(" ");
		String type = cmd.substring(0, index).toUpperCase();
		Table table = file.readObjectFromFile(new Table(), fileName);
		switch(type) {
		case "ADD":
			alterAddLogic(table, cmd);
			break;
		case "RENAME": // rename과 renameto 구분하여 작성 필요
			break;
		case "MODIFY":
			alterModifyLogic(table, cmd);
			break;
		case "CHANGE":
			alterChangeLogic(table, cmd);
			break;
		case "DROP":
			alterDropLogic(table, cmd);
			break;
		}
		return table;
	}

	private void alterAddLogic(Table table, String cmd) {
		String [] cmdParse = cmd.trim().split("\\s+");
	}

	private void alterRenameLogic(Table table, String cmd) {
		String [] cmdParse = cmd.trim().split("\\s+");
	}

	private void alterModifyLogic(Table table, String cmd) {
		String [] cmdParse = cmd.trim().split("\\s+");
	}

	private void alterChangeLogic(Table table, String cmd) {
		String [] cmdParse = cmd.trim().split("\\s+");
	}

	private void alterDropLogic(Table table, String cmd) {
		String [] cmdParse = cmd.trim().split("\\s+");
	}

	private void alterRenameToLogic(Table table, String cmd) {
		String [] cmdParse = cmd.trim().split("\\s+");
	}

	private void createIndexLogic() {

	}
}