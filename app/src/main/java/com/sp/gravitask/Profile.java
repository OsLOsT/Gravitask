package com.sp.gravitask;

public class Profile {

    String profileName;
    String email;
    String password;

    public Profile(String profileName, String email, String password) {
        this.profileName = profileName;
        this.email = email;
        this.password = password;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}
