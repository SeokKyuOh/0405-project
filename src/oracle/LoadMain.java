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
	
	/*나중에 작업하기 위에 멤버변수로 올려둔다.*/
	FileReader reader = null;		//파일을 대상으로 하는 문자도 읽을 수 있는 reader
	BufferedReader buffr = null;		
	
	//윈도우 창이 열리면 이미 접속을 확보해놓자.
	DBManager manager = DBManager.getInstance();		//초기화. 메서드 호출   = 다음은 생성자에 해도 상관없다.
	Connection con;
	
	Vector<Vector> list;
	Vector columnName;
	
	public LoadMain() {
		p_north = new JPanel();
		t_path = new JTextField(25);
		bt_open = new JButton("파일열기");
		bt_load = new JButton("로드하기");
		bt_excel = new JButton("엑셀로드");
		bt_del = new JButton("삭제하기");
		
		table = new JTable();		//JTable은 테이블 모델을 사용할 경우 보여지는 것은 물론 편집까지 테이블 모델이 책임진다.
		scroll = new JScrollPane(table);
		chooser = new JFileChooser("C://animal");	// 기본경로 
				
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
		
		//윈도우와 리스너 연결
		//이미 LoadMain이 누군가의 자식이므로 내부연결로 하자
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				//데이터베이스 자원 해제
				manager.disConnect(con);
				
				//프로세스 종료
				System.exit(0);
			}
		});
		
		
		
		
		
		setVisible(true);
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		init();
		
	}
	//파일 탐색기 띄우기
	public void open(){
		int result = chooser.showOpenDialog(this);
		
		//열기를 누르면..목적 파일에 스트림을 생성하자
		if(result == JFileChooser.APPROVE_OPTION){
			
			//유저가 선택한 파일
			File file = chooser.getSelectedFile();
			t_path.setText(file.getAbsolutePath());

			try {
				reader = new FileReader(file);				//이걸 위에 따로 뺸 이유?? try문 안에 있을 경우 나중에 finally에서 닫을 수가 없기 때문에.
				buffr = new BufferedReader(reader);		//작은 빨대인 reader를 담는다.
				/*while문을 통해서 읽고 데이터를 바로 오라클에 입력하려면 while문이 돌아가는 속도와 네트워크 속도간의 차이가 발생한다. 
				그래서 빨대만 꽂아두고 빨아들이는 것은 나중에 하자*/
						
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}		
		}
		
	} 
	
	//CSV --> Oracle로 데이터 이전(migragtion)하기
	public void load(){
		//버퍼스트림을 이용하여 CSV의 데이터를 1줄씩 읽어들여 insert시키자
		//레코다가 없을 때 까지..
		//while문으로 돌리면 너무 빠르므로 네트워크가 감당할 수 없기 때문에 일부러 지연시키면서 실행한다.
		String data;
		StringBuffer sb = new StringBuffer();
		PreparedStatement pstmt = null;
			
		try {
			while(true){
				data = buffr.readLine();
				
				if(data==null)break;
				
				String[] value = data.split(",");			//나는 ,를 기준으로 분리할꺼야   .과 같은 기능성 문자는 앞에 \\를 넣어줘야하지만 여기서 ,는 기능성 문자가 아님.
				
				//seq 줄을 제외하고 insert하겠다.
				if(!value[0].equals("seq")){	
					sb.append("insert into hospital(seq,name,addr,regdate,status,dimension,type)");
					sb.append(" values("+value[0]+",'"+value[1]+"','"+value[2]+"','"+value[3]+"','"+value[4]+"',"+value[5]+",'"+value[6]+"')");
					
					System.out.println(sb.toString());
					pstmt = con.prepareStatement(sb.toString());		//쿼리문 받아오기
					
					int result = pstmt.executeUpdate();		//쿼리수행
					
					//기존에 누적된 StringBuffer의 데이터를 모두 지우기
					sb.delete(0, sb.length());
					
				}else{
					System.out.println("난 첫번째 줄이므로 제외");
				}
			}	
			JOptionPane.showMessageDialog(this, "마이그레이션 완료!");
			
			//JTable 나오게 처리
			getList();
			table.setModel(new MyModel(list, columnName));
			
			//테이블 모델과 리스너와의 연결	//모델을 적용한 후에 리스너를 연결해야한다. 그래야 수정관련된 메서드가 호출가능
			table.getModel().addTableModelListener(this);;		//현재사용하고 있는 table이 현재 MyModel을 사용하고 있어서 table이라고 지칭해도 된다. MyModel해도 되고.
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
	
	//엑셀 파일 읽어서 DB에 마이그레이션 하기
	//javaSE 엑셀제어 라이브러리 있다??? 없음.
	//open Source 공개소프트웨어
	//copyright (MS의 정책, 돈내라) <---> copyleft(아파치 단체. 소프트웨어는 공개되어야한다.)
	//POI 라이브러리! http://apache.org
	//APCHE의 API   http://poi.apache.org/apidocs/index.html
	/*
	 	HSSFWorkbook : 엑셀파일
	 	HSSFSheet : sheet
	 	HSSFRow : row
	 	HSSFCell : cell
	 	
	 	여는순서 파일 -> sheet -> row -> cell
	*/
	public void loadExcel(){		// 엑셀파일 중 xls만 가능하다. xlsx는 불가능 (다른 메서드를 사용해야한다.)
		int result = chooser.showOpenDialog(this);
		
		if(result == JFileChooser.APPROVE_OPTION){
			File file = chooser.getSelectedFile();	
			FileInputStream fis=null;
			
			try {
				fis = new FileInputStream(file);	//원본빨대
				
				HSSFWorkbook book = null;		//아파치에서 다운받은 라이브러리를 추가했기 때문에 import 가능
				book = new HSSFWorkbook(fis);
				
				HSSFSheet sheet = null;
				sheet = book.getSheet("sheet1");
				
				int total = sheet.getLastRowNum();		//row의 마지막 번호 받기
				DataFormatter df = new DataFormatter();	//자료형에 상관없이 받기.
												
				for(int a=1; a<=total; a++){	//0번째 줄 컬럼 제외하기 위해 1번부터 시작
					HSSFRow row = sheet.getRow(a);
					
					int columnCount = row.getLastCellNum();		//컬럼의 마지막 번호 받기
					
					for(int i=0; i<columnCount; i++){	//얘는 0번째부터 값을 받을꺼니까 0부터
						HSSFCell cell = row.getCell(i);
						
						//자료형에 국한되지 않고 모두 String 처리
						String value = df.formatCellValue(cell);
						
						System.out.print(value); //표기
					}
					System.out.println("");	//줄띄우기
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}		
	}
	
	//JTable에 모든 레코드 가져오기
	public void getList(){
		String sql="select * from hospital order by seq asc";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			//rs를 DB연동 끝난다음에 vector를 가공하자. tableModel을 위해서. 
			
			//컬럼명도 추출
			ResultSetMetaData meta = rs.getMetaData();	
			int count = meta.getColumnCount();		//for문 돌때 컬럼명단에 add하는 역할
			columnName = new Vector();
			
			for(int i=0;i<count;i++){
				columnName.add(meta.getColumnName(i+1));
			}
			
			list = new Vector<Vector>();		//이차원 백터. 아래의 작은 벡터들을 담을 큰 벡터 // 필요한 시점에서 활용할 수 있게 멤버변수로 뺴두자
			
			while(rs.next()){
				Vector vec = new Vector(); //레코드 1건 담을거임
				
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
	
	//선택한 레코드 삭제
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
		//Connection 얻어다 놓기
		con = manager.getConnection();
	}
	
	//테이블 모델의 데이터값에 변경이 발생하면, 그 찰나를 감지하는 리스너
	public void tableChanged(TableModelEvent e) {
		System.out.println("바꿨니?");
		//update hospital set 컬럼명=값 where   업데이트문 출력하기
		//내가 현재 수정한 데이터가 몇콤마 몇인지 찍어보자.
		
	}
	
	public static void main(String[] args) {
		new LoadMain();

	}

}


