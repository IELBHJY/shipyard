package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class image {

    @FXML
    private ImageView imageview;

    @FXML
    private Button button1;

    @FXML
    void show_image(ActionEvent event) {
        Image image=new Image("sample/Images/roads.png");
        imageview.setImage(image);
    }
}
