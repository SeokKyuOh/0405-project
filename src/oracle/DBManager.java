package oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	static private DBManager instance;
	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@localhost:1521:XE";
	private String user = "batman";
	private String password = "1234";
	
	Connection con;		//접속 후, 그 정보 담는 객체
	
	// 만드는 이유?? new를 막기 위함. 아무나 못만들게 하기 위해
	/*
		1.드라이버 로드
		2.접속
		3.쿼리실행
		4.반납
	*/
	private DBManager() {
		try {
			Class.forName(driver);//드라이버 로드
			con = DriverManager.getConnection(url, user, password);		//접속
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//인스턴스를 new하지 않고 만들기 위해.. 그래서 static으로 선언
	static public DBManager getInstance(){
		if(instance == null){		//최초 한번만 new하기 위해
			instance = new DBManager();
		}
		return instance;
	}
	
	//접속객체 반환
	public Connection getConnection(){
		return con;
	}
	
	//접속해제
	public void disConnect(Connection con){
		if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}












