package kernel.dml;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataStructure.TableData;
import exception.InvalidSyntaxException;

public class DataSearching {
	private String query;
	private String selectStatement = null;
	private String fromStatement = null;
	private String whereStatement = null;
	private String groupByStatement = null;
	private String havingStatement = null;
	private String orderByStatement = null;
	//부속질의 구현 안함

	public DataSearching(String query) {
		this.query = query;
	}

	public void queryParsing() {
		Pattern selectPattern = Pattern.compile("SELECT\\s+(.*)\\s+FROM.*");
		Pattern fromPattern = Pattern.compile("FROM\\s+(.*)\\s+(WHERE.*)|(GROUP BY.*)|(ORDER BY.*)|$"); // order by를 추가하면 오류.. 왜?
		Pattern wherePattern = Pattern.compile("WHERE\\s+(.*)\\s+(GROUP BY.*)|(ORDER BY.*)|$");
		Pattern groupByPattern = Pattern.compile("GROUP BY\\s+(.*)\\s+(HAVING.*)|(ORDER BY.*)|$");
		Pattern havingPattern = Pattern.compile("HAVING\\s+(.*)\\s+?:(ORDER BY.*)|$");
		Pattern orderByPattern = Pattern.compile("ORDER BY\\s+(.*)");

		Matcher matcher = selectPattern.matcher(query);
		if(matcher.find()) selectStatement = matcher.group(1);
		else throw new InvalidSyntaxException();

		matcher = fromPattern.matcher(query);
		if(matcher.find()) fromStatement = matcher.group(1);
		else throw new InvalidSyntaxException();

		matcher = wherePattern.matcher(query);
		if(matcher.find()) whereStatement = matcher.group(1);

		matcher = groupByPattern.matcher(query);
		if(matcher.find()) groupByStatement = matcher.group(1);

		matcher = havingPattern.matcher(query);
		if(matcher.find()) havingStatement = matcher.group(1);

		matcher = orderByPattern.matcher(query);
		if(matcher.find()) orderByStatement = matcher.group(1);
	}

	public TableData selectStatementProcessing() {

	}

	public void execute() {
		TableData tabledata;
		tabledata = fromStatementProcessing();
		tabledata = selectStatementProcessing();
		tabledata.printTable();
	}

	public TableData fromStatementProcessing() {
		String [] parseFromStatement = fromStatement.split(", ");
	}

	public void printTable() {
		for(String attr: attributes) {
			System.out.println(attr + '\t');
		}
		for(List<String> column: table) {
			for(String item: column) {
				System.out.println(item + '\t');
			}
			System.out.println('\n');
		}
	}

}
