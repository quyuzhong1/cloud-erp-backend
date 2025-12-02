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

 * @Date 2022-07-11 9:36
 * @Created by yl
 */
public class JwtUtils {

    private JwtUtils() {
    }

    /**
     * 私钥加密token
     *
     * @param info 用户信息
     * @param secret 密钥
     * @param expireMinutes 过期时间，单位秒
     * @return JWT Token
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
     * 私钥加密token（带权限路径列表）
     *
     * @param info 用户信息
     * @param secret 密钥
     * @param expireMinutes 过期时间，单位秒
     * @param pathList 权限路径列表
     * @return JWT Token
     */
    public static String generateToken(SysUserDTO info, String secret, Long expireMinutes, String[] pathList) {
        Date nowDate = new Date();
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("uid", info.getUid());
        claimsMap.put("userName", info.getUserName());
        claimsMap.put(SecurityConstants.USER_KEY, info.getToken());
        claimsMap.put("pathList", pathList);
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

    /**
     * 从JWT Token中获取权限路径列表
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return 权限路径列表
     */
    public static String[] getPathList(String token, String secret) {
        try {
            Claims claims = parseToken(token, secret);
            Object pathListObj = claims.get("pathList");
            if (pathListObj != null) {
                if (pathListObj instanceof String[]) {
                    return (String[]) pathListObj;
                } else if (pathListObj instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> list = (java.util.List<String>) pathListObj;
                    return list.toArray(new String[0]);
                }
            }
        } catch (Exception e) {
            // 解析失败，返回空数组
        }
        return new String[0];
    }


}
