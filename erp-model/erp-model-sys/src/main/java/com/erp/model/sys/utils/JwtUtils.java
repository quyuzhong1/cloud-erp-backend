package com.erp.model.sys.utils;


import com.common.core.utils.ConvertUtil;
import com.common.business.constant.SecurityConstants;
import com.erp.model.sys.dto.SysUserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname JwtUtils 工具类
 * @Description TODO
 * @Date 2022-07-11 9:36
 * @Created by yl
 */
public class JwtUtils {


    /**
     * 私钥加密token
     *
     * @param
     * @param expireMinutes 过期时间，单位秒
     * @return
     * @throws Exception
     */
    public static String generateToken(SysUserDTO info, String secret, Long expireMinutes) {
        Date nowDate = new Date();
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("uid", info.getUid());
        claimsMap.put("userName", info.getUserName());
        claimsMap.put(SecurityConstants.USER_KEY, info.getToken());
        //过期时间
        Date expireDate = new Date(nowDate.getTime() + expireMinutes * 1000);
        SecretKey key = generateKey(secret);
        return Jwts.builder()
                //作为什么用户的唯一标志
                .setSubject(info.getUid() + "")
                .setClaims(claimsMap)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(key)
                .compact();
    }

    /**
     * 根据令牌获取用户标识
     *
     * @param token 令牌
     * @return 用户ID
     */
    public static String getUserKey(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return getValue(claims, SecurityConstants.USER_KEY);
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    public static Claims parseToken(String token, String secret) {
        SecretKey key = generateKey(secret);
        Claims body = Jwts.parserBuilder().setSigningKey(key).
                build().parseClaimsJws(token).getBody();
        return body;
    }


    private static SecretKey generateKey(String secret) {
        byte[] encodedKey = secret.getBytes();
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, SignatureAlgorithm.HS256.getJcaName());
    }


    /**
     * 根据身份信息获取键值
     *
     * @param claims 身份信息
     * @param key    键
     * @return 值
     */
    public static String getValue(Claims claims, String key) {
        return ConvertUtil.toStr(claims.get(key), "");
    }


}
