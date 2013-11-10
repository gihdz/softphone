package softphoneutils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by GDHB on 07/05/13.
 */
public class GlobalUtils {

    public static class GlobalParameters {

        public static final String ip_address = "192.168.1.7";

        public static final String SOAP_ACTION_REGISTERCOP = "http://tempuri.org/registerCop";
        public static final String SOAP_ACTION_LOGINCOP = "http://tempuri.org/loginCop";
        public static final String SOAP_ACTION_VERIFYIMEI = "http://tempuri.org/verifyIMEI";

        public static final String OPERATION_NAME_REGISTERCOP = "registerCop";
        public static final String OPERATION_NAME_LOGINCOP = "loginCop";
        public static final String OPERATION_NAME_VERIFYIMEI = "verifyIMEI";

        public static final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";

        public static final String SOAP_ADDRESS = "http://"+ip_address+"/AndroidWEB/AccessDB.asmx";



        //10.0.0.100

        //192.168.25.107

    }


    public static class EmailValidator {

        private Pattern pattern;
        private Matcher matcher;

        private static final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{3,})$";

        public EmailValidator() {
            pattern = Pattern.compile(EMAIL_PATTERN);
        }

        /**
         * Validate hex with regular expression
         *
         * @param Email
         *            email for validation
         * @return true valid email, false invalid email
         */
        public boolean validateEmail(final String Email) {

            matcher = pattern.matcher(Email);
            return matcher.matches();

        }
    }



}
