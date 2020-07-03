package com.example.watervalve.Login;

import org.litepal.crud.LitePalSupport;

public class ScanMessage extends LitePalSupport {
    private int id;
    private String  clientid;
    private String  mqqtuser;
    private String  mqqtpwd;
    private String  mqqttip;
    private String  topic;
    private String num;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getMqqtuser() {
        return mqqtuser;
    }

    public void setMqqtuser(String mqqtuser) {
        this.mqqtuser = mqqtuser;
    }

    public String getMqqtpwd() {
        return mqqtpwd;
    }

    public void setMqqtpwd(String mqqtpwd) {
        this.mqqtpwd = mqqtpwd;
    }

    public String getMqqttip() {
        return mqqttip;
    }

    public void setMqqttip(String mqqttip) {
        this.mqqttip = mqqttip;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
