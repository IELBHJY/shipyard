package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnectDB {
    Connection con;
    Statement statement;
    /***
     * java通过sql.jdbc连接mysql数据库
     * @throws Exception
     */
    public void connection() throws Exception{
        //驱动程序名
        String driver = "com.mysql.jdbc.Driver";
        //URL指向要访问的数据库名mydata
        String url = "jdbc:mysql://localhost:3306/shipyard";
        //MySQL配置时的用户名
        String user = "root";
        //MySQL配置时的密码
        String password = "12345678";
        Class.forName(driver);
        con = DriverManager.getConnection(url,user,password);
        if(!con.isClosed())
            System.out.println("Succeeded connecting to the Database!");
    }

    public void close() throws Exception{
        this.con.close();
    }

    public ResultSet query(String table_name) throws Exception{
        statement = con.createStatement();
        String sql = "select * from "+table_name;
        ResultSet rs = statement.executeQuery(sql);
        return rs;
    }

    public ResultSet search(String order) throws Exception{
        statement = con.createStatement();
        ResultSet rs = statement.executeQuery(order);
        return rs;
    }
}
