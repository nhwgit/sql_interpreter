package kernel.dml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DataSearching {
	private List<String> attribute = new LinkedList<String>();
	private List<List<String>> table = new LinkedList<List<String>>();
	private String query;
	String selectStatement;
	String fromStatement;
	String whereStatement;
	String groupByStatement;
	String havingStatement;
	String orderByStatement;

	public DataSearching(String Query) {
		this.query = query;
	}

	public void QueryProcessing() {
		String [] wholeQuery = query.split("MINUS");
		String presentQuery = wholeQuery[0];
		String [] statement = presentQuery.split("\r\n|\n"); // 공백문자 기준으로 바꾸자.
		for(String line : statement) {
			if		(line.startsWith("SELECT")) selectStatement = line;
			else if (line.startsWith("FROM")) fromStatement = line;
		}
		fromStatementProcessing();
		selectStatementProcessing();
		printTable();
	}

	public void selectStatementProcessing() {

	}

	public void fromStatementProcessing() {
		try (BufferedReader br = new BufferedReader(new FileReader("aaa.txt"))) { // 수정 예정

		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void printTable() {

	}

}
