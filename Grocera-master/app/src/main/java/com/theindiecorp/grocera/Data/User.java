package com.theindiecorp.grocera.Data;


public class User {
    private String name;
    private String id;
    private String email;
    private String pinCode;
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public User(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String name, String phone, String userId, String email, String pinCode){
        this.name = name;
        this.phone = phone;
        this.id = userId;
        this.email = email;
        this.pinCode = pinCode;
    }

    public User(String name,String phone,String id) {
        this.id = id;
        this.phone = phone;
        this.name = name;
    }
}
