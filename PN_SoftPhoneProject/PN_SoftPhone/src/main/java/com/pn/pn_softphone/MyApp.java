package com.pn.pn_softphone;

import android.app.Application;

public class MyApp extends Application {
    private String myState;
    private PnSoftPhoneActivity SoftPhoneActivity;

    public void setSoftPhoneActivity(PnSoftPhoneActivity SoftPhoneActivity){
        this.SoftPhoneActivity = SoftPhoneActivity;
    }
    public PnSoftPhoneActivity getSoftPhoneActivity(){
        return SoftPhoneActivity;
    }

    public String getState(){
        return myState;
    }
    public void setState(String s){
        myState = s;
    }

}
