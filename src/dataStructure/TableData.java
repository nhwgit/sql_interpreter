package dataStructure;

import java.util.List;

public class TableData {
	List<String> attributes;
	List<List<String>> data;

	public TableData(List<String> attributes, List<List<String>> data) {
		this.attributes = attributes;
		this.data = data;
	}

	public void printTable() {
		for(String attr: attributes) {
			System.out.println(attr+ '\t');
		}
		for(List<String> column: data) {
			for(String item : column) {
				System.out.println(item+ '\t');
			}
			System.out.println('\n');
		}
	}
}
