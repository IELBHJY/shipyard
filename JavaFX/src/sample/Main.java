package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage login_stage;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("FXML/Login.fxml"));
        login_stage=new Stage();
        login_stage.setTitle("用户登陆");
        login_stage.setScene(new Scene(root, 800, 600));
        login_stage.show();
    }



    public void showMainStage() throws Exception{
        Stage main_stage=new Stage();
        Main.login_stage.close();
        Parent root = FXMLLoader.load(getClass().getResource("FXML/main_stage.fxml"));
        main_stage.setScene(new Scene(root, 800, 600));
        main_stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
