package shell;

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
			case "CREATE": new DDL().createCommand(sql); break;
			case "ALTER": new DDL().alterCommand(sql); break;
			case "DEOP": new DDL().dropCommand(sql); break;
			case "INSERT": new DML().insertCommand(sql); break;
			case "UPDATE": new DML().updateCommand(sql); break;
			case "DELETE": new DML().deleteCommand(sql); break;
			case "SELECT": new DCL().dclCommand(sql); break;
			default : break;
		}
	}
}
