package kernel;
import java.io.File;

public class DDL {
	public void createCommand(String cmd) {
		int headerSize = cmd.indexOf("(");
		String [] header = cmd.substring(0, headerSize).split(" ");
		String type = header[1].toUpperCase();
		String objectName = header[2];
		File table = new File(objectName+".txt");
		if(table.exists()) throw new FileAlreadyExistenceException();
		switch(type) {
		case "TABLE":
			createTableLogic(cmd.substring(headerSize));
			break;
		case "INDEX":
			createIndexLogic();
			break;
		default:
			throw new NotSupportedTypeException();
		}


	}

	public void alterCommand(String cmd) {

	}

	public void dropCommand(String cmd) {

	}

	private void createTableLogic(String cmd) {

	}

	private void createIndexLogic() {

	}
}
