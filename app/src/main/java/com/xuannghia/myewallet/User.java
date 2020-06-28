package com.xuannghia.myewallet;

public class User {
    String email;
    String uuid;
    String address;

    public User() {
    }

    public User(String email, String uuid, String address) {
        this.email = email;
        this.uuid = uuid;
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
