package com.example.aslib.vo;

public class Asset {
    private Double amount;
    private String app;
    private String currency;//如果是total 则为 ALL
    private String name;
    private Double price;
    private String title;
    private String token;//币地址
    private String unit;
    private boolean needUpdate;

    public Asset() {
    }

    public Asset(String app, String currency, Double amount, String name, String unit) {
        this.app = app;
        this.currency = currency;
        this.amount = amount;
        this.name = name;
        this.unit = unit;
    }


    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
