package com.example.android.staysaferesq;

public class UserSafe {
    String phone,flag;
    double lat,lon;

    UserSafe(String p,String f,double l1,double l2){
        phone=p;
        flag=f;
        lat=l1;
        lon=l2;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getFlag() {
        return flag;
    }

    public String getPhone() {
        return phone;
    }
}
