package shell;

import exception.InvalidSyntaxException;
import kernel.ddl.DDL;
import kernel.dml.DataSearching;
import kernel.dml.Delete;
import kernel.dml.Insert;
import kernel.dml.Update;

public class Handler {
	String sql;

	public Handler(String sql) {
		this.sql = sql;
	}

	public void interpreter() {
		int index = sql.indexOf(" ");
		String command = sql.substring(0, index).toUpperCase();
		switch(command) {
			case "CREATE":{
				DDL ddl = new DDL();
				ddl.createCommand(sql); break;
			}
			case "ALTER": {
				DDL ddl = new DDL();
				ddl.alterCommand(sql); break;
			}
			case "DROP": {
				DDL ddl = new DDL();
				ddl.dropCommand(sql); break;
			}
			case "INSERT": {
				Insert insert = new Insert();
				insert.parsingAndInit(sql);
				insert.insertCommand(command);
				break;
			}
			case "UPDATE": {
				Update update = new Update();
				update.parsingAndInit(sql);
				update.updateCommand();
				break;
			}

			case "DELETE": {
				Delete drop = new Delete();
				drop.parsingAndInit(sql);
				drop.deleteCommand(); break;
			}
			case "SELECT": {
				DataSearching query = new DataSearching(sql);
				query.queryParsing();
				query.execute();
				break;
			}
			default : throw new InvalidSyntaxException();
		}
	}
}
