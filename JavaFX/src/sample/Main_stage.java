package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.stage.Stage;

public class Main_stage {


    @FXML
    private MenuItem ship_data_close;

    @FXML
    private MenuItem block_information;

    @FXML
    private MenuItem userInformation;

    @FXML
    private MenuItem user_close;

    @FXML
    private MenuItem flatcar_information;

    @FXML
    private RadioMenuItem yard_information;

    @FXML
    private MenuItem shipyard_information;
    Stage user_stage;
    Stage flatcar_stage;
    Stage road_stage;

    @FXML
    void show_userTable_click(ActionEvent event) throws Exception{
        user_stage=new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("FXML/user.fxml"));
        user_stage.setScene(new Scene(root, 600, 500));
        user_stage.show();
    }

    @FXML
    void user_close_click(ActionEvent event) {


    }

    @FXML
    void show_blocksTable(ActionEvent event) {

    }

    @FXML
    void show_shipyardTable(ActionEvent event) {

    }

    @FXML
    void show_yardTable(ActionEvent event) {

    }

    @FXML
    void show_flatcarTable(ActionEvent event) throws Exception{
        flatcar_stage=new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("FXML/flatcar.fxml"));
        flatcar_stage.setScene(new Scene(root, 600, 500));
        flatcar_stage.show();
    }

    @FXML
    void show_road(ActionEvent event) throws Exception{
        road_stage=new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("FXML/road.fxml"));
        road_stage.setScene(new Scene(root, 600, 500));
        road_stage.show();
    }


    @FXML
    void ship_data_close_click(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert ship_data_close != null : "fx:id=\"ship_data_close\" was not injected: check your FXML file 'main_stage.fxml'.";
        assert block_information != null : "fx:id=\"block_information\" was not injected: check your FXML file 'main_stage.fxml'.";
        assert userInformation != null : "fx:id=\"userInformation\" was not injected: check your FXML file 'main_stage.fxml'.";
        assert user_close != null : "fx:id=\"user_close\" was not injected: check your FXML file 'main_stage.fxml'.";
        assert flatcar_information != null : "fx:id=\"flatcar_information\" was not injected: check your FXML file 'main_stage.fxml'.";
        assert yard_information != null : "fx:id=\"yard_information\" was not injected: check your FXML file 'main_stage.fxml'.";
        assert shipyard_information != null : "fx:id=\"shipyard_information\" was not injected: check your FXML file 'main_stage.fxml'.";

    }
}
