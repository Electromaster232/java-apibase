package me.djelectro.javaflasklike.utils;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


public class PasswordFactory {

    private static PasswordFactory instance;
    public static void initFactory(String salt) { instance = new PasswordFactory(salt); }
    public static PasswordFactory getInstance(){return instance;}

    String salt;
    int iterations = 10000;
    int keyLength = 512;
    boolean init = false;

    public String encodeString(String raw){
        if(!init)
            throw new RuntimeException("Password Factory was not initialized!");

        char[] passwordChars = raw.toCharArray();
        byte[] saltBytes = salt.getBytes();

        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
            PBEKeySpec spec = new PBEKeySpec( passwordChars, saltBytes, iterations, keyLength );
            SecretKey key = skf.generateSecret( spec );
            byte[] res = key.getEncoded( );
            return Hex.encodeHexString(res);
        } catch ( NoSuchAlgorithmException | InvalidKeySpecException e ) {
            throw new RuntimeException( e );
        }
    }

    public PasswordFactory(String salt){

        this.salt = salt;
        init = true;
    }


}
