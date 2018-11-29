package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import sample.shipyard.Data;
import sample.shipyard.Genetic;
import sample.shipyard.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class  ovoc {

    @FXML
    private TextField file_path;

    @FXML
    private Button select_task_file;

    @FXML
    private ComboBox<String> truck_list;

    @FXML
    private Button select_truck_button;

    @FXML
    private Button solve_button;

    @FXML
    private Button show_path_button;

    @FXML
    private ListView<String> truck_listview;

    @FXML
    private ProgressBar solve_process;

    @FXML
    private TextField truck;

    private int truck_list_index;

    private int task_count;
    private int truck_count;
    private List<Integer> trucks;

    @FXML
    private TableColumn<Solutions, Integer> truck_id;

    @FXML
    private TableView<Solutions> result_table;
    @FXML
    private TableColumn<Solutions, Double> task_weight;
    @FXML
    private TableColumn<Solutions, Double> late_time;
    @FXML
    private TableColumn<Solutions, Integer> task_id;
    @FXML
    private TableColumn<Solutions, Double> task_time;
    @FXML
    private TableColumn<Solutions, Double> early_time;
    @FXML
    private TableColumn<Solutions,String> task_path;
    @FXML
    private TableColumn<Solutions,Double> truck_capacity;

    private ObservableList<Solutions> data;

    private Data ovoc_data;

    @FXML
    void initialize() {
        assert file_path != null : "fx:id=\"file_path\" was not injected: check your FXML file 'ovoc.fxml'.";
        assert select_task_file != null : "fx:id=\"select_task_file\" was not injected: check your FXML file 'ovoc.fxml'.";
        assert truck_list != null : "fx:id=\"truck_list\" was not injected: check your FXML file 'ovoc.fxml'.";
        for(int i=1;i<=16;i++){
            truck_list.getItems().add(i+"号平板车");
        }
        truck_listview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton()==MouseButton.PRIMARY &&
                        event.getClickCount()==2){
                    String temp1=truck_listview.getSelectionModel().getSelectedItem();
                    truck_listview.getItems().remove(temp1);
                    int t=Integer.parseInt(temp1.split("号")[0]);
                    for(int i=0;i<truck_list.getItems().size();i++){
                        String temp=truck_list.getItems().get(i);
                        int t1=Integer.parseInt(temp.split("号")[0]);
                        if(t1<t){
                            truck_list_index=i;
                        }
                    }
                    //System.out.println(t+" "+truck_list_index+" "+temp1);
                    if(truck_list_index==0){
                        truck_list.getItems().add(truck_list_index,temp1);
                    }else {
                        truck_list.getItems().add(truck_list_index + 1, temp1);
                    }
                }
            }
        });
    }

    @FXML
    void select_file(ActionEvent event) throws IOException,BiffException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("Data/"));
        File seletedFile = fc.showOpenDialog(null);
        if(seletedFile != null) {
            file_path.setText(seletedFile.getAbsolutePath());
            InputStream stream = new FileInputStream(file_path.getText());
            //获取Excel文件对象
            Workbook rwb = Workbook.getWorkbook(stream);
            Sheet sheet=rwb.getSheet("test40");
            task_count=sheet.getRows()-1;
            System.out.println(task_count);
        }else{
            System.out.println("File is not valid");
        }
    }

    @FXML
    void select_truck(ActionEvent event) {
        truck_listview.getItems().add(truck_list.getValue());
        String temp=truck_list.getValue();
        truck_list_index=truck_list.getItems().indexOf(temp);
        truck_list.getItems().remove(truck_list_index);
    }

    private void setTruck_count(){
        truck_count=truck_listview.getItems().size();
        trucks=new ArrayList<Integer>();
        for(int i=0;i<truck_count;i++){
            String temp=truck_listview.getItems().get(i);
            trucks.add(Integer.parseInt(temp.split("号")[0]));
        }
    }

    @FXML
    void solve(ActionEvent event) throws Exception {
        //确定是哪些平板车
        setTruck_count();
        data=FXCollections.observableArrayList();
        String filePath=file_path.getText();
        ovoc_data=new Data(task_count,truck_count,16,237,trucks);
        final Genetic ga = new Genetic(task_count, truck_count, Parameter.ga_size, ovoc_data);
        try {
            ovoc_data.readData(filePath);
        }catch (Exception e){
            e.printStackTrace();
        }
        Service<Integer> service = new Service<Integer>() {
            @Override
            protected Task<Integer> createTask() {
                return new Task<Integer>() {
                    @Override
                    protected Integer call()  {
                        int current_count=0;
                        int improve_count=0;
                        ga.creatInitialSolution();
                        while(current_count<Parameter.ga_iteration && improve_count<Parameter.improve_iteration) {
                            double before=ga.getBest_value();
                            ga.solve();
                            current_count++;
                            double after=ga.getBest_value();
                            if(before-after<Parameter.epsilon){
                                improve_count++;
                            }else{
                                improve_count=0;
                            }
                            System.out.println(current_count);
                            updateProgress(current_count, Parameter.ga_iteration);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        ga.testSolution();
                        System.out.println("执行完毕");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("执行run");
                                initialData(ga.getBest_value(),ga.getResults(),ga.getTaskTime());
                                configTableView();
                                setData();
                            }
                        });
                        System.out.println("done");
                        return null;
                        };
                    };
                };
        };
        solve_process.progressProperty().bind(service.progressProperty());
        service.restart();
    }


    private void setData(){
        result_table.setItems(data);
    }

    /**
     * 初始化表格数据，传入调度方案
     * */
    private void initialData(double cost,HashMap<Integer,Integer> result,double[] taskTime) {
        for(int i=1;i<=task_count;i++){
            int task_id=i;
            int truck_id=ovoc_data.truckIDs[result.get(i)];
            double task_weight=ovoc_data.taskWeights[task_id];
            double truck_capacity=ovoc_data.truckCaptitys[result.get(i)];
            double early_time=ovoc_data.earlyTime[task_id];
            double late_time=ovoc_data.lateTime[task_id];
            double task_time=taskTime[i];
            String path=ovoc_data.getPaths(ovoc_data.taskStartPlace[task_id],ovoc_data.taskEndPlace[task_id]);
            Solutions solution=new Solutions(task_id,truck_id,task_weight,truck_capacity,early_time,late_time,task_time,path);
            data.add(solution);
        }
    }

    /***
     * 把数据关联到表格上
     */
    private void configTableView(){
        task_id.setCellValueFactory(new PropertyValueFactory<Solutions, Integer>("task_id"));
        task_weight.setCellValueFactory(new PropertyValueFactory<Solutions, Double>("task_weight"));
        truck_id.setCellValueFactory(new PropertyValueFactory<Solutions, Integer>("truck_id"));
        truck_capacity.setCellValueFactory(new PropertyValueFactory<Solutions, Double>("truck_capacity"));
        early_time.setCellValueFactory(new PropertyValueFactory<Solutions, Double>("early_time"));
        late_time.setCellValueFactory(new PropertyValueFactory<Solutions, Double>("late_time"));
        task_time.setCellValueFactory(new PropertyValueFactory<Solutions, Double>("task_time"));
        task_path.setCellValueFactory(new PropertyValueFactory<Solutions, String>("task_path"));
    }

    @FXML
    void show_path(ActionEvent event) {
       String t=truck.getText();
       //弹出一个界面，展示平板车t的行驶路径，以及行驶详情。
    }


}
