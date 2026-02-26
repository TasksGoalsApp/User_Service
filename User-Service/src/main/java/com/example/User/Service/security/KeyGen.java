package com.example.User.Service.security;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
public class KeyGen {


    public static void main(String[] args) {
        var key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        var base64Key = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("Base64 HS256 key: " + base64Key);
    }

}
