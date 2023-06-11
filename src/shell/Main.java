package shell;

public class Main {
	public static void main(String [] args) {
		String totalSql = "CREATE TABLE NewBook; ALTER TABLE Book;";
		String [] partialSql = totalSql.split(";");
		for(String command:partialSql) {
			command = command.trim();
			Handler handler = new Handler(command);
			handler.interpreter();
		}
	}
}
