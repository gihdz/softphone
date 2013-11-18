package com.pn.pn_softphone;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by GDHB on 10/21/13.
 */
public class IncomingCallGui extends Activity implements SensorEventListener {

    PnSoftPhoneActivity SoftPhoneActivity;

    SensorManager sensorManager;
    PowerManager pm;
    Sensor proximitySensor;
    private PowerManager.WakeLock screenWakeLock;

    Button buttonAnswer;
    Button buttonReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_gui);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        MyApp myApp = (MyApp)getApplicationContext();
        SoftPhoneActivity = myApp.getSoftPhoneActivity();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        screenWakeLock = powerManager.newWakeLock(powerManager.PARTIAL_WAKE_LOCK,"screenWakeLock");
//        screenWakeLock.acquire();
//        screenWakeLock.release();

        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);

        buttonAnswer = (Button) findViewById(R.id.buttonAnswer);
        buttonReject = (Button) findViewById(R.id.buttonReject);

        buttonAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCallAcceptButton(view);
            }
        });

        buttonReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCallRejectButton(view);
            }
        });
    }
    public void onCallAcceptButton(View view){
        buttonAnswer.setClickable(false);
        //IncomingCallReceiver.answerCall();
        SoftPhoneActivity.answerCallBeta();

    }
    public void onCallRejectButton(View view){
        //IncomingCallReceiver.cancelCall();
        SoftPhoneActivity.closeCallBeta();
        this.finish();
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
            buttonReject.setClickable(false);


            }



        }
        if(sensorEvent.values[0] >= 5 && SoftPhoneActivity.call.isInCall()) {
            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            buttonReject.setClickable(true);

        }





    }
//    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
//        int childCount = viewGroup.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View view = viewGroup.getChildAt(i);
//            view.setEnabled(enabled);
//
//            if (view instanceof ViewGroup) {
//                enableDisableViewGroup((ViewGroup) view, enabled);
//            }
//        }
//    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //return super.onKeyDown(keyCode, event);
        return true;
    }
}
