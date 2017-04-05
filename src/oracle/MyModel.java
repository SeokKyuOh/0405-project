/*
	JTable이 수시로 정보를 얻어가는 컨트롤러
*/
package oracle;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MyModel extends AbstractTableModel{
	Vector columnName;		//컬럼의 제목을 담을 벡터
	Vector <Vector>list;					//레코드를 담을 이차원 벡터			//벡터를 제너릭으로 준것은 계속 줘야한다.
	
	public MyModel(Vector list, Vector columnName) {
		this.list = list;
		this.columnName = columnName;
		
	}
	
	public int getColumnCount() {
		return columnName.size();
	}

	public int getRowCount() {
		return list.size();
	}
	
	//컬럼이름 가져오기.
	public String getColumnName(int col) {	
		return (String)columnName.elementAt(col);
	}
	
	//row, col에 위치한 셀을 편집 가능하게 한다.
	public boolean isCellEditable(int rowIndex, int columnIndex) {		//편집가능하게 만드는 것
		return true;		//각 (0,0) 셀마다 수정이 가능한지 물어본다. true값줘서. 
	}
	
	//각 셀의 변경값을 반영하는 메서드 오버라이드
	public void setValueAt(Object Value, int row, int col) {
		//2차원 벡터를 인덱스로 조종하여 값을 수정하는 것이다.
		//층(row), 호수(col)를 변경한다.
		Vector vec = list.get(row);
		vec.set(col, Value);	//col에 있는 value를 건들겠다.
			
		this.fireTableCellUpdated(row, col); //테이블의 값이 변경되었을 때 알려주는 메서드
	}

	public Object getValueAt(int row, int col) {
		Vector vec = list.get(row);
		return vec.elementAt(col);
	}

	
}
