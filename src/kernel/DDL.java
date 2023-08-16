package kernel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import dataStructure.ForeignKey;
import dataStructure.Table;
import dataStructure.Tuple;
import exception.FileAlreadyExistenceException;
import exception.NotSupportedTypeException;

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
			try(ObjectOutputStream oo = new ObjectOutputStream(
					new FileOutputStream(objectName+".bin"))) {
				oo.writeObject(tableInfo);
			}
			catch(IOException e) { e.printStackTrace(); }
			break;
		case "INDEX":
			createIndexLogic();
			break;
		default:
			throw new NotSupportedTypeException();
		}
	}

	public void alterCommand(String cmd) {

	}

	public void dropCommand(String cmd) {

	}

	private Table createTableLogic(String cmd) {
		Table table = new Table();
		String [] columns = cmd.split(",");
		for(String column:columns) {
			String [] item = column.split("\\s");
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
				type = item[1].substring(openParenPosition);
				typeSize = Integer.parseInt(item[1].substring(openParenPosition+1, closeParenPosition));
			}
			for(int i=2; i<item.length; i++) { // not null, pk, fk등
				if(i < item.length-1) {
					String command = item[i] + " " + item[i+1].toUpperCase();
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
					case ");":
					};
				}
				table.insertTuple(new Tuple(field, type, typeSize, allowNull, infoForeignKey));
			}
		}
		return table;
	}

	private void createIndexLogic() {

	}
}
