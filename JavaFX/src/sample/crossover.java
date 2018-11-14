package sample;

import javafx.beans.property.SimpleStringProperty;

public class crossover {
    private SimpleStringProperty id;
    private SimpleStringProperty xcood;
    private SimpleStringProperty ycood;

    public crossover(String id,String xcood,String ycood) {
        this.id = new SimpleStringProperty(id);
        this.xcood=new SimpleStringProperty(xcood);
        this.ycood=new SimpleStringProperty(ycood);
    }

    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public String getXcood() {
        return xcood.get();
    }

    public SimpleStringProperty xcoodProperty() {
        return xcood;
    }

    public String getYcood() {
        return ycood.get();
    }

    public SimpleStringProperty ycoodProperty(){
        return ycood;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public void setXcood(String xcood) {
        this.xcood.set(xcood);
    }

    public void setYcood(String ycood) {
        this.ycood.set(ycood);
    }
}
