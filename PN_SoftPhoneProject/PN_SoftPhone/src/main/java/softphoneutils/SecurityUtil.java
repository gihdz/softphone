package softphoneutils;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;

import java.util.Properties;

/**
 * Created by GDHB on 11/03/13.
 */
public class SecurityUtil extends Properties {

    public String decrypt(String name, String keyString)
            throws Exception {
        BlowfishEngine engine = new BlowfishEngine();
        PaddedBufferedBlockCipher cipher =
                new PaddedBufferedBlockCipher(engine);
        StringBuffer result = new StringBuffer();
        KeyParameter key = new KeyParameter(keyString.getBytes());
        cipher.init(false, key);
        byte out[] = Base64.decode(name);
        byte out2[] = new byte[cipher.getOutputSize(out.length)];
        int len2 = cipher.processBytes(out, 0, out.length, out2, 0);
        cipher.doFinal(out2, len2);
        String s2 = new String(out2);
        for (int i = 0; i < s2.length(); i++) {
            char c = s2.charAt(i);
            if (c != 0) {
                result.append(c);
            }
        }

        return result.toString();
    }
    public String encrypt(String value, String keyString)
            throws Exception {
        BlowfishEngine engine = new BlowfishEngine();
        PaddedBufferedBlockCipher cipher =
                new PaddedBufferedBlockCipher(engine);
        KeyParameter key = new KeyParameter(keyString.getBytes());
        cipher.init(true, key);
        byte in[] = value.getBytes();
        byte out[] = new byte[cipher.getOutputSize(in.length)];
        int len1 = cipher.processBytes(in, 0, in.length, out, 0);
        try {
            cipher.doFinal(out, len1);
        } catch (CryptoException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        String s = new String(Base64.encode(out));
        return s;
    }
}


