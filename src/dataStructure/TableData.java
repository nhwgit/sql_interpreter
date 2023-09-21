package dataStructure;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import exception.InsertTupleException;

public class TableData {
	private List<Pair<String, String>> attributeInfo = new LinkedList<>();// (테이플 혹은 별칭, 이름)
	private List<List<String>> data = new LinkedList<>();
	private int endIdx; // 집계함수 제외 끝 idx


	public void setAttributes(String tableName, String [] attrs) {
		for(String attr: attrs) {
			Pair<String, String> pair = new Pair<>(tableName, attr);
			attributeInfo.add(pair);
		}
		endIdx = attributeInfo.size();
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
		endIdx = attributeInfo.size();
	}

	public void extractAttributes(String [] list) {
		//추출할 idx 구한다.
		List<Integer> extractIdx = findExtractIdx(list);
		//추출
		attrExtractor(extractIdx);
	}

	private void extractTuples(String condition) {
		String [] condParse = condition.split("\\s+");
		String columnName = condParse[0];
		String operator = condParse[1];
		String criteria = condParse[2];
		int columnIdx = findExtractIdx(columnName);
		// table에서 type 가져오기
		// criteria type과 일치하는지 확인
		// = 뒤에 문자열이면 조인, 문자열 비교는 LIKE로. 부등호과 LIKE만 구현 할 것임
		switch(operator) { // 이 함수에서 처리하면 복잡해질 것 같다.
			case ">": {

			}
			case ">=": {

			}
			case "<": {

			}
			case "<=": {

			}
			case "=": {

			}
			case "LIKE": {

			}
		}
	}

	private void attrExtractor(List<Integer> extractIdx) {
		int maxIdx = attributeInfo.size()-1;
		 List<Integer> removeIdx = IntStream.rangeClosed(0, maxIdx)
	                .boxed()
	                .sorted(Collections.reverseOrder()) // 역순으로 정렬
	                .collect(Collectors.toList());

		removeIdx.removeAll(extractIdx); //extractIdx가 아닌 원소들을 제거하려고.
        for(int i=0; i<removeIdx.size(); i++) {
        	int rIdx = removeIdx.get(i);
        	attributeInfo.remove(rIdx);
        	for(List<String> tuple: data) {
        		tuple.remove(rIdx);
        	}
        }
	}

	private int findExtractIdx(String name) {
		for(int i=0; i<attributeInfo.size(); i++) {
			String columnAlias = attributeInfo.get(i).first;
			String columnName = attributeInfo.get(i).second;
			String columnName2 = columnAlias+"."+columnName;
			if(columnName.equals(name) || columnName2.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	private List<Integer> findExtractIdx(String [] list) {
		List<Integer> extractIdx = new LinkedList<>();
		for(String item: list) {
			if(item.equals("*")) { // *은 집계함수 제외한 모든 칼럼 가져온다.
				for(int i=0; i<endIdx; i++)
					extractIdx.add(i);
			}
			else { // attributeInfo (테이블 혹은 별칭, 이름)
				for(int i=0; i<attributeInfo.size(); i++) {
					String columnAlias = attributeInfo.get(i).first;
					String columnName = attributeInfo.get(i).second;
					String columnName2 = columnAlias+"."+columnName;
					if(columnName.equals(item) || columnName2.equals(item)) {
						extractIdx.add(i);
					}
				}
			}
		}
		List<Integer> retList = extractIdx.stream().distinct()
								.collect(Collectors.toList());

		return retList;
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
