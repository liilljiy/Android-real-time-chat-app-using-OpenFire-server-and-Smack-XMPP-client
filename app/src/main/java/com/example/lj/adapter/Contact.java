package com.example.lj.adapter;

public class Contact {
    private String name;
    private String account;
    private String status;

    public Contact(String name, String account, String status) {
        this.name = name;
        this.account = account;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getAccount() {
        return account;
    }

    public String getStatus() {
        return status;
    }
}
