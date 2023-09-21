package kernel.dml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
		Pattern selectPattern = Pattern.compile("SELECT\\s+(.*)\\s+FROM");
		Pattern fromPattern = Pattern.compile("FROM\\s+(.*)");
		Pattern wherePattern = Pattern.compile("WHERE\\s+(\\S+)");
		Pattern groupByPattern = Pattern.compile("GROUP BY\\s+(\\S+)");
		Pattern havingPattern = Pattern.compile("HAVING\\s+(\\S+)");
		Pattern orderByPattern = Pattern.compile("ORDER BY\\s+(\\S+)");
		//정규표현식을 이렇게 짜는 것이 최선일까?

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

	public void execute() {
		TableData tableData = fromStatementProcessing();
		selectStatementProcessing(tableData);
		tableData.printTable();
	}

	public TableData fromStatementProcessing() {
		String [] parseFromStatement = fromStatement.split(", ");
		TableData retTable = null;

		for(String tableName: parseFromStatement) {
			TableData table = new TableData();
			try (BufferedReader br = new BufferedReader(new FileReader(tableName+ ".txt"))) {
				String header = br.readLine();
				String [] attrs = header.split("\\s+");
				table.setAttributes(tableName, attrs);
				String tuple;
				while(true) {
					tuple = br.readLine();
					if(tuple==null) break;
					String [] parsedTuple = tuple.split("\\s+");
					table.insertTuple(parsedTuple);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			if(retTable==null) retTable = table;
			else retTable.mergeTable(table);
		}
		return retTable;
	}

	public void selectStatementProcessing(TableData tableData) {
		String [] parseSelectStatement = selectStatement.split(", ");
		System.out.println(parseSelectStatement[0]);
		tableData.extractAttributes(parseSelectStatement);
	}

	public void whereStatementProcessing(TableData tableData) {
	}
}
