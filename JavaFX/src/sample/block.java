package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

import java.io.File;

public class block {

    @FXML
    private Button button1;

    String dataPath;

    @FXML
    void button1_click(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("source"));
        File seletedFile = fc.showOpenDialog(null);
        if(seletedFile != null) {
            dataPath = seletedFile.getAbsolutePath();
        }else{
            System.out.println("File is not valid");
        }
    }
}
