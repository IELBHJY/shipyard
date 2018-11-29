package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.ResultSet;

public class storage {

    @FXML
    private TableColumn<duiwei, String> xcood;

    @FXML
    private TableColumn<duiwei, String> name;

    @FXML
    private TableView<duiwei> storage_table;

    @FXML
    private TableColumn<duiwei, String> id;

    @FXML
    private TableColumn<duiwei, String> ycood;

    private ObservableList<duiwei> data;

    ConnectDatabase db;

    @FXML
    void initialize() throws Exception{
        data=FXCollections.observableArrayList();
        initialData();
        configTableView();
        storage_table.setItems(data);
    }

    /**
     * 初始化表格数据，连接mysql数据库
     * */
    private void initialData() throws Exception{
        db=new ConnectDatabase();
        db.connection();
        ResultSet rs=db.query("storage");
        while (rs.next()){
            data.add(new duiwei(rs.getString("ID"),rs.getString("storage_name"),rs.getString("xcoordinate"),rs.getString("ycoordinate")));
        }
        db.close();
    }

    /***
     * 把数据关联到表格上
     */
    private void configTableView(){
        id.setCellValueFactory(new PropertyValueFactory<duiwei, String>("id"));
        name.setCellValueFactory(new PropertyValueFactory<duiwei, String>("name"));
        xcood.setCellValueFactory(new PropertyValueFactory<duiwei, String>("xcood"));
        ycood.setCellValueFactory(new PropertyValueFactory<duiwei, String>("ycood"));
    }

}
