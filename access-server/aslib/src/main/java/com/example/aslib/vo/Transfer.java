package com.example.aslib.vo;

import java.util.Date;
import java.util.Objects;

public class Transfer {
    private String deviceId;
    private Double amount;
    private String app;
    private String balance;
    private String currency;
    private boolean isSubmitted;
    private String net;
    private String receiver;
    private int result;
    private String sender;
    private String uuid;
    private String walletName;//钱包名字
    private Date submitTime;

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
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

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }


    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return Objects.equals(amount, transfer.amount) && Objects.equals(receiver, transfer.receiver) && Objects.equals(receiver, transfer.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, receiver, sender);
    }
}
