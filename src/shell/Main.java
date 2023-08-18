package shell;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import dataStructure.Table;

public class Main {
	public static void main(String [] args) {
		/*String totalSql = "create table City(\r\n" +
				"id number primary key,\r\n" +
				"name varchar(15) not null\r\n" +
				");";*/

		String totalSql = "create table NewBook(\r\n" +
				"id number primary key,\r\n" +
				"name varchar(15) not null,\r\n" +
				"cid number foreign key references City on delete set null\r\n" +
				");";

		/*String totalSql = "ALTER TABLE NewBook\r\n" +
						"RENAME TO NewBook2";*/
		String [] partialSql = totalSql.split(";");
		for(String command:partialSql) {
			command = command.trim();
			Handler handler = new Handler(command);
			handler.interpreter();
		}

		//테스트용
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
