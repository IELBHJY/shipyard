package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.ResultSet;

public class road {

    @FXML
    private TableColumn<crossover, String> xcood;

    @FXML
    private TableView<crossover> road_table;

    @FXML
    private ImageView road_image;

    @FXML
    private TableColumn<crossover, String> id;

    @FXML
    private TableColumn<crossover, String> ycood;

    private ObservableList<crossover> data;

    ConnectDatabase db;

    @FXML
    void initialize() throws Exception{
        data=FXCollections.observableArrayList();
        initialData();
        configTableView();
        road_table.setItems(data);
        Image image=new Image("sample/Images/roads.png");
        road_image.setImage(image);
    }

    /**
     * 初始化表格数据，连接mysql数据库
     * */
    private void initialData() throws Exception{
        db=new ConnectDatabase();
        db.connection();
        ResultSet rs=db.query("roads");
        while (rs.next()){
            data.add(new crossover(rs.getString("ID"),rs.getString("横坐标"),rs.getString("纵坐标")));
        }
        db.close();
    }

    /***
     * 把数据关联到表格上
     */
    private void configTableView(){
        id.setCellValueFactory(new PropertyValueFactory<crossover, String>("id"));
        xcood.setCellValueFactory(new PropertyValueFactory<crossover, String>("xcood"));
        ycood.setCellValueFactory(new PropertyValueFactory<crossover, String>("ycood"));
    }

}
