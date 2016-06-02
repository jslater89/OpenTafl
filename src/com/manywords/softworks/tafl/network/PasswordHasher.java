package com.manywords.softworks.tafl.network;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by jay on 5/25/16.
 */
public class PasswordHasher {
    public static String generateSalt() {
        byte[] salt = new byte[8];
        new SecureRandom().nextBytes(salt);
        return Base64.encode(salt);
    }

    public static String hashPassword(String salt, String password) {
        String hashedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            password = salt + password;
            byte[] bytes = md.digest(password.getBytes("UTF-8"));
            hashedPassword = Base64.encode(bytes);
        }
        catch (NoSuchAlgorithmException e){
            throw new RuntimeException("What kind of platform doesn't support SHA-512?");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("What kind of platform doesn't support UTF-8?");
        }
        return hashedPassword;
    }

}
