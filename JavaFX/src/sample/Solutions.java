package sample;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Solutions {

    private SimpleIntegerProperty task_id;
    private SimpleIntegerProperty truck_id;
    private SimpleDoubleProperty task_weight;
    private SimpleDoubleProperty truck_capacity;
    private SimpleDoubleProperty early_time;
    private SimpleDoubleProperty late_time;
    private SimpleDoubleProperty task_time;
    private SimpleStringProperty task_path;

    public Solutions(int task_id,int truck_id,double task_weight,double truck_capacity,
                     double early_time,double late_time,double task_time,String task_path) {
        this.task_id=new SimpleIntegerProperty(task_id);
        this.truck_id=new SimpleIntegerProperty(truck_id);
        this.task_weight=new SimpleDoubleProperty(task_weight);
        this.truck_capacity=new SimpleDoubleProperty(truck_capacity);
        this.early_time=new SimpleDoubleProperty(early_time);
        this.late_time=new SimpleDoubleProperty(late_time);
        this.task_time=new SimpleDoubleProperty(task_time);
        this.task_path=new SimpleStringProperty(task_path);
    }

    public int getTask_id() {
        return task_id.get();
    }

    public SimpleIntegerProperty task_idProperty() {
        return task_id;
    }

    public int getTruck_id() {
        return truck_id.get();
    }

    public SimpleIntegerProperty truck_idProperty() {
        return truck_id;
    }

    public double getTask_weight() {
        return task_weight.get();
    }

    public SimpleDoubleProperty task_weightProperty() {
        return task_weight;
    }

    public double getTruck_capacity() {
        return truck_capacity.get();
    }

    public SimpleDoubleProperty truck_capacityProperty() {
        return truck_capacity;
    }

    public double getEarly_time() {
        return early_time.get();
    }

    public SimpleDoubleProperty early_timeProperty() {
        return early_time;
    }

    public double getLate_time() {
        return late_time.get();
    }

    public SimpleDoubleProperty late_timeProperty() {
        return late_time;
    }

    public double getTask_time() {
        return task_time.get();
    }

    public SimpleDoubleProperty task_timeProperty() {
        return task_time;
    }

    public String getTask_path() {
        return task_path.get();
    }

    public SimpleStringProperty task_pathProperty() {
        return task_path;
    }

    public void setTask_id(int task_id) {
        this.task_id.set(task_id);
    }

    public void setTruck_id(int truck_id) {
        this.truck_id.set(truck_id);
    }

    public void setTask_weight(double task_weight) {
        this.task_weight.set(task_weight);
    }

    public void setTruck_capacity(double truck_capacity) {
        this.truck_capacity.set(truck_capacity);
    }

    public void setEarly_time(double early_time) {
        this.early_time.set(early_time);
    }

    public void setLate_time(double late_time) {
        this.late_time.set(late_time);
    }

    public void setTask_time(double task_time) {
        this.task_time.set(task_time);
    }

    public void setTask_path(String task_path) {
        this.task_path.set(task_path);
    }
}
