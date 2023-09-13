package shell;

import exception.InvalidSyntaxException;
import kernel.DCL;
import kernel.ddl.DDL;
import kernel.dml.DataSetting;

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
			case "INSERT": DataSetting.insertCommand(sql); break;
			case "UPDATE": DataSetting.updateCommand(sql); break;
			case "DELETE": DataSetting.deleteCommand(sql); break;
			case "SELECT": DCL.dclCommand(sql); break;
			default : throw new InvalidSyntaxException();
		}
	}
}
