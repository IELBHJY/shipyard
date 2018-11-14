package sample;

import javafx.beans.property.SimpleStringProperty;

public class Person {
    private SimpleStringProperty id;
    private SimpleStringProperty user_name;
    private SimpleStringProperty password;

    public Person(String id,String user_name, String password) {
        this.id=new SimpleStringProperty(id);
        this.user_name = new SimpleStringProperty(user_name);
        this.password = new SimpleStringProperty(password);
    }

    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public String getUser_name() {
        return user_name.get();
    }

    public SimpleStringProperty user_nameProperty() {
        return user_name;
    }

    public String getPassword() {
        return password.get();
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public void setUser_name(String user_name) {
        this.user_name.set(user_name);
    }

    public void setPassword(String password) {
        this.password.set(password);
    }
}
