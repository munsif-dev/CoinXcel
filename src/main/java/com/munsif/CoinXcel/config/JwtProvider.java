package com.munsif.CoinXcel.config;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Collection;

public class JwtProvider {
    private static SecretKey key =  Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

    public static String generateToken(String subject) {
        Collection<? extends  >
    }
}
