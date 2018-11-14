package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.ResultSet;

public class Login {

    @FXML
    private Label password;

    @FXML
    private Label user_id;

    @FXML
    private TextField nameText;

    @FXML
    private PasswordField passField;

    @FXML
    private Button OK;

    Main main;

    ConnectDatabase db;

    @FXML
    void initialize() {
        main=new Main();
    }

    @FXML
    private void button_ok_click() throws Exception {
        db=new ConnectDatabase();
        db.connection();
        ResultSet rs=db.query("user");
        while (rs.next()){
            if(rs.getString("user_name").equals(nameText.getText())
                && rs.getString("password").equals(passField.getText())){
                main.showMainStage();
                break;
            }
        }
        db.close();
    }
}
