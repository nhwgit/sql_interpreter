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
				"cid number foreign key references City on delete set null\r\n" +
				");";*/

		/*String totalSql = "ALTER TABLE City\r\n" +
						"ADD COLUMN test INTEGER PRIMARY KEY";*/

		/*String totalSql = "INSERT INTO City\r\n" +
				"VALUES (4, '서울')\r\n" +
				"VALUES (5, '부산')\r\n+" +
				"VALUES (6, '인천');";*/

		String totalSql = "UPDATE City\r\n"+
						"SET name = '부산'\r\n"+
						"WHERE name = '서울'";

		String [] partialSql = totalSql.split(";");
		for(String command:partialSql) {
			command = command.trim();
			Handler handler = new Handler(command);
			handler.interpreter();
		}

		//테스트용
		/*try(ObjectInputStream oi =
			new ObjectInputStream(new FileInputStream("City.bin"))) {
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
