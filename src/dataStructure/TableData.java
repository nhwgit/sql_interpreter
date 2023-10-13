package dataStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dataStructure.table.Attribute;
import dataStructure.table.Table;
import exception.InsertTupleException;
import exception.InvalidSyntaxException;
import util.FileUtil;

public class TableData {
	private List<List<String>> data = new ArrayList<>();
	private int endIdx; // 집계함수 제외 끝 idx
	private List<AttributeInfo> attrInfos = new ArrayList<>();

	public TableData(String tableName) {
		Table metaData = FileUtil.readObjectFromFile(new Table(), tableName+".bin");
		List<Attribute> attrs = metaData.getAttribute();
		Iterator<Attribute> itr = attrs.iterator();
		while(itr.hasNext()) {
			Attribute attr = itr.next();
			String attrName = attr.getField();
			String typeName = attr.getType().getTypeName();
			AttributeInfo attrInfo = new AttributeInfo(tableName, attrName, typeName);
			attrInfos.add(attrInfo);
		}
	}

	private List<AttributeInfo> getAttributes() {
		return attrInfos;
	}

	private List<List<String>> getData() {
		return data;
	}

	public void insertTuple(String[] tuple) {
		if (attrInfos.size() != tuple.length)
			throw new InsertTupleException();
		List<String> partData = new LinkedList<>();
		for (String item : tuple)
			partData.add(item);
		data.add(partData);
	}

	public void mergeTable(TableData newTable) {
		List<AttributeInfo> mergeAttr = newTable.getAttributes();
		List<List<String>> mergeData = newTable.getData();

		attrInfos.addAll(mergeAttr);

		// 카티션 프러덕트
		List<List<String>> newData = new LinkedList<>();
		for (List<String> tuple : data) {
			for (List<String> mergeTuple : mergeData) {
				List<String> newTuple = new LinkedList<>();
				newTuple.addAll(tuple);
				newTuple.addAll(mergeTuple);
				newData.add(newTuple);
			}
		}
		data = newData;
		endIdx = attrInfos.size();
	}

	public void extractAttributes(String[] list) {
		// 추출할 idx 구한다.
		List<Integer> extractIdx = findExtractIdx(list);
		// 추출
		attrExtractor(extractIdx);
	}

	public void extractTuples(String condition) {
		String[] condParse = condition.split("\\s+");
		String columnName = condParse[0];
		String operator = condParse[1];
		String criteria = condParse[2];
		int columnIdx = findExtractIdx(columnName);

		Iterator<List<String>> itr = data.iterator();
		// LIKE => 문자 비교
		if (operator.equals("LIKE")) {
			while (itr.hasNext()) {
				List<String> tuple = itr.next();
				String evlautedData = tuple.get(columnIdx);
				if (!evlautedData.equals(criteria)) {
					itr.remove();
				}
			}
		}
		// 비교 연산자 => 숫자 비교
		else if (isComparisonOperator(operator)) {
			while (itr.hasNext()) {
				List<String> tuple = itr.next();
				String evlautedData = tuple.get(columnIdx);
				if (isRemoveData(evlautedData, operator, criteria))
					itr.remove();
			}
		} else {
			throw new InvalidSyntaxException();
		}
	}

	public void sortTuple(String columnName, boolean isASC) {
		int columnIdx = findExtractIdx(columnName);
		String type = attrInfos.get(columnIdx).getTypeName();
		Collections.sort(data, new Comparator<List<String>>() {
			@Override
			public int compare(List<String> s1, List<String> s2) {
				String compareItem1 = s1.get(columnIdx);
				String compareItem2 = s2.get(columnIdx);

				if(type.equals("INTEGER")) {
					if(isASC)
						return Integer.parseInt(compareItem1) - Integer.parseInt(compareItem2);
					else
						return Integer.parseInt(compareItem2) - Integer.parseInt(compareItem1);
				}
				else {
					if(isASC)
						return compareItem1.compareTo(compareItem2);
					else
						return compareItem2.compareTo(compareItem1);
				}

			}
		});
	}

	private boolean isRemoveData(String data, String operator, String criteria) {
		try {
			int nData = Integer.parseInt(data);
			int nCriteria = Integer.parseInt(criteria);

			switch (operator) {
			case "<":
				if (nData < nCriteria)
					return false;
				else
					return true;
			case "<=":
				if (nData <= nCriteria)
					return false;
				else
					return true;
			case ">":
				if (nData > nCriteria)
					return false;
				else
					return true;
			case ">=":
				if (nData >= nCriteria)
					return false;
				else
					return true;
			case "=":
				if (nData == nCriteria)
					return false;
				else
					return true;
			default:
				return false;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean isComparisonOperator(String criteria) {
		List<String> comparator = List.of("<", "<=", ">", ">=", "=");
		if (comparator.contains(criteria))
			return true;
		else
			return false;

	}

	private void attrExtractor(List<Integer> extractIdx) {
		int maxIdx = attrInfos.size() - 1;
		List<Integer> removeIdx = IntStream.rangeClosed(0, maxIdx).boxed().sorted(Collections.reverseOrder()) // 역순으로 정렬
				.collect(Collectors.toList());

		removeIdx.removeAll(extractIdx); // extractIdx가 아닌 원소들을 제거하려고.
		for (int i = 0; i < removeIdx.size(); i++) {
			int rIdx = removeIdx.get(i);
			attrInfos.remove(rIdx);
			for (List<String> tuple : data) {
				tuple.remove(rIdx);
			}
		}
	}

	private int findExtractIdx(String name) {
		for (int i = 0; i < attrInfos.size(); i++) {
			String columnAlias = attrInfos.get(i).getTableName();
			String columnName = attrInfos.get(i).getAttrName();
			String columnName2 = columnAlias + "." + columnName;
			if (columnName.equals(name) || columnName2.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	private List<Integer> findExtractIdx(String[] list) {
		List<Integer> extractIdx = new LinkedList<>();
		for (String item : list) {
			if (item.equals("*")) { // *은 집계함수 제외한 모든 칼럼 가져온다.
				for (int i = 0; i < endIdx; i++)
					extractIdx.add(i);
			} else { // attributeInfo (테이블 혹은 별칭, 이름)
				for (int i = 0; i < attrInfos.size(); i++) {
					String columnAlias = attrInfos.get(i).getTableName();
					String columnName = attrInfos.get(i).getAttrName();
					String columnName2 = columnAlias + "." + columnName;
					if (columnName.equals(item) || columnName2.equals(item)) {
						extractIdx.add(i);
					}
				}
			}
		}
		List<Integer> retList = extractIdx.stream().distinct().collect(Collectors.toList());

		return retList;
	}

	public void printTableData() {
		for (AttributeInfo attr : attrInfos) {
			System.out.print(attr.getAttrName() + '\t');
		}
		System.out.println();
		for (List<String> column : data) {
			for (String item : column) {
				System.out.print(item + '\t');
			}
			System.out.println();
		}
	}
}
