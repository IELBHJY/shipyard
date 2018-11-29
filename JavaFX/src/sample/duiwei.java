package sample;

import javafx.beans.property.SimpleStringProperty;

public class duiwei {
    private SimpleStringProperty id;
    private SimpleStringProperty name;
    private SimpleStringProperty xcood;
    private SimpleStringProperty ycood;

    public duiwei(String id,String name,String xcood,String ycood) {
        this.id=new SimpleStringProperty(id);
        this.name=new SimpleStringProperty(name);
        this.xcood=new SimpleStringProperty(xcood);
        this.ycood=new SimpleStringProperty(ycood);
    }

    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
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

    public SimpleStringProperty ycoodProperty() {
        return ycood;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setXcood(String xcood) {
        this.xcood.set(xcood);
    }

    public void setYcood(String ycood) {
        this.ycood.set(ycood);
    }
}
