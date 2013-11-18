/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pn.pn_softphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.net.sip.SipSession;


/**
 * Listens for incoming SIP calls, intercepts and hands them off to WalkieTalkieActivity.
 */

public class IncomingCallReceiver extends BroadcastReceiver {
    public MediaPlayer mPlayer;
    static SipAudioCall incomingCall = null;
    static PnSoftPhoneActivity pnSoftPhoneActivity;

    /**
     * Processes the incoming call, answers it, and hands it over to the
     * PnSoftPhoneActivity.
     * @param context The context under which the receiver is running.
     * @param intent The intent being received.
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {




        pnSoftPhoneActivity = (PnSoftPhoneActivity) context;


        try {

            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    super.onRinging(call, caller);
                    //pnSoftPhoneActivity.showDialog("prueba onRinging");
                    //pnSoftPhoneActivity.callDialog().show();
                    showIncomingCallActivity(intent, context);

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
    public void answerCall(PnSoftPhoneActivity pnSoftPhoneActivity, SipAudioCall incomingCall){
       // mPlayer.stop();
        try{
            incomingCall.answerCall(30);
//            incomingCall.startAudio();
//            incomingCall.setSpeakerMode(false);
//            pnSoftPhoneActivity.updateStatus(incomingCall);
        }catch (Exception e) {
            if (incomingCall != null) {
                incomingCall.close();
            }
        }

    }
    public static void answerCall(){
        // mPlayer.stop();
        try{
            incomingCall.answerCall(30);
//            incomingCall.startAudio();
//            incomingCall.setSpeakerMode(false);
//            pnSoftPhoneActivity.updateStatus(incomingCall);
        }catch (Exception e) {
            if (incomingCall != null) {
                incomingCall.close();
            }
        }

    }

    public void cancelCall(PnSoftPhoneActivity pnSoftPhoneActivity, SipAudioCall call, SipProfile me, SipSession sipSession)  {
        try {
            //call.answerCall(30);
            call.endCall();
            call.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
    public static void cancelCall()  {
        try {
            if(incomingCall != null){
            incomingCall.endCall();
            incomingCall.close();

            }
            pnSoftPhoneActivity.updateStatus("Llamada finalizada.");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
   public void showIncomingCallActivity(Intent intent, Context context){
       Intent incomingCall=new Intent(context,IncomingCallGui.class);
       //context.startActivity(incomingCall);
       pnSoftPhoneActivity.startActivityForResult(incomingCall, 5);
//       Intent callIntent = new Intent(Intent.ACTION_CALL);
//       callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//       callIntent.setClass(pnSoftPhoneActivity, IncomingCallGui.class);
//
//       pnSoftPhoneActivity.startActivityForResult(callIntent,5);


    }
}
