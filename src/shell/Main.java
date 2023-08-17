package shell;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import dataStructure.Table;

public class Main {
	public static void main(String [] args) {
		String totalSql = "CREATE TABLE NewBook(\r\n"
				+ "	id NUMBER PRIMARY KEY,\r\n"
				+ "	name varchar(15) NOT NULL\r\n"
				+ ");";
		String [] partialSql = totalSql.split(";");
		for(String command:partialSql) {
			command = command.trim();
			Handler handler = new Handler(command);
			handler.interpreter();
		}
		try(ObjectInputStream oi =
			new ObjectInputStream(new FileInputStream("NewBook.bin"))) {
			Table table = (Table)oi.readObject();
			table.printTableInfo();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
