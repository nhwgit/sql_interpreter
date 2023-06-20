package dataStructure;

public class Table implements java.io.Serializable {
	private String primaryKey = null;
	private String forienKey[] = new String[3]; // 외래키, 참조 테이블, 삭제 규칙

	Table() {
	}
}
