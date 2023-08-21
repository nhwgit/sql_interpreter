package shell;

import exception.InvalidSyntaxException;
import kernel.DCL;
import kernel.DDL;
import kernel.DML;

public class Handler {
	String sql;

	public Handler(String sql) {
		this.sql = sql;
	}

	public void interpreter() {
		int index = sql.indexOf(" ");
		String command = sql.substring(0, index).toUpperCase();
		switch(command) {
			case "CREATE": DDL.createCommand(sql); break;
			case "ALTER": DDL.alterCommand(sql); break;
			case "DROP": DDL.dropCommand(sql); break;
			case "INSERT": DML.insertCommand(sql); break;
			case "UPDATE": DML.updateCommand(sql); break;
			case "DELETE": DML.deleteCommand(sql); break;
			case "SELECT": DCL.dclCommand(sql); break;
			default : throw new InvalidSyntaxException();
		}
	}
}
