package shell;

public class Main {
	public static void main(String [] args) {
		/*String totalSql = "create table City(\r\n" +
				"id number primary key,\r\n" +
				"name varchar(15) not null\r\n" +
				");";*/

		/*String totalSql = "create table NewBook(\r\n" +
				"id number primary key,\r\n" +
				"name varchar(15) not null,\r\n" +
				"cid number foreign key references City on delete set null " +
				"on update cascade\r\n" +
				");";*/

		/*String totalSql = "ALTER TABLE City\r\n" +
						"ADD COLUMN test INTEGER PRIMARY KEY";*/

		/*String totalSql = "INSERT INTO City\r\n" +
				"VALUES (4, '서울')\r\n" +
				"VALUES (5, '부산')\r\n+" +
				"VALUES (6, '인천');";*/

		/*String totalSql = "INSERT INTO NewBook\r\n" +
				"VALUES (1, '책1', 6)\r\n" +
				"VALUES (2, '책2', 1);";*/

		String totalSql = "UPDATE City\r\n"+
						"SET id = 7\r\n"+
						"WHERE name = '인천'";

		String [] partialSql = totalSql.split(";");
		for(String command:partialSql) {
			command = command.trim();
			Handler handler = new Handler(command);
			handler.interpreter();
		}

		//테스트용
		/*try(ObjectInputStream oi =
			new ObjectInputStream(new FileInputStream("NewBook.bin"))) {
			Table table = (Table)oi.readObject();
			table.printTableInfo();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
		}*/
	}
}
