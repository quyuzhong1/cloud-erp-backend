package com.cloud.erp.gateway.web.server;

import com.alibaba.fastjson2.JSONObject;
import com.cloud.erp.gateway.config.JwtProperties;
import com.comm.core.constant.RedisCacheConstants;
import com.comm.core.utils.IdUtils;
import com.comm.core.utils.JwtUtils;
import com.common.web.service.RedisService;
import com.erp.common.modules.sys.dto.SysUserDTO;
import com.erp.common.vo.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Classname TokenService
 * @Description TODO
 * @Date 2022-07-11 12:00
 * @Created by yl
 */
@Component
@Slf4j
public class TokenService {

    @Resource
    private JwtProperties jwtProperties;

    @Autowired
    private RedisService redisService;


    private final static long expireTime = RedisCacheConstants.EXPIRATION;


    /**
     * 刷新token
     */
    public void refreshToken(SysUserDTO info, Long expireTime) {
        //放入缓存
        String userKey = getTokenKey(info.getToken());
        redisService.setCacheObject(userKey, JSONObject.toJSONString(info) , expireTime, TimeUnit.DAYS);
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
        String token = IdUtils.fastUUID();
        info.setToken(token);
        refreshToken(info, expireTime);
        String accessToken = JwtUtils.generateToken(info, jwtProperties.getSecret(), jwtProperties.getExpire());
        return accessToken;

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
                String userKey = JwtUtils.getUserKey(accessToken, jwtProperties.getSecret());
                String userJson = redisService.getCacheObject(getTokenKey(userKey));
                user = JSONObject.parseObject(userJson, LoginUser.class);
            }
        } catch (Exception e) {
            log.error("出错了==",e);
        }
        return user;
    }


    private String getTokenKey(String token) {
        return RedisCacheConstants.LOGIN_TOKEN_KEY + token;
    }
}
