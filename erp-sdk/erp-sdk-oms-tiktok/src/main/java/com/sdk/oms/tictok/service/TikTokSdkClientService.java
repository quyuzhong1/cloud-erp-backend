package com.sdk.oms.tictok.service;

import com.common.business.utils.RedisUtil;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 美客多平台SDK
 * @Author Luo_WG
 * @Date 2024/2/27 18:04
 **/
@Slf4j
@Component
public class TikTokSdkClientService {
    public static void main(String[] args) {

    }


    private static RedisUtil redisUtil;


    @Resource
    private ShopInfoFeign shopInfoFeign;


    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        TikTokSdkClientService.redisUtil = redisUtil;
    }


}
