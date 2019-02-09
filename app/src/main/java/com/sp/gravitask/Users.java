package com.sp.gravitask;

public class Users {


    private String Name;
    private String Email;
    private String Password;
    private String PhoneNumber;
    private int Points;
    private boolean taskAccepted;



    public Users() {}


    public Users(String Name, String Email, String Password, String PhoneNumber, int Points, boolean taskAccepted) {
        this.Name = Name;
        this.Email = Email;
        this.Password = Password;
        this.PhoneNumber = PhoneNumber;
        this.Points = Points;
        this.taskAccepted = taskAccepted;
    }

    public boolean isTaskAccepted() {
        return taskAccepted;
    }

    public void setTaskAccepted(boolean taskAccepted) {
        this.taskAccepted = taskAccepted;
    }

    public String getName() {
        return Name;
    }

    public String getEmail() {
        return Email;
    }

    public String getPassword() {
        return Password;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public int getPoints() {
        return Points;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public void setPoints(int points) {
        Points = points;
    }
}