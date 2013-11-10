package softphoneutils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by GDHB on 11/08/13.
 */
public class SoftPhoneService extends Service {
    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";

    private CallBr br_call;
    private Listener mListener = null;
    private int cont;
    @Override
    public void onCreate() {
        Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
        Log.d("SoftPhone Service", "onCreate");
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
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);
        return (START_STICKY);
    }
    public class CallBr extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(ACTION_IN))
                Toast.makeText(context, "INCOMING", Toast.LENGTH_LONG).show();
            else if (intent.getAction().equals(ACTION_OUT))
                Toast.makeText(context, "OUTGOING", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
