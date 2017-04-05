package oracle;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;

public class LoadMain extends JFrame implements ActionListener, TableModelListener{
	JPanel p_north;
	JTextField t_path;
	JButton bt_open, bt_load, bt_excel, bt_del;
	JTable table;
	JScrollPane scroll;
	JFileChooser chooser;
	
	/*���߿� �۾��ϱ� ���� ��������� �÷��д�.*/
	FileReader reader = null;		//������ ������� �ϴ� ���ڵ� ���� �� �ִ� reader
	BufferedReader buffr = null;		
	
	//������ â�� ������ �̹� ������ Ȯ���س���.
	DBManager manager = DBManager.getInstance();		//�ʱ�ȭ. �޼��� ȣ��   = ������ �����ڿ� �ص� �������.
	Connection con;
	
	Vector<Vector> list;
	Vector columnName;
	
	public LoadMain() {
		p_north = new JPanel();
		t_path = new JTextField(25);
		bt_open = new JButton("���Ͽ���");
		bt_load = new JButton("�ε��ϱ�");
		bt_excel = new JButton("�����ε�");
		bt_del = new JButton("�����ϱ�");
		
		table = new JTable();		//JTable�� ���̺� ���� ����� ��� �������� ���� ���� �������� ���̺� ���� å������.
		scroll = new JScrollPane(table);
		chooser = new JFileChooser("C://animal");	// �⺻��� 
				
		p_north.add(t_path);
		p_north.add(bt_open);
		p_north.add(bt_load);
		p_north.add(bt_excel);
		p_north.add(bt_del);
		
		add(p_north, BorderLayout.NORTH);
		add(scroll);
				
		bt_open.addActionListener(this);
		bt_load.addActionListener(this);
		bt_excel.addActionListener(this);
		bt_del.addActionListener(this);
		
		//������� ������ ����
		//�̹� LoadMain�� �������� �ڽ��̹Ƿ� ���ο���� ����
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				//�����ͺ��̽� �ڿ� ����
				manager.disConnect(con);
				
				//���μ��� ����
				System.exit(0);
			}
		});
		
		
		
		
		
		setVisible(true);
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		init();
		
	}
	//���� Ž���� ����
	public void open(){
		int result = chooser.showOpenDialog(this);
		
		//���⸦ ������..���� ���Ͽ� ��Ʈ���� ��������
		if(result == JFileChooser.APPROVE_OPTION){
			
			//������ ������ ����
			File file = chooser.getSelectedFile();
			t_path.setText(file.getAbsolutePath());

			try {
				reader = new FileReader(file);				//�̰� ���� ���� �A ����?? try�� �ȿ� ���� ��� ���߿� finally���� ���� ���� ���� ������.
				buffr = new BufferedReader(reader);		//���� ������ reader�� ��´�.
				/*while���� ���ؼ� �а� �����͸� �ٷ� ����Ŭ�� �Է��Ϸ��� while���� ���ư��� �ӵ��� ��Ʈ��ũ �ӵ����� ���̰� �߻��Ѵ�. 
				�׷��� ���븸 �ȾƵΰ� ���Ƶ��̴� ���� ���߿� ����*/
						
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}		
		}
		
	} 
	
	//CSV --> Oracle�� ������ ����(migragtion)�ϱ�
	public void load(){
		//���۽�Ʈ���� �̿��Ͽ� CSV�� �����͸� 1�پ� �о�鿩 insert��Ű��
		//���ڴٰ� ���� �� ����..
		//while������ ������ �ʹ� �����Ƿ� ��Ʈ��ũ�� ������ �� ���� ������ �Ϻη� ������Ű�鼭 �����Ѵ�.
		String data;
		StringBuffer sb = new StringBuffer();
		PreparedStatement pstmt = null;
			
		try {
			while(true){
				data = buffr.readLine();
				
				if(data==null)break;
				
				String[] value = data.split(",");			//���� ,�� �������� �и��Ҳ���   .�� ���� ��ɼ� ���ڴ� �տ� \\�� �־���������� ���⼭ ,�� ��ɼ� ���ڰ� �ƴ�.
				
				//seq ���� �����ϰ� insert�ϰڴ�.
				if(!value[0].equals("seq")){	
					sb.append("insert into hospital(seq,name,addr,regdate,status,dimension,type)");
					sb.append(" values("+value[0]+",'"+value[1]+"','"+value[2]+"','"+value[3]+"','"+value[4]+"',"+value[5]+",'"+value[6]+"')");
					
					System.out.println(sb.toString());
					pstmt = con.prepareStatement(sb.toString());		//������ �޾ƿ���
					
					int result = pstmt.executeUpdate();		//��������
					
					//������ ������ StringBuffer�� �����͸� ��� �����
					sb.delete(0, sb.length());
					
				}else{
					System.out.println("�� ù��° ���̹Ƿ� ����");
				}
			}	
			JOptionPane.showMessageDialog(this, "���̱׷��̼� �Ϸ�!");
			
			//JTable ������ ó��
			getList();
			table.setModel(new MyModel(list, columnName));
			
			//���̺� �𵨰� �����ʿ��� ����	//���� ������ �Ŀ� �����ʸ� �����ؾ��Ѵ�. �׷��� �������õ� �޼��尡 ȣ�Ⱑ��
			table.getModel().addTableModelListener(this);;		//�������ϰ� �ִ� table�� ���� MyModel�� ����ϰ� �־ table�̶�� ��Ī�ص� �ȴ�. MyModel�ص� �ǰ�.
			table.updateUI();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//���� ���� �о DB�� ���̱׷��̼� �ϱ�
	//javaSE �������� ���̺귯�� �ִ�??? ����.
	//open Source ��������Ʈ����
	//copyright (MS�� ��å, ������) <---> copyleft(����ġ ��ü. ����Ʈ����� �����Ǿ���Ѵ�.)
	//POI ���̺귯��! http://apache.org
	//APCHE�� API   http://poi.apache.org/apidocs/index.html
	/*
	 	HSSFWorkbook : ��������
	 	HSSFSheet : sheet
	 	HSSFRow : row
	 	HSSFCell : cell
	 	
	 	���¼��� ���� -> sheet -> row -> cell
	*/
	public void loadExcel(){		// �������� �� xls�� �����ϴ�. xlsx�� �Ұ��� (�ٸ� �޼��带 ����ؾ��Ѵ�.)
		int result = chooser.showOpenDialog(this);
		
		if(result == JFileChooser.APPROVE_OPTION){
			File file = chooser.getSelectedFile();	
			FileInputStream fis=null;
			
			try {
				fis = new FileInputStream(file);	//��������
				
				HSSFWorkbook book = null;		//����ġ���� �ٿ���� ���̺귯���� �߰��߱� ������ import ����
				book = new HSSFWorkbook(fis);
				
				HSSFSheet sheet = null;
				sheet = book.getSheet("sheet1");
				
				int total = sheet.getLastRowNum();		//row�� ������ ��ȣ �ޱ�
				DataFormatter df = new DataFormatter();	//�ڷ����� ������� �ޱ�.
												
				for(int a=1; a<=total; a++){	//0��° �� �÷� �����ϱ� ���� 1������ ����
					HSSFRow row = sheet.getRow(a);
					
					int columnCount = row.getLastCellNum();		//�÷��� ������ ��ȣ �ޱ�
					
					for(int i=0; i<columnCount; i++){	//��� 0��°���� ���� �������ϱ� 0����
						HSSFCell cell = row.getCell(i);
						
						//�ڷ����� ���ѵ��� �ʰ� ��� String ó��
						String value = df.formatCellValue(cell);
						
						System.out.print(value); //ǥ��
					}
					System.out.println("");	//�ٶ���
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}		
	}
	
	//JTable�� ��� ���ڵ� ��������
	public void getList(){
		String sql="select * from hospital order by seq asc";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			//rs�� DB���� ���������� vector�� ��������. tableModel�� ���ؼ�. 
			
			//�÷��� ����
			ResultSetMetaData meta = rs.getMetaData();	
			int count = meta.getColumnCount();		//for�� ���� �÷���ܿ� add�ϴ� ����
			columnName = new Vector();
			
			for(int i=0;i<count;i++){
				columnName.add(meta.getColumnName(i+1));
			}
			
			list = new Vector<Vector>();		//������ ����. �Ʒ��� ���� ���͵��� ���� ū ���� // �ʿ��� �������� Ȱ���� �� �ְ� ��������� ������
			
			while(rs.next()){
				Vector vec = new Vector(); //���ڵ� 1�� ��������
				
				vec.add(rs.getString("seq"));
				vec.add(rs.getString("name"));
				vec.add(rs.getString("addr"));
				vec.add(rs.getString("regdate"));
				vec.add(rs.getString("status"));
				vec.add(rs.getString("dimension"));
				vec.add(rs.getString("type"));
				
				list.add(vec);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	//������ ���ڵ� ����
	public void delete(){
		
	}
	
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj == bt_open){
			open();
		} else if(obj == bt_load){
			load();
		} else if(obj == bt_excel){
			loadExcel();
		} else if(obj == bt_del){
			delete();
		}	
	}
	
	public void init(){
		//Connection ���� ����
		con = manager.getConnection();
	}
	
	//���̺� ���� �����Ͱ��� ������ �߻��ϸ�, �� ������ �����ϴ� ������
	public void tableChanged(TableModelEvent e) {
		System.out.println("�ٲ��?");
		//update hospital set �÷���=�� where   ������Ʈ�� ����ϱ�
		//���� ���� ������ �����Ͱ� ���޸� ������ ����.
		
	}
	
	public static void main(String[] args) {
		new LoadMain();

	}

}


