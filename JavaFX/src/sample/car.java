package sample;

import javafx.beans.property.SimpleStringProperty;

public class car {
    private SimpleStringProperty id;
    private SimpleStringProperty type;
    private SimpleStringProperty carry_weight;

    public car(String id,String type,String carry_weight){
        this.id=new SimpleStringProperty(id);
        this.type=new SimpleStringProperty(type);
        this.carry_weight=new SimpleStringProperty(carry_weight);
    }

    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public String getCarry_weight() {
        return carry_weight.get();
    }

    public SimpleStringProperty carry_weightProperty() {
        return carry_weight;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public void setCarry_weight(String carry_weight) {
        this.carry_weight.set(carry_weight);
    }
}
