package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.ResultSet;

public class user {

    @FXML
    private TableView<Person> user_table;

    @FXML
    private TableColumn<Person, String> password;

    @FXML
    private TableColumn<Person, String> user_name;

    @FXML
    private TableColumn<Person, String> ID;

    private ObservableList<Person> data;

    ConnectDatabase db;

    @FXML
    void initialize() throws Exception{
        data=FXCollections.observableArrayList();
        initialData();
        configTableView();
        user_table.setItems(data);
    }

    /**
     * 初始化表格数据，连接mysql数据库
     * */
    private void initialData() throws Exception{
        db=new ConnectDatabase();
        db.connection();
        ResultSet rs=db.query("user");
        while (rs.next()){
            data.add(new Person(rs.getString("ID"),rs.getString("user_name"),rs.getString("password")));
        }
        db.close();
    }

    /***
     * 把数据关联到表格上
     */
    private void configTableView(){
        ID.setCellValueFactory(new PropertyValueFactory<Person, String>("id"));
        user_name.setCellValueFactory(new PropertyValueFactory<Person, String>("user_name"));
        password.setCellValueFactory(new PropertyValueFactory<Person, String>("password"));
    }

}
