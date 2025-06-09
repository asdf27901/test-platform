package com.lmj.platformserver.utils;

import com.lmj.platformserver.exception.TokenValidFailException;
import com.lmj.platformserver.result.ResultCodeEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    // 秘钥
    public static final SecretKey JWT_KEY = Keys.hmacShaKeyFor("jahsljdflksjdfklsjdfkljklsdfjsdf".getBytes());

    // token有效期
    private static final long EXPIRATION_TIME = 60 * 60 * 1000 * 2L;

    public static String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setIssuedAt(now)                // 签发时间
                .setExpiration(expiryDate)      // 过期时间
                .signWith(JWT_KEY, SignatureAlgorithm.HS256)  // 签名算法和密钥
                .claim("userId", userId)
                .compact();
    }

    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(JWT_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // token已过期
            throw new TokenValidFailException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch (Exception e) {
            // 无效token
            throw new TokenValidFailException(ResultCodeEnum.TOKEN_INVALID);
        }
    }
}
