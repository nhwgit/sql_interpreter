package shell;

public class Main {
	public static void main(String [] args) {
		String totalSql = "ALTER TABLE NewBook\r\n" +
						"RENAME TO NewBook2";
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
