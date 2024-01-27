package com.erp.server.tms.service.logistics;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.ErpServerTmsApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author zdy
 * @ClassName UBILogisticsHandlerImplTest
 * @description: TODO
 * @date 2023年11月16日
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class Track123LogisticsHandlerImplTest {
    @Resource
    private Track123LogisticsHandlerImpl track123LogisticsHandler;

    private Map<String, String> authMap = new HashMap<>();

    public Track123LogisticsHandlerImplTest(){
        //test
//        authMap.put("clientSecret","9fa500686633410a84ff0b00daed555e");
        //pro
        authMap.put("clientSecret","579cf53f55694d89aef0887d81886aec");
    }

    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = track123LogisticsHandler.getLogisticsAuthConfig("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = track123LogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }

    @Test
    public void registerLogisticsNumber(){
        List<LogisticsRegisterVO> registerVOS = new ArrayList<>();
        LogisticsRegisterVO vo = LogisticsRegisterVO.builder()
                .trackNo("DPK212369350859")
                .build();
        registerVOS.add(vo);
//        LogisticsRegisterVO vo1 = LogisticsRegisterVO.builder()
//                .trackNo("00369744292706509832")
//                .build();
//        registerVOS.add(vo1);
        RegisterTrackVO registerTrackVO = RegisterTrackVO.builder()
                .authMap(authMap)
                .logisticsRegisterVOS(registerVOS)
                .build();
        ApiResult<List<RegisterResponseVO>> listApiResult = track123LogisticsHandler.registerLogisticsNumber(registerTrackVO);
        System.out.println(listApiResult);
    }

    @Test
    public void getTrack(){
        LogisticsTrackVO logisticsQueryVO = new LogisticsTrackVO();
        List<String> trackNos = new ArrayList<>();
        trackNos.add("UJ076562757YP");
//        trackNos.add("00369744292706509832");
        logisticsQueryVO.setTrackNos(trackNos);
        logisticsQueryVO.setAuthMap(authMap);
        ApiResult<List<LogisticsTrackEntity>> track = track123LogisticsHandler.getTrack(logisticsQueryVO);
        System.out.println(track);
    }

    @Test
    public void authorization() {
        ApiResult apiResult = track123LogisticsHandler.authorization(authMap);
        System.out.println(apiResult);
    }
}
