/*
	JTable�� ���÷� ������ ���� ��Ʈ�ѷ�
*/
package oracle;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MyModel extends AbstractTableModel{
	Vector columnName;		//�÷��� ������ ���� ����
	Vector <Vector>list;					//���ڵ带 ���� ������ ����			//���͸� ���ʸ����� �ذ��� ��� ����Ѵ�.
	
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
	
	//�÷��̸� ��������.
	public String getColumnName(int col) {	
		return (String)columnName.elementAt(col);
	}
	
	//row, col�� ��ġ�� ���� ���� �����ϰ� �Ѵ�.
	public boolean isCellEditable(int rowIndex, int columnIndex) {		//���������ϰ� ����� ��
		return true;		//�� (0,0) ������ ������ �������� �����. true���༭. 
	}
	
	//�� ���� ���氪�� �ݿ��ϴ� �޼��� �������̵�
	public void setValueAt(Object Value, int row, int col) {
		//2���� ���͸� �ε����� �����Ͽ� ���� �����ϴ� ���̴�.
		//��(row), ȣ��(col)�� �����Ѵ�.
		Vector vec = list.get(row);
		vec.set(col, Value);	//col�� �ִ� value�� �ǵ�ڴ�.
			
		this.fireTableCellUpdated(row, col); //���̺��� ���� ����Ǿ��� �� �˷��ִ� �޼���
	}

	public Object getValueAt(int row, int col) {
		Vector vec = list.get(row);
		return vec.elementAt(col);
	}

	
}
