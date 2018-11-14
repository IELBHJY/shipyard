package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.ResultSet;

public class flatcar {
    @FXML
    private TableView<car> flatcar_table;

    @FXML
    private TableColumn<car, String> id;

    @FXML
    private TableColumn<car, String> carry_weight;

    @FXML
    private TableColumn<car, String> type;

    private ObservableList<car> data;

    ConnectDatabase db;

    @FXML
    void initialize() throws Exception{
        data=FXCollections.observableArrayList();
        initialData();
        configTableView();
        flatcar_table.setItems(data);
    }

    /**
     * 初始化表格数据，连接mysql数据库
     * */
    private void initialData() throws Exception{
        db=new ConnectDatabase();
        db.connection();
        ResultSet rs=db.query("flatcars");
        while (rs.next()){
            data.add(new car(rs.getString("ID"),rs.getString("平板车编号"),rs.getString("额定载重")));
        }
        db.close();
    }

    /***
     * 把数据关联到表格上
     */
    private void configTableView(){
        id.setCellValueFactory(new PropertyValueFactory<car, String>("id"));
        type.setCellValueFactory(new PropertyValueFactory<car, String>("type"));
        carry_weight.setCellValueFactory(new PropertyValueFactory<car, String>("carry_weight"));
    }

}
