package com.pn.pn_softphone;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by GDHB on 10/24/13.
 */
public class OnCallingGui extends Activity implements SensorEventListener {

    Button buttonHangCall;
    PnSoftPhoneActivity SoftPhoneActivity;

    SensorManager sensorManager;
    PowerManager pm;
    Sensor proximitySensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_calling_gui);
        MyApp myApp = (MyApp)getApplicationContext();
        SoftPhoneActivity = myApp.getSoftPhoneActivity();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);



        buttonHangCall = (Button) findViewById(R.id.buttonHangCall);

        buttonHangCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HangCall();

            }
        });
    }
    public void HangCall(){
        try {
            if(SoftPhoneActivity.call != null){
                SoftPhoneActivity.call.endCall();
                SoftPhoneActivity.call.close();
            }
            SoftPhoneActivity.updateStatus("Llamada finalizada.");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.values[0] < 5) {


           if(SoftPhoneActivity.call.isInCall()){
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
            params.screenBrightness = 0f;
            getWindow().setAttributes(params);
            buttonHangCall.setClickable(false);


            }



        }
        if(sensorEvent.values[0] >= 5 && SoftPhoneActivity.call.isInCall()) {
            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            buttonHangCall.setClickable(true);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

//    @Override
//    public void onAttachedToWindow() {
//        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
//        super.onAttachedToWindow();
//    }
}
