package com.example.indeedgambling;

public class Entrant extends Profile{

    private String realName;
    private String email;
    private String phoneNum;

    public Entrant(String password, String profileName) {
        super(password, profileName);
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
