package softphoneutils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.pn.pn_softphone.IncomingCallGui;
import com.pn.pn_softphone.MyApp;
import com.pn.pn_softphone.PnSoftPhoneActivity;

/**
 * Created by GDHB on 11/08/13.
 */
public class SoftPhoneService extends Service {
    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";

    private CallBr br_call;
    private Listener mListener = null;
    private int cont;

    PnSoftPhoneActivity pnSoftPhoneActivity;

    @Override
    public void onCreate() {
        Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
        Log.d("SoftPhone Service", "onCreate");
        MyApp myApp = ((MyApp)getApplicationContext());
        pnSoftPhoneActivity = myApp.getSoftPhoneActivity();


        super.onCreate();
    }



    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        Log.d("SoftPhone Started", "onStart");
    }


    public interface Listener{
        public void onStateChange(int cont);
    }
    public void registerListener (Listener listener) {
        mListener = listener;
        cont = 0;
    }
    public void addCont()
    {
        cont++;
        if(mListener != null)
            mListener.onStateChange(cont);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        Log.d("SoftPhone Service", "onDestroy");
    }


    @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        filter.addAction("android.SipDemo.INCOMING_CALL");
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);
        return (START_STICKY);
   }

    public class CallBr extends BroadcastReceiver
    {
        SipAudioCall incomingCall = null;
        //PnSoftPhoneActivity pnSoftPhoneActivity;


        @Override
        public void onReceive(Context context, Intent intent)
        {

            if (intent.getAction().equals("android.SipDemo.INCOMING_CALL")){
                Toast.makeText(context, "INCOMING", Toast.LENGTH_LONG).show();
                final Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callIntent.setClass(context, IncomingCallGui.class);
                startActivity(callIntent);

                //        Intent incomingCall=new Intent(pnSoftPhoneActivity,IncomingCallGui.class);
//        //context.startActivity(incomingCall);

//
//                pnSoftPhoneActivity.startActivityForResult(callIntent,5);

                //pnSoftPhoneActivity = (PnSoftPhoneActivity) context;



                try {

                    SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                        @Override
                        public void onRinging(SipAudioCall call, SipProfile caller) {
                            super.onRinging(call, caller);
                            //pnSoftPhoneActivity.showDialog("prueba onRinging");
                            //pnSoftPhoneActivity.callDialog().show();
                            //showIncomingCallActivity();
                            //pnSoftPhoneActivity.startActivityForResult(callIntent, 5);
                            startActivity(callIntent);

                            try {
//                        pnSoftPhoneActivity.call = call;
//                        pnSoftPhoneActivity.startRinging();
//                        pnSoftPhoneActivity.call.answerCall(30);
                                //pnSoftPhoneActivity.showCallDialog();

                                //call.answerCall(30);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onCallEstablished(SipAudioCall call) {

                            call.startAudio();
                            call.setSpeakerMode(false);
                            pnSoftPhoneActivity.updateStatus(call);
                            super.onCallEstablished(call);



                        }

                        @Override
                        public void onCallEnded(SipAudioCall call) {
                            pnSoftPhoneActivity.updateStatus("Llamada finalizada.");
                            //pnSoftPhoneActivity.callDialog().dismiss();
                            pnSoftPhoneActivity.finishActivity(5);

                            super.onCallEnded(call);

                        }



                        @Override
                        public void onError(SipAudioCall call, int errorCode, String errorMessage) {

                            //pnSoftPhoneActivity.showDialog(errorMessage);
                            pnSoftPhoneActivity.finishActivity(5);
                            super.onError(call, errorCode, errorMessage);

                        }

                        @Override
                        public void onCalling(SipAudioCall call) {
                            //pnSoftPhoneActivity.showDialog("prueba onCalling");
                            super.onCalling(call);
                        }

                        @Override
                        public void onChanged(SipAudioCall call) {
                            //pnSoftPhoneActivity.showDialog("prueba onChanged");
                            super.onChanged(call);
                        }

                        @Override
                        public void onReadyToCall(SipAudioCall call) {
                            //pnSoftPhoneActivity.showDialog("prueba onReadyCall");
                            super.onReadyToCall(call);
                        }

                        @Override
                        public void onCallBusy(SipAudioCall call) {
                            //pnSoftPhoneActivity.showDialog("prueba onCallBusy");
                            super.onCallBusy(call);
                        }

                        @Override
                        public void onCallHeld(SipAudioCall call) {
                            //pnSoftPhoneActivity.showDialog("prueba onCallHeld");
                            super.onCallHeld(call);
                        }

                        @Override
                        public void onRingingBack(SipAudioCall call) {
                            //pnSoftPhoneActivity.showDialog("prueba onRingingBack");
                            super.onRingingBack(call);
                        }



                    };


//            mPlayer = MediaPlayer.create(context, R.raw.telephone);
//            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mPlayer.setLooping(true);
//            mPlayer.start();
                    // if(pnSoftPhoneActivity.manager.isIncomingCallIntent(intent)){
                    //incomingCall = pnSoftPhoneActivity.manager.takeAudioCall(intent, listener);
                    //set(incomingCall,listener, intent);
                    incomingCall = pnSoftPhoneActivity.manager.takeAudioCall(intent, null);
                    incomingCall.setListener(listener, true);



//
//            incomingCall.answerCall(30);
//            incomingCall.startAudio();
//            incomingCall.setSpeakerMode(false);

                    pnSoftPhoneActivity.call = incomingCall;


//                Notification not;
//
//                Notification.Builder notBuilder = new Notification.Builder(context);
//                notBuilder.setContentTitle("Llamada Entrante");
//
//                not = notBuilder.build();
//
//                NotificationManager nm = (NotificationManager)
//                        context.getSystemService(Context.NOTIFICATION_SERVICE);
//                nm.notify("Llamda entrante",2,not);

                    //pnSoftPhoneActivity.callDialog().show();
                    //}
                    //pnSoftPhoneActivity.call.answerCall(30);
                    //pnSoftPhoneActivity.updateStatus(incomingCall);


                    //pnSoftPhoneActivity.call = incomingCall;

                    //pnSoftPhoneActivity.showCallDialog();

//            incomingCall = wtActivity.manager.takeAudioCall(intent, listener);
//            incomingCall.answerCall(30);
//            incomingCall.startAudio();
//            incomingCall.setSpeakerMode(false);
////            if(incomingCall.isMuted()) {
////                incomingCall.toggleMute();
////            }
//
//            wtActivity.call = incomingCall;
//
//            wtActivity.updateStatus(incomingCall);

                } catch (Exception e) {
                    if (incomingCall != null) {
                        incomingCall.close();
                    }
                }

            }

            else if (intent.getAction().equals(ACTION_OUT))
                Toast.makeText(context, "OUTGOING", Toast.LENGTH_LONG).show();
        }
        public void showIncomingCallActivity(){
//        Intent incomingCall=new Intent(pnSoftPhoneActivity,IncomingCallGui.class);
//        //context.startActivity(incomingCall);
//        pnSoftPhoneActivity.startActivityForResult(incomingCall, 5);
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.setClass(pnSoftPhoneActivity, IncomingCallGui.class);

            pnSoftPhoneActivity.startActivityForResult(callIntent,5);
        }

    }

    public void set(SipAudioCall incomingCall, SipAudioCall.Listener listener, Intent intent){
        try {
            incomingCall = pnSoftPhoneActivity.manager.takeAudioCall(intent, null);
        } catch (SipException e) {
            e.printStackTrace();
        }
        incomingCall.setListener(listener, true);




        pnSoftPhoneActivity.call = incomingCall;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
