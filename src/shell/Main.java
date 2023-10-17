package shell;

public class Main {
	public static void main(String [] args) {
		/*String totalSql = "create table City( " +
				"id number primary key, " +
				"name varchar(15) not null " +
				");";*/

		/*String totalSql = "create table NewBook( " +
				"id number primary key, " +
				"name varchar(15) not null, " +
				"cid number foreign key references City on delete set null " +
				"on update cascade " +
				");";*/

		/*String totalSql = "ALTER TABLE City " +
						"ADD COLUMN test INTEGER";*/

		/*String totalSql = "ALTER TABLE City " +
						  "RENAME COLUMN test2 TO test3";*/

		/*String totalSql = "INSERT INTO City " +
				"VALUES (4, '서울') " +
				"VALUES (5, '부산') " +
				"VALUES (6, '인천');";*/

		/*String totalSql = "INSERT INTO NewBook" +
				"VALUES (1, '책1', 6) " +
				"VALUES (2, '책2', 1);";*/

		/*String totalSql = "ALTER TABLE City " +
		"DROP COLUMN test";*/

		/*String totalSql = "UPDATE City "+
						"SET id = 7 "+
						"WHERE name = '인천'";*/

		/*String totalSql = "DELETE City"+
						"WHERE name = '인천'";*/

		/*String totalSql = "SELECT id, name FROM City, newBook "
						+ "WHERE id >= 4 and id < 8 "
						+ "and id < 8 ORDER BY name DESC";*/

		//String totalSql = "SELECT id, name FROM city WHERE name LIKE 부산";

		//String totalSql = "SELECT id, name FROM City, NewBook;";

		String totalSql = "SELECT id, name FROM City, NewBook " +
						"WHERE City.id = NewBook.cid and City.id >= 7 " +
						"ORDER BY City.name";

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
