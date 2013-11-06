package com.pn.pn_softphone;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.HashMap;
import java.util.Map;

import softphoneutils.GlobalUtils;
import softphoneutils.Prueba;
import softphoneutils.SecurityUtil;

import static softphoneutils.GlobalUtils.GlobalParameters.OPERATION_NAME_LOGINCOP;
import static softphoneutils.GlobalUtils.GlobalParameters.OPERATION_NAME_VERIFYIMEI;
import static softphoneutils.GlobalUtils.GlobalParameters.SOAP_ACTION_LOGINCOP;
import static softphoneutils.GlobalUtils.GlobalParameters.SOAP_ACTION_VERIFYIMEI;
import static softphoneutils.GlobalUtils.GlobalParameters.SOAP_ADDRESS;
import static softphoneutils.GlobalUtils.GlobalParameters.WSDL_TARGET_NAMESPACE;

public class LoginActivity extends Activity implements SensorEventListener {
    private View LoginForm;
    private View LoginFormStatus;
    TextView Login_Status_Message;

    SecurityUtil securityUtil;

    SharedPreferences sharedPreferences;

    public TelephonyManager telephonyManager;

    //private GlobalUtils.GlobalParameters globalParameters = new GlobalUtils.GlobalParameters();
    private GlobalUtils.EmailValidator emailValidator = new GlobalUtils.EmailValidator();


    public static String aIMEI;



    EditText editText_Email;
    EditText editText_Contrasena;

    Button button_Query;
    Button button_Login;

    String Email;
    String Contrasena;

    SensorManager sensorManager;
    Sensor proximitySensor;

    Map<Prueba, String> AsociacionMensajes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        securityUtil = new SecurityUtil();


        this.setFinishOnTouchOutside(false);

        sharedPreferences = getSharedPreferences("datos_login",Context.MODE_PRIVATE);

//        aIMEI = "131231232323";
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("IMEI",aIMEI);
//        editor.commit();
        getIMEI();


        editText_Email = (EditText) findViewById(R.id.editTextEmail);
        editText_Email.setText(sharedPreferences.getString("Email",""));


        editText_Contrasena = (EditText) findViewById(R.id.editTextContrasena);
        editText_Contrasena.setText(sharedPreferences.getString("Contrasena",""));


        button_Query = (Button) findViewById(R.id.buttonQuery);
        button_Login = (Button) findViewById(R.id.buttonLogin);

        LoginForm = findViewById(R.id.login_table2);
        LoginFormStatus = findViewById(R.id.login_status);
        Login_Status_Message = (TextView) findViewById(R.id.login_status_message2);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Almacena los valores para comprobar si los edit_text contienen valor.
        Email = editText_Email.getText().toString();
        Contrasena = editText_Contrasena.getText().toString();

        AsociacionMensajes = new HashMap<Prueba, String>();
        AsociacionMensajes.put(Prueba.Excepcion, "El inicio de sesion ha fallado, intente de nuevo mas tarde!");
        AsociacionMensajes.put(Prueba.InicioDenegado, "Usuario no registrado! Proceda a registro.");

        if(!(TextUtils.isEmpty(Email) && TextUtils.isEmpty(Contrasena))){
            intentarLogin();
        }


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        if(sensorEvent.values[0] < 10) {
//        Toast toast = Toast.makeText(this, "Probando el sensor de proximidad.", Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//        toast.show();
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }




    /**
     * Para verificar los parametros requerido para logear al usuario.
     * @param view
     */
    public void LoginPolicia(View view){
        intentarLogin();
    }
    public void LlamarRegistroPoliciaActivity(){
        Intent intent = new Intent(this, RegistroActivity.class);
        startActivity(intent);
    }
    /**
     * Llama el activity para para registro de usuario en la base de datos.
     * @param view
     */
    public void preLlamarRegistroPoliciaActivity(View view){
        //VerificarIMEI();
        Intent intent = new Intent(this, RegistroActivity.class);
        startActivity(intent);
    }
    private void llamarSoftPhoneActivity() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Email = editText_Email.getText().toString();
        int atIndex = 0;
        atIndex = Email.indexOf('@');
        String Username = Email.substring(0,atIndex);

        editor.putString("Usuario", Username);
        editor.putString("Email", Email);
        editor.putString("Contrasena", editText_Contrasena.getText().toString());
        editor.putString("IMEI",aIMEI);
        editor.commit();
        //Esto es para que no haga un loop luego de hacer login.
        finish();

        //Intent intent = new Intent(this, PnSoftPhoneActivity.class);
      //  startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    /**
     * Para verificar el usuario regitrado.
     */
    private void intentarLogin() {

        // Resetea los errores
        editText_Email.setError(null);
        editText_Contrasena.setError(null);

        // Almacena los valores para comprobar si los edit_text contienen valor.
        Email = editText_Email.getText().toString();
        Contrasena = editText_Contrasena.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Para verificar contraseña.
        if(TextUtils.isEmpty(Contrasena)){
            editText_Contrasena.setError(getString(R.string.error_campo_requerido));
            focusView = editText_Contrasena;
            cancel = true;
        } else if(Contrasena.length() < 5){
            editText_Contrasena.setError(getString(R.string.error_contrasena_tamano));
            focusView = editText_Contrasena;
            cancel = true;
        }
        // Para verificar Email
        if (TextUtils.isEmpty(Email)) {
            editText_Email.setError(getString(R.string.error_campo_requerido));
            focusView = editText_Email;
            cancel = true;
        } else if (!emailValidator.validateEmail(Email)) {
            editText_Email.setError(getString(R.string.error_email_invalido));
            focusView = editText_Email;
            cancel = true;
        }

        if(cancel){
            // Si hubo error en los campos anteriores, no se registrará el usuario.
            focusView.requestFocus();
        } else{
            Login_Status_Message.setText(R.string.status_login);
            showProgress(true);
            InputMethodManager imm =
                    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText_Email.getWindowToken(), 0);
            new AsyncLoginPolicia().execute();
        }



    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            LoginFormStatus.setVisibility(View.VISIBLE);
            LoginFormStatus.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            LoginFormStatus.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            LoginForm.setVisibility(View.VISIBLE);
            LoginForm.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            LoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            LoginFormStatus.setVisibility(show ? View.VISIBLE : View.GONE);
            LoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Verifica si el IMEI del dispositivo esta ya registrado.
     */
    private void VerificarIMEI(){

        new AsyncVerificarIMEI().execute();
    }
    public  class AsyncVerificarIMEI extends  AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean IMEI_Availability = false;
            SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME_VERIFYIMEI);

            PropertyInfo property_IMEI = new PropertyInfo();
            property_IMEI.type = PropertyInfo.STRING_CLASS;
            property_IMEI.name = "IMEI";

            request.addPropertyIfValue(property_IMEI, aIMEI);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);


            HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);

            try  {
                httpTransport.call(SOAP_ACTION_VERIFYIMEI, envelope);
                IMEI_Availability = Boolean.valueOf(envelope.getResponse().toString());

            }  catch (Exception exception)   {
                exception.printStackTrace();
            }
            return IMEI_Availability;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            if (aBoolean){
                showSimpleDialog("Este dispositivo ya ha sido registrado!");

            }
            else{
                LlamarRegistroPoliciaActivity();

            }
            super.onPostExecute(aBoolean);
        }
    }

    /**
     * Logeo de Policias
     */
    public class AsyncLoginPolicia extends AsyncTask<Void,Void,Prueba>{

        @Override
        protected Prueba doInBackground(Void... voids) {

            Prueba respuesta;
            String encryptedPass = "";
            String encryptedEmail = "";

            SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME_LOGINCOP);

            PropertyInfo property_Email = new PropertyInfo();
            property_Email.type = PropertyInfo.STRING_CLASS;
            property_Email.name = "Email";

            PropertyInfo property_Contrasena = new PropertyInfo();
            property_Contrasena.type = PropertyInfo.STRING_CLASS;
            property_Contrasena.name = "Password";

            PropertyInfo property_IMEI = new PropertyInfo();
            property_IMEI.type = PropertyInfo.STRING_CLASS;
            property_IMEI.name = "IMEI";



            Email = editText_Email.getText().toString();
            Contrasena = editText_Contrasena.getText().toString();

            try {
                encryptedPass = securityUtil.encrypt(Contrasena, aIMEI);
                encryptedEmail = securityUtil.encrypt(Email, aIMEI);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //request.addPropertyIfValue(property_Email, Email);
            //request.addPropertyIfValue(property_Contrasena, Contrasena);
            request.addPropertyIfValue(property_Email, encryptedEmail);
            request.addPropertyIfValue(property_Contrasena, encryptedPass);
            request.addPropertyIfValue(property_IMEI, aIMEI);



            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);


            HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);

            try  {
                httpTransport.call(SOAP_ACTION_LOGINCOP, envelope);
                respuesta = Prueba.valueOf(envelope.getResponse().toString());

            }  catch (Exception exception)   {
                //exception.printStackTrace();
                //showSimpleDialog(envelope.bodyIn.toString());
                respuesta = Prueba.Excepcion;
            }
            return respuesta;
        }


        @Override
        protected void onPostExecute(Prueba aPrueba) {
            showProgress(false);
            if(aPrueba == Prueba.InicioExitoso){
                llamarSoftPhoneActivity();
            }
            else if(aPrueba == Prueba.InicioDenegado){
                showSimpleDialog(AsociacionMensajes.get(aPrueba));
            }
            else if(aPrueba == Prueba.Excepcion){
                showSimpleDialog(AsociacionMensajes.get(aPrueba));
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
            super.onCancelled();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }
    private void showSimpleDialog(String s){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Login");
        builder.setMessage(s);
        builder.setPositiveButton("OK",null);
        builder.create();
        builder.show();


    }
    /**
     * Devuelve el IMEI del dispositivo.
     */
    private void getIMEI(){
        try{
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            aIMEI = telephonyManager.getDeviceId();
        }
        catch (Exception e){
            e.printStackTrace();
            aIMEI = e.getMessage().toString();
        }
    }

}
