package com.cloud.erp.gateway.web.server;

import com.alibaba.fastjson.JSON;
import com.cloud.erp.gateway.config.JwtProperties;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.service.impl.RedisService;
import com.common.business.vo.LoginUser;
import com.common.core.utils.IdUtils;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Classname TokenService

 * @Date 2022-07-11 12:00
 * @Created by yl
 */
@Component
@Slf4j
public class TokenService {

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private RedisService redisService;


    private static final long EXPIRE_TIME = RedisCacheConstants.EXPIRATION;


    /**
     * 刷新token
     */
    public void refreshToken(SysUserDTO info, Long expireTime) {
        //放入缓存
        String userKey = getTokenKey(info.getToken());
        redisService.setCacheObject(userKey, JSON.toJSONString(info) , expireTime, TimeUnit.DAYS);
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
        refreshToken(info, EXPIRE_TIME);
        return JwtUtils.generateToken(info, jwtProperties.getSecret(), jwtProperties.getExpire());

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
                user = JSON.parseObject(userJson, LoginUser.class);
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
