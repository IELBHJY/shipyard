package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class Login {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label password;

    @FXML
    private Label user_id;

    @FXML
    private TextField password_text;

    @FXML
    private Button OK;

    @FXML
    private TextField user_id_text;

    Main main;

    @FXML
    void initialize() {
        assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'login.fxml'.";
        assert user_id != null : "fx:id=\"user_id\" was not injected: check your FXML file 'login.fxml'.";
        assert password_text != null : "fx:id=\"password_text\" was not injected: check your FXML file 'login.fxml'.";
        assert OK != null : "fx:id=\"OK\" was not injected: check your FXML file 'login.fxml'.";
        assert user_id_text != null : "fx:id=\"user_id_text\" was not injected: check your FXML file 'login.fxml'.";
        main=new Main();
    }
    @FXML
    private void button_ok_click() throws Exception {
        main.showMainStage();
    }
}
