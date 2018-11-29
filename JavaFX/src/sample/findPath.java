package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sample.Algorithm.FindBestPath;

import java.util.List;

public class findPath {

    FindBestPath findBestPath;

    @FXML
    private TextField start_point;

    @FXML
    private TextField end_point;

    @FXML
    private ImageView image_view;

    @FXML
    private Button find_path;

    @FXML
    private Label title;

    @FXML
    private TextField result_path;
    @FXML
    private TextField distance;

    @FXML
    private TextField turn;

    @FXML
    private TextField time;

    @FXML
    void show_path(ActionEvent event) throws Exception {
       String start=start_point.getText();
       String end=end_point.getText();
       double[] cost={0.0,0.0};
       List<Integer> list=findBestPath.findShortPath(start,end,cost);
       String result=start+"->";
       for(Integer i:list){
           result+=i.toString()+"->";
       }
       result+=end;
       result_path.setText(result);
       distance.setText(String.valueOf(cost[0]));
       turn.setText(String.valueOf(cost[1]));
    }


    @FXML
    void initialize() {
        assert start_point != null : "fx:id=\"start_point\" was not injected: check your FXML file 'findPath.fxml'.";
        assert end_point != null : "fx:id=\"end_point\" was not injected: check your FXML file 'findPath.fxml'.";
        assert image_view != null : "fx:id=\"image_view\" was not injected: check your FXML file 'findPath.fxml'.";
        assert find_path != null : "fx:id=\"find_path\" was not injected: check your FXML file 'findPath.fxml'.";
        assert title != null : "fx:id=\"title\" was not injected: check your FXML file 'findPath.fxml'.";
        assert result_path != null : "fx:id=\"result_path\" was not injected: check your FXML file 'findPath.fxml'.";
        Image image=new Image("sample/Images/new_roads.png");
        image_view.setImage(image);
        int[][] adj={{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,1,1,0,1,1,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,1,0,0,1,0,0,0,1,0,0,0,0,0},
                {0,0,0,0,0,1,1,0,0,0,0,0,1,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0},
                {0,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0},
                {0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0},
                {0,0,0,0,0,0,1,0,0,0,1,0,1,0,0,0,1},
                {0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0},
                {0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1,0},
                {0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1},
                {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0},
        };
        try {
            findBestPath = new FindBestPath(16, adj);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
