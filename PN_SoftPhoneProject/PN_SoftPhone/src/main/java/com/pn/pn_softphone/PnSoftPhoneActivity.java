package com.pn.pn_softphone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import softphoneutils.Prueba;

import static softphoneutils.GlobalUtils.GlobalParameters.ip_address;

/**
 * Handles all calling, receiving calls, and UI interaction in the WalkieTalkie app.
 */
public class PnSoftPhoneActivity extends Activity implements SensorEventListener {
    private PnSoftPhoneActivity parentSoftPhoneActivity;


    public String sipAddress = null;

    public SipManager manager = null;
    public SipProfile me = null;
    public SipProfile peerProfile = null;
    public SipSession sipSession = null;
    public SipSession.Listener sipSessionListener = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver;

    private static final int CALL_ADDRESS = 1;
    private static final int SET_AUTH_INFO = 2;
    private static final int UPDATE_SETTINGS_DIALOG = 3;
    private static final int HANG_UP = 4;

   // public Vibrator vibrator;


    SensorManager sensorManager;
    PowerManager powerManager;
    Sensor proximitySensor;
    private PowerManager.WakeLock screenWakeLock;

    public MediaPlayer mPlayer;

    Map<Prueba, String> AsociacionMensajes;

    SharedPreferences  sharedPreferences;

    AudioManager m_amAudioManager;

    Button Hangbutton;
    Button callbutton;

    EditText editText_InsertaContacto;

    ImageView imageStatus;



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pnsoftphone);
        parentSoftPhoneActivity = this;
        try{

        MyApp myApp = ((MyApp)getApplicationContext());
        myApp.setSoftPhoneActivity(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        editText_InsertaContacto = (EditText) findViewById(R.id.llamareditText);


        sharedPreferences = getSharedPreferences("datos_login", Context.MODE_PRIVATE);
        imageStatus = (ImageView) findViewById(R.id.imageStatus);

//        mPlayer = MediaPlayer.create(this, R.raw.usringbacktone);
//        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        mPlayer.setLooping(true);
        m_amAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        m_amAudioManager.setMode(AudioManager.STREAM_MUSIC);
        m_amAudioManager.setSpeakerphoneOn(false);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        screenWakeLock = powerManager.newWakeLock(powerManager.PARTIAL_WAKE_LOCK,"screenWakeLock");
//        screenWakeLock.acquire();
//        screenWakeLock.release();

        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        final Vibrator vibrator =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Modificando el programa para que pueda llamar con botones normal y no por el toggle button.
        //Este boton termina la llamada.
        Hangbutton = (Button) findViewById(R.id.Hangbutton);
        Hangbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(100);
                if(call != null) {
                    try {
                        int state;
                        call.endCall();
                        state = call.getState();
                        if(state == 7){
                            showDialog("Cancelando la llamada!");
//                            Intent i = new Intent();
//                            i.setAction("android.SipDemo.INCOMING_CALL");
//                            sendBroadcast(i);
                        }

                        updateStatus("Llamada finalizada");
                        if(mPlayer.isPlaying()){
                            mPlayer.stop();
                        }
                    } catch (SipException se) {
                        Log.d("pnSoftPhoneActivity/onOptionsItemSelected",
                                "Error ending call.", se);
                    }
                    call.close();
                }}

        });

        callbutton = (Button) findViewById(R.id.callbutton);
        callbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText textField = (EditText) (findViewById(R.id.llamareditText));
                sipAddress = textField.getText().toString()+"@"+ ip_address;
//                SipProfile.Builder builder = null;
//                try {
//                    builder = new SipProfile.Builder(textField.getText().toString(), ip_address);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                builder.setPassword(password);
//                   builder.setPort(5060);
//
//                peerProfile = builder.build();

//                String status = "Llamando a " + textField.getText().toString();
//                updateStatus(status);
                //sipAddress = "555"+"@10.0.0.13";
                vibrator.vibrate(100);

                initiateCall();
            }});

        // Set up the intent filter.  This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);


        AsociacionMensajes = new HashMap<Prueba, String>();
        AsociacionMensajes.put(Prueba.UsuarioRegistrado, "Usuario Registrado!");
        AsociacionMensajes.put(Prueba.UsuarioExiste, "Usuario ya ha sido resgitrado anteriormente.\nFavor Comprobar credenciales.");
        AsociacionMensajes.put(Prueba.Excepcion, "Se ha producido un error.");



        /*//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, PnSoftPhoneActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(PnSoftPhoneActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());*/



        inicializar();
        showDialog("SensorName: "+proximitySensor.getName()+
        "\nSensorMaximumRange: "+proximitySensor.getMaximumRange()+
        "\nType: "+proximitySensor.getType() +
        "\nResolution: "+proximitySensor.getResolution() +
        "\nMinDelay: "+proximitySensor.getMinDelay());
    }
    private void inicializar(){
        if(!SipManager.isVoipSupported(this)){
            showDialog("El dispositivo o la version del sistema operativo no soporta VOIP.");
            return;
        }
        if(!SipManager.isApiSupported(this)){
            showDialog("El dispositivo o la version del sistema operativo no soporta el API de SIP utilizada por esta aplicación.");
            return;
        }
        if(SipManager.isSipWifiOnly(this)){
            Toast toast = Toast.makeText(this, "El dispositivo solo soporta VOIP via WIFI", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }

        String username = sharedPreferences.getString("Usuario","");
        String password = sharedPreferences.getString("Contrasena","");

        if((TextUtils.isEmpty(username) || TextUtils.isEmpty(password))){
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
        }else{
            initializeManager();
        }

        //initializeManager();
    }

    @Override
    public void onStart() {
        super.onStart();

        // When we get back from the preference setting Activity, assume
        // settings have changed, and re-login with new auth info.
        //inicializar();
        initializeManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //initializeManager();
        //initializeLocalProfile();
        mPlayer = MediaPlayer.create(this, R.raw.usringbacktone);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setLooping(true);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.close();
        }

        closeLocalProfile();

        if (callReceiver != null) {
            this.unregisterReceiver(callReceiver);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPlayer != null){
        mPlayer.release();
        mPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void initializeManager() {

        if(manager == null) {
            manager = SipManager.newInstance(this);
        }

        initializeLocalProfile();
    }

    /**
     * Logs you into your SIP provider, registering this device as the location to
     * send SIP calls to for your SIP address.
     */
    public void initializeLocalProfile() {
        if (manager == null) {
            return;
        }

        if (me != null) {
            closeLocalProfile();
        }

        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //SharedPreferences  sharedPreferences = getSharedPreferences("datos_login", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("Usuario",""); //prefs.getString("namePref", "");
        String domain = ip_address;//prefs.getString("domainPref", "");
        String password = sharedPreferences.getString("Contrasena","");//prefs.getString("passPref", "");

        if (username.length() == 0 || domain.length() == 0 || password.length() == 0) {
            //showDialog(UPDATE_SETTINGS_DIALOG);
            return;
        }

        try {
            SipProfile.Builder builder = new SipProfile.Builder(username, domain);
            builder.setPassword(password);
            //   builder.setPort(5060);

            me = builder.build();

            Intent i = new Intent();
            i.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);
            manager.open(me, pi, null);


            // This listener must be added AFTER manager.open is called,
            // Otherwise the methods aren't guaranteed to fire.

            manager.setRegistrationListener(me.getUriString(), new SipRegistrationListener() {


                public void onRegistering(String localProfileUri) {
                    //updateStatus("Registrando con el servidor SIP...");
                    try{
                    parentSoftPhoneActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int id = getResources().getIdentifier("com.pn.pn_softphone:drawable/ic_status_connecting", null, null);
                            imageStatus.setBackgroundResource(id);
                        }
                    });
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    //updateStatus("Conectado al servidor SIP.");
                    try{

                    parentSoftPhoneActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int id = getResources().getIdentifier("com.pn.pn_softphone:drawable/ic_status_online", null, null);
                            imageStatus.setBackgroundResource(id);
                            //editText_InsertaContacto.setClickable(true);
                            callbutton.setActivated(true);
                            Hangbutton.setActivated(true);
                            callbutton.setClickable(true);
                            Hangbutton.setClickable(true);
                        }
                    });

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }



                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    //updateStatus("Fallo el registro. Por favor verifica las configuraciones.");
                    try{
                    parentSoftPhoneActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int id = getResources().getIdentifier("com.pn.pn_softphone:drawable/ic_status_reg_fail", null, null);
                            imageStatus.setBackgroundResource(id);
                            //editText_InsertaContacto.setActivated(false);
                            //editText_InsertaContacto.setClickable(false);
                            callbutton.setActivated(false);
                            Hangbutton.setActivated(false);
                            callbutton.setClickable(false);
                            Hangbutton.setClickable(false);
//                            Toast toast = Toast.makeText(parentSoftPhoneActivity.getBaseContext(), "Fallo el registro. Por favor verifica las configuraciones.", Toast.LENGTH_LONG);
//                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//                            toast.show();
                        }
                    });
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }







                }
            });
        } catch (ParseException pe) {
            updateStatus("Connection Error.");
        } catch (SipException se) {
            updateStatus("Connection error.");
        }
    }

    /**
     * Closes out your local profile, freeing associated objects into memory
     * and unregistering your device from the server.
     */
    public void closeLocalProfile() {
        if (manager == null) {
            return;
        }
        try {
            if (me != null) {
                manager.close(me.getUriString());
            }
        } catch (Exception ee) {
            Log.d("WalkieTalkieActivity/onDestroy", "Failed to close local profile.", ee);
        }
    }

    /**
     * Make an outgoing call.
     */
    public void initiateCall() {
        if(TextUtils.isEmpty(editText_InsertaContacto.getText().toString())){
            return;
        }
        //updateStatus(sipAddress);

        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners.  Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is established.
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    if(mPlayer != null){
                    if(mPlayer.isPlaying()){
                    mPlayer.stop();
                        mPlayer.stop();
                        mPlayer.release();
                        mPlayer = null;
                    }
                    }
                    call.startAudio();
                    call.setSpeakerMode(false);
                    EditText textField = (EditText) (findViewById(R.id.llamareditText));
                    // call.toggleMute();  // esto es lo que hace que sea toggle con el boton.
                    updateStatus("Hablando con " + textField.getText().toString());

                    super.onCallEstablished(call);
                }



                @Override
                public void onCallBusy(SipAudioCall call) {
                    super.onCallBusy(call);
                }

                @Override
                public void onCallHeld(SipAudioCall call) {
                    super.onCallHeld(call);
                }

                @Override
                public void onReadyToCall(SipAudioCall call) {
                    super.onReadyToCall(call);
                }

                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    super.onRinging(call, caller);
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    if(mPlayer != null){
                    if (mPlayer.isPlaying()){
                        mPlayer.stop();
                        mPlayer.release();
                        mPlayer = null;

                    }
                    }
                    updateStatus("Llamada finalizada");//("Ready.");

                    parentSoftPhoneActivity.finishActivity(HANG_UP);


                    super.onCallEnded(call);
                }

                @Override
                public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                    //super.onError(call, errorCode, errorMessage);
                    int state = call.getState();
                    if(errorCode == -2){
                        if(call != null){
                            call.close();
                            parentSoftPhoneActivity.finishActivity(HANG_UP);
                            updateStatus("Llamada finalizada.");
                        }
                    }
                    //showDialog("Error code: "+errorCode +", Error Message: "+errorMessage);
                    parentSoftPhoneActivity.finishActivity(HANG_UP);
                    super.onError(call, errorCode,errorMessage);
                }

                @Override
                public void onCalling(SipAudioCall call) {
                EditText textField = (EditText) (findViewById(R.id.llamareditText));
                String status = "Llamando a " + textField.getText().toString();
                updateStatus(status);
//                    int state = call.getState();
//                    if(state == 5){
//                    Intent i = new Intent(parentSoftPhoneActivity, OnCallingGui.class);
//                    startActivityForResult(i,HANG_UP);
//                    }

//                    Intent i = new Intent(parentSoftPhoneActivity, OnCallingGui.class);
//                    Bundle bundle = new Bundle(3);
//                    bundle.putString("PeerUser", status);
//                    //bundle.pu
//                    i.putExtra("PeerUser", status);
//                    //i.put

                super.onCalling(call);
                }

                @Override
                public void onRingingBack(SipAudioCall call) {
                    mPlayer = MediaPlayer.create(parentSoftPhoneActivity, R.raw.usringbacktone);
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.setLooping(true);
                    mPlayer.start();
                    //super.onRingingBack(call);
                    //showDialog("prueba onRingingBack");
                    super.onRingingBack(call);
                }

                @Override
                public void onChanged(SipAudioCall call) {
                    //showDialog("prueba onChanged");
                    super.onChanged(call);
                }
            };

            SipSession.Listener sipSessionListener = new SipSession.Listener(){

            };

            call = manager.makeAudioCall(me.getUriString(), sipAddress, null, 30);
            call.setListener(listener, true);
            Intent i = new Intent(parentSoftPhoneActivity, OnCallingGui.class);
            parentSoftPhoneActivity.startActivityForResult(i,HANG_UP);
            //call = manager.makeAudioCall(me, peerProfile, listener, 30);
//            sipSession = manager.createSipSession(me,sipSessionListener );
//            call.attachCall(sipSession, "llamada random");

        }
        catch (Exception e) {
            Log.i("WalkieTalkieActivity/InitiateCall", "Error when trying to close manager.", e);
            if (me != null) {
                try {
                    manager.close(me.getUriString());
                } catch (Exception ee) {
                    Log.i("WalkieTalkieActivity/InitiateCall",
                            "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
    }


    /**
     * Updates the status box at the top of the UI with a messege of your choice.
     * @param status The String to display in the status box.
     */
    public void updateStatus(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        this.runOnUiThread(new Runnable() {
            public void run() {
                TextView labelView = (TextView) findViewById(R.id.sipLabel);
                labelView.setText(status);
            }
        });
    }

    /**
     * Updates the status box with the SIP address of the current call.
     * @param call The current, active call.
     */
    public void updateStatus(SipAudioCall call) {
        String useName = call.getPeerProfile().getDisplayName();
        if(useName == null) {
            useName = call.getPeerProfile().getUserName();
        }
        updateStatus("Hablando con "+useName /*+ "@" + call.getPeerProfile().getSipDomain()*/);
    }

//    /**
//     * Updates whether or not the user's voice is muted, depending on whether the button is pressed.
//     * @param v The View where the touch event is being fired.
//     * @param event The motion to act on.
//     * @return boolean Returns false to indicate that the parent view should handle the touch event
//     * as it normally would.
//     */
//    public boolean onTouch(View v, MotionEvent event) {
//        if (call == null) {
//            return false;
//        } else if (event.getAction() == MotionEvent.ACTION_DOWN && call != null && call.isMuted()) {
//            call.toggleMute();
//        } else if (event.getAction() == MotionEvent.ACTION_UP && !call.isMuted()) {
//            call.toggleMute();
//        }
//        return false;
//    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, CALL_ADDRESS, 0, "Llamar");
        menu.add(0, SET_AUTH_INFO, 0, "Editar las configuraciones del SIP");
        menu.add(0, HANG_UP, 0, "Terminar Llamada");

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CALL_ADDRESS:
                showDialog(CALL_ADDRESS);
                break;
            case SET_AUTH_INFO:
                updatePreferences();
                break;
            case HANG_UP:
                if(call != null) {
                    try {
                        call.endCall();
                    } catch (SipException se) {
                        Log.d("WalkieTalkieActivity/onOptionsItemSelected",
                                "Error ending call.", se);
                    }
                    call.close();
                }
                break;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CALL_ADDRESS:

                LayoutInflater factory = LayoutInflater.from(this);
                final View textBoxView = factory.inflate(R.layout.call_address_dialog, null);
                return new AlertDialog.Builder(this)
                        .setTitle("Llamar")
                        .setView(textBoxView)
                        .setPositiveButton(
                                android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EditText textField = (EditText)
                                        (textBoxView.findViewById(R.id.calladdress_edit));
                                sipAddress = textField.getText().toString()+ ip_address;
                                initiateCall();

                            }
                        })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Noop.
                            }
                        })
                        .create();

            case UPDATE_SETTINGS_DIALOG:
                return new AlertDialog.Builder(this)
                        .setMessage("Please update your SIP Account Settings.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                updatePreferences();
                            }
                        })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Noop.
                            }
                        })
                        .create();
        }
        return null;
    }


    public void updatePreferences() {
        Intent settingsActivity = new Intent(getBaseContext(),SipSettings.class);
        startActivity(settingsActivity);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.values[0] < 10) {
//            PowerManager.WakeLock screenWakeLock;
//
//           PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//            screenWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//                    "screenWakeLock");
//            screenWakeLock.acquire();
//
//            Window mywindow = getWindow();
//
//            WindowManager.LayoutParams lp = mywindow.getAttributes();
//
//            lp.screenBrightness = 0.0f;
//
//            mywindow.setAttributes(lp);
//            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//            screenWakeLock = powerManager.newWakeLock(powerManager.PARTIAL_WAKE_LOCK,"screenWakeLock");
//            screenWakeLock.acquire();
//            if(call != null){
//            WindowManager.LayoutParams params = getWindow().getAttributes();
//            params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//            params.screenBrightness = 0f;
//            getWindow().setAttributes(params);
//            }
        }
//        else if(screenWakeLock != null){
//
//            if(screenWakeLock.isHeld()){
//                screenWakeLock.release();
//            }
//            screenWakeLock = null;
//        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public void showDialog(String s){
        AlertDialog.Builder randowDialog = new AlertDialog.Builder(this);
        randowDialog.setTitle("Aviso");
        randowDialog.setMessage(s);
        randowDialog.setPositiveButton("OK",null);
        randowDialog.create();
        randowDialog.show();


    }

    public Dialog callDialog(){
        String Username =null;
        if(Username == null) {
            Username = call.getPeerProfile().getUserName();
        }

        return new AlertDialog.Builder(this)

                .setTitle("Llamada Entrante")
                .setMessage("Llamada entrante de " + Username)
                .setPositiveButton("Responder", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        answerCall();
                    }
                })
                .setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                closeCall();
            }

            })
                .create();



    }

    public void showCallDialog() {
        String Username =null;
        if(Username == null) {
            Username = call.getPeerProfile().getUserName();
        }
        AlertDialog.Builder DialogoLlamada = new AlertDialog.Builder(this);
        DialogoLlamada.setTitle("Llamada Entrante");
        DialogoLlamada.setMessage("Llamada entrante de " + Username);
        DialogoLlamada.setCancelable(false);

        DialogoLlamada.setPositiveButton("Responder", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                answerCall();
            }
        });
        DialogoLlamada.setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                closeCall();

            }
        });
        DialogoLlamada.show();

    }
    public void startRinging(){

        mPlayer.start();
    }
    public void stopRinging(){
        mPlayer.stop();
    }

    private void closeCall() {

        callReceiver.cancelCall(this, call, me, sipSession);


//
    }

    private void answerCall() {
        callReceiver.answerCall(this, call);
    }
}

