package com.pn.pn_softphone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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

import static softphoneutils.GlobalUtils.GlobalParameters.OPERATION_NAME_REGISTERCOP;
import static softphoneutils.GlobalUtils.GlobalParameters.SOAP_ACTION_REGISTERCOP;
import static softphoneutils.GlobalUtils.GlobalParameters.SOAP_ADDRESS;
import static softphoneutils.GlobalUtils.GlobalParameters.WSDL_TARGET_NAMESPACE;

/**
 * Created by GDHB on 07/05/13.
 */
public class RegistroActivity extends Activity {
    //private UserLoginTask mAuthTask = null;
    View RegistroForm;
    View RegistroFormStatus;
    TextView Registro_Status_Message;

    public TelephonyManager telephonyManager;

    SharedPreferences sharedPreferences;

    public static String aIMEI;

    GlobalUtils.GlobalParameters globalParameters = new GlobalUtils.GlobalParameters();
    GlobalUtils.EmailValidator emailValidator = new GlobalUtils.EmailValidator();

    TextView textView_ShowIMEI;

    EditText editText_Email;
    EditText editText_Contrasena;
    EditText editText_ID_Policia;


    String emailString;
    String contrasenaString;
    String idPolicia_String;

    Button button_Registrar;
    Button button_Cancelar;

    Map<Prueba, String> AsociacionMensajes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        sharedPreferences = getSharedPreferences("datos_login",Context.MODE_PRIVATE);

        aIMEI = sharedPreferences.getString("IMEI","");
        getIMEI();

        textView_ShowIMEI = (TextView) findViewById(R.id.textViewRegistroShowIMEI);
        textView_ShowIMEI.setText(aIMEI);

        editText_Email = (EditText) findViewById(R.id.editTextRegistroPoliciaEmail);
        editText_Contrasena = (EditText) findViewById(R.id.editTextRegistroContrasena);
        editText_ID_Policia = (EditText) findViewById(R.id.editTextRegistroIdPolicia);


        button_Registrar = (Button) findViewById(R.id.buttonRegistroRegistrar);
        button_Cancelar = (Button) findViewById(R.id.buttonRegistroCancelar);

        button_Registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentarRegistro();
            }
        });
        button_Cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cancelar(view);
            }
        });

        RegistroForm = findViewById(R.id.login_table);
        RegistroFormStatus = findViewById(R.id.login_table_status);
        Registro_Status_Message = (TextView) findViewById(R.id.login_status_message);

        AsociacionMensajes = new HashMap<Prueba, String>();
        AsociacionMensajes.put(Prueba.UsuarioRegistrado, "Usuario Registrado!");
        AsociacionMensajes.put(Prueba.UsuarioExiste, "Usuario ya ha sido resgitrado anteriormente.\nFavor Comprobar credenciales.");
        AsociacionMensajes.put(Prueba.Excepcion, "Se ha producido un error.");






    }

    public void Cancelar(View view){
        finish();
    }
    /**
     * Para registrar un usuario.
     */
    private void intentarRegistro() {

        // Resetea los errores.
        editText_Email.setError(null);
        editText_Contrasena.setError(null);
        editText_ID_Policia.setError(null);

        // Almacena los valores para comprobar si los edit_text contienen valor.
        emailString = editText_Email.getText().toString();
        contrasenaString = editText_Contrasena.getText().toString();
        idPolicia_String = editText_ID_Policia.getText().toString();

        boolean cancel = false;
        View focusView = null;
        // Para verificar Email.
        if(TextUtils.isEmpty(emailString)){
            editText_Email.setError(getString(R.string.error_campo_requerido));
            focusView = editText_Email;
            cancel = true;
        } else if(!emailValidator.validateEmail(emailString)){
            editText_Email.setError(getString(R.string.error_email_invalido));
            focusView = editText_Email;
            cancel = true;
        }

        // Para verificar la contraseña
        if(TextUtils.isEmpty(contrasenaString)){
            editText_Contrasena.setError(getString(R.string.error_campo_requerido));
            focusView = editText_Contrasena;
            cancel = true;
        } else if(contrasenaString.length() < 5){
            editText_Contrasena.setError(getString(R.string.error_contrasena_tamano));
            focusView = editText_Contrasena;
            cancel = true;
        }

        // Para verificar ID_Policia
        if(TextUtils.isEmpty(idPolicia_String)){
            editText_ID_Policia.setError(getString(R.string.error_campo_requerido));
            focusView = editText_ID_Policia;
            cancel = true;
        }
        //else if(idPolicia_String.length() < 5){
         //   editText_ID_Policia.setError(getString(R.string.error_argumento_muy_corto));
        //    focusView = editText_ID_Policia;
        //    cancel = true;
       // }



        if(cancel){
            focusView.requestFocus();
        } else {
            Registro_Status_Message.setText(R.string.status_registrando);
            showProgress(true);
            InputMethodManager imm =
                    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText_Email.getWindowToken(), 0);
            new AsyncRegistrarPolicia().execute();


        }

    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            RegistroFormStatus.setVisibility(View.VISIBLE);
            RegistroFormStatus.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            RegistroFormStatus.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            RegistroForm.setVisibility(View.VISIBLE);
            RegistroForm.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            RegistroForm.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            RegistroFormStatus.setVisibility(show ? View.VISIBLE : View.GONE);
            RegistroForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    private class AsyncRegistrarPolicia extends AsyncTask<Void,Void,Prueba>{

        @Override
        protected Prueba doInBackground(Void... voids) {
            Prueba respuesta;
            SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME_REGISTERCOP);

            PropertyInfo property_Email = new PropertyInfo();
            property_Email.type = PropertyInfo.STRING_CLASS;
            property_Email.name = "Email";

            PropertyInfo property_Contrasena = new PropertyInfo();
            property_Contrasena.type = PropertyInfo.STRING_CLASS;
            property_Contrasena.name = "Contrasena";

            PropertyInfo property_ID_Policia = new PropertyInfo();
            property_ID_Policia.type = PropertyInfo.STRING_CLASS;
            property_ID_Policia.name = "IdPolicia";

            PropertyInfo property_IMEI = new PropertyInfo();
            property_IMEI.type = PropertyInfo.STRING_CLASS;
            property_IMEI.name = "IMEI";

            emailString = editText_Email.getText().toString();
            idPolicia_String = editText_ID_Policia.getText().toString();
            contrasenaString = editText_Contrasena.getText().toString();

            request.addPropertyIfValue(property_Email,emailString);
            request.addPropertyIfValue(property_Contrasena,contrasenaString);
            request.addPropertyIfValue(property_ID_Policia, idPolicia_String);
            request.addPropertyIfValue(property_IMEI, aIMEI);


            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);
            HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS, 40000);


            try  {
                httpTransport.call(SOAP_ACTION_REGISTERCOP, envelope);

                //response = Boolean.valueOf(envelope.getResponse().toString());
                respuesta = Prueba.valueOf(envelope.getResponse().toString());

            }  catch (Exception e)   {
                //mostrarDialogoSimple("Servidor no responde.");
                respuesta = Prueba.Excepcion;


            }
            return respuesta;


        }

        @Override
        protected void onPostExecute(Prueba aPrueba) {
            showProgress(false);
            String mensaje = AsociacionMensajes.get(aPrueba);
            if(aPrueba == Prueba.UsuarioRegistrado){
                mostrarDialogoRegistroExitoso(mensaje);
            }
            else{
                mostrarDialogoSimple(mensaje);
            }
//            if(aBoolean == null){
//                mostrarDialogoSimple("Servidor");
//            }
//            else if(aBoolean == false){
//                mostrarDialogoSimple("Usuario ya ha sido resgitrado anteriormente.\n" +
//                        "Favor comprobar credenciales.");
//            }else if(aBoolean == true){
//                mostrarDialogoRegistroExitoso("Usuario Registrado!");
//            }

            super.onPostExecute(aPrueba);
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
            super.onCancelled();
        }
    }
    private void mostrarDialogoSimple(String s){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registrar");
        builder.setMessage(s);
        builder.setCancelable(false);
        builder.setPositiveButton("OK",null);
        builder.create();
        builder.show();


    }
    private void mostrarDialogoRegistroExitoso(String mensaje){
        AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
        dialogo.setTitle("Registrar");
        dialogo.setMessage(mensaje);
        dialogo.setCancelable(false);
        dialogo.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        dialogo.create();
        dialogo.show();
    }

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
