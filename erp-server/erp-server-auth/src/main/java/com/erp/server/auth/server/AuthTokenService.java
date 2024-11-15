package com.erp.server.auth.server;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.service.impl.RedisService;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.utils.JwtUtils;
import com.erp.server.auth.config.AuthJwtProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * @Classname TokenService

 * @Date 2022-07-11 12:00
 * @Created by yl
 */
@Slf4j
@Component
public class AuthTokenService {

    @Resource
    private AuthJwtProperties authJwtProperties;


    @Autowired
    private RedisService redisService;


    /**
     * 刷新token
     */
    public void refreshToken(SysUserDTO info, Long expireTime) {
        //放入缓存
        String userKey = getTokenKey(info.getToken());
        redisService.setCacheObject(userKey, JSONObject.toJSONString(info), expireTime, TimeUnit.SECONDS);
    }

    /**
     * 创建token
     *
     * @param info
     * @return java.lang.String
     * @author yl
     * @date 2022-07-14 14:26
     */

    public String createToken(SysUserDTO info) {
        //先生成一个token
        String token = info.getUid();
        info.setToken(token);
        refreshToken(info, authJwtProperties.getExpire());
        return JwtUtils.generateToken(info, authJwtProperties.getSecret(), authJwtProperties.getExpire());

    }


    /**
     * 根据token  获取用户信息
     *
     * @param accessToken
     * @return com.cloud.erp.common.common.token.vo.LoginUser
     * @author yl
     * @date 2022-07-15 9:55
     */

    public LoginUser getLoginUser(String accessToken) {
        LoginUser user = null;
        try {
            if (StringUtils.isNotBlank(accessToken)) {
                String userKey = JwtUtils.getUserKey(accessToken, authJwtProperties.getSecret());
                String userJson = redisService.getCacheObject(getTokenKey(userKey));
                user = JSONObject.parseObject(userJson, LoginUser.class);
            }
        } catch (Exception e) {
        	log.error("获取登录用户错误");
        }
        return user;
    }


    private String getTokenKey(String token) {
        return RedisCacheConstants.LOGIN_TOKEN_KEY + token;
    }

    /**
     * 删除 redis 里面的token 信息
     *
     * @param accessToken 原始的token
     * @return void
     * @author yl
     * @date 2022-07-18 15:36
     */
    public void removeToken(String accessToken) {
        if (StringUtils.isNotBlank(accessToken)) {
            String userToken = JwtUtils.getUserKey(accessToken, authJwtProperties.getSecret());
            String userKey=getTokenKey(userToken);
            if (StringUtils.isNotBlank(userKey)) {
                redisService.deleteObject(userKey);
            }
            //暂时
            UserContext.clear();

        }
    }
}
