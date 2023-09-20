package dataStructure;

import java.util.LinkedList;
import java.util.List;

import exception.InsertTupleException;

public class TableData {
	private List<Pair<String, String>> attributeInfo = new LinkedList<>();// (테이플, 이름)
	private List<List<String>> data = new LinkedList<>();


	public void setAttributes(String tableName, String [] attrs) {
		for(String attr: attrs) {
			Pair<String, String> pair = new Pair<>(tableName, attr);
			attributeInfo.add(pair);
		}
	}

	private List<Pair<String, String>> getAttributes() {
		return attributeInfo;
	}

	private List<List<String>> getData() {
		return data;
	}

	public void insertTuple(String [] tuple) {
		if(attributeInfo.size() != tuple.length)
			throw new InsertTupleException();
		List<String> partData = new LinkedList<>();
		for(String item: tuple)
			partData.add(item);
		data.add(partData);
	}

	public void mergeTable(TableData table) {
		List<Pair<String, String>> mergeAttr = table.getAttributes();
		List<List<String>> mergeData = table.getData();

		for(int i=0; i<mergeAttr.size(); i++) {
			attributeInfo.add(mergeAttr.get(i));
		}

		//카티션 프러덕트
		List<List<String>> newData = new LinkedList<>();
		for(List<String> tuple: data) {
			for(List<String> mergeTuple: mergeData) {
				List<String> newTuple = new LinkedList<>();
				newTuple.addAll(tuple);
				newTuple.addAll(mergeTuple);
				newData.add(newTuple);
			}
		}
		data=newData;
	}

	public void printTable() {
		for(Pair<String, String> attr: attributeInfo) {
			System.out.print(attr.second+ '\t');
		}
		System.out.println();
		for(List<String> column: data) {
			for(String item : column) {
				System.out.print(item+ '\t');
			}
			System.out.println();
		}
	}
}
