package com.erp.server.tms.service.logistics;

import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.LogisticsTrackBaseDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
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
        authMap.put("clientId","Yg4Zf06w_sxZs3A5D");
        authMap.put("clientSecret","579cf53f55694d89aef0887d81886aec");
    }

    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = track123LogisticsHandler.getLogisticsAuthConfigByShopId("");
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
                .trackNo("SL1694930851235")
                .phoneSuffix("18855193495")
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
        /**
         * {"code":"00000","data":{"accepted":[],"rejected":[{"index":0,"trackNo":"SF1694930851235","courierCode":"sfb2c",
         * "error":{"code":"A0400","msg":"The order number has been imported"}}]},
         * "msg":"Success","traceId":"1bd02e6204f94aec92e0c7066a86506e.2971.17161982770923801"}
         */
    }

    /**
     * 航运注册
     */
    @Test
    public void oceanRegisterLogisticsNumber(){
        List<LogisticsTrackBaseDTO.OceanRegisterRequestDTO> requestList = new ArrayList<>();
        LogisticsTrackBaseDTO.OceanRegisterRequestDTO oceanRegisterRequestDTO = LogisticsTrackBaseDTO.OceanRegisterRequestDTO.builder()
                .trackNo("FSCU8892622")
                .carrierCode("cosco")
                .id("1789113371434422274")
                .type(MathUtil.THREE)
                .authMap(authMap)
                .build();
        requestList.add(oceanRegisterRequestDTO);
        ApiResult<List<RegisterResponseVO>> listApiResult = track123LogisticsHandler.oceanRegisterLogisticsNumber(requestList);
        System.out.println(listApiResult);
    }

    @Test
    public void getTrack(){
        LogisticsTrackVO logisticsQueryVO = new LogisticsTrackVO();
//        String str = "{\"trackNos\":[\"NM327424528BR\",\"NM324763782BR\",\"NM321937918BR\",\"NM324753459BR\",\"NM329788123BR\",\"NM324761248BR\",\"NM324756132BR\",\"NM321941988BR\",\"NM329786459BR\",\"NM319647276BR\",\"NM329784532BR\",\"NM331935240BR\",\"NM331936625BR\",\"NM321948751BR\",\"NM331932138BR\",\"NM327438323BR\",\"NM321944785BR\",\"NM321940038BR\",\"NM327434967BR\",\"NM321940863BR\",\"NM321933540BR\",\"NM321934973BR\",\"NM329791462BR\",\"NM331927226BR\",\"NM324757416BR\",\"NM331937532BR\",\"NM331931132BR\",\"NM324752127BR\",\"NM331936435BR\",\"NM329790073BR\",\"NM331932963BR\",\"NM331933725BR\",\"NM324751988BR\",\"NM329785135BR\",\"NM331934920BR\",\"NM324755256BR\",\"NM321933598BR\",\"NM331928592BR\",\"NM329787216BR\",\"NM329790467BR\",\"NM331939428BR\",\"NM331924159BR\",\"NM324753621BR\",\"NM331937078BR\",\"NM329787785BR\",\"NM331938507BR\",\"NM329791445BR\",\"NM329781712BR\",\"NM324762328BR\",\"NM324756645BR\",\"NM331923533BR\",\"NM321939905BR\",\"NM331934978BR\",\"NM324758748BR\",\"NM331934068BR\",\"NM324754091BR\",\"NM321945256BR\",\"NM324749701BR\",\"NM321942895BR\",\"NM329791272BR\",\"NM331924162BR\",\"NM324755769BR\",\"NM331930619BR\",\"NM331938970BR\",\"NM331931968BR\",\"NM321942802BR\",\"NM321946322BR\",\"NM331938864BR\",\"NM321937135BR\",\"NM321935611BR\",\"NM308770524BR\",\"NM327431682BR\",\"NM321936020BR\",\"NM321937189BR\",\"NM329791286BR\",\"NM329793256BR\",\"NM324754560BR\",\"NM329786989BR\",\"NM329791357BR\",\"NM329786808BR\",\"NM327432303BR\",\"NM327434233BR\",\"NM329785952BR\",\"NM321944281BR\",\"NM329787468BR\",\"NM321941356BR\",\"NM327427904BR\",\"NM331940372BR\",\"NM324761336BR\",\"NM331934187BR\",\"NM327435450BR\",\"NM331931628BR\",\"NM324753666BR\",\"NM331940324BR\",\"NM331930137BR\",\"NM324747657BR\",\"NM321942440BR\",\"NM321940347BR\",\"NM331931512BR\",\"NM327436013BR\"],\"authMap\":{\"clientId\":\"Yg4Zf06w_sxZs3A5D\",\"logisticsPlatform\":\"TRACK123\",\"clientSecret\":\"579cf53f55694d89aef0887d81886aec\",\"id\":\"1727135808492032002\"}}";
//        LogisticsTrackVO logisticsTrackVO = JSON.parseObject(str, LogisticsTrackVO.class);
        List<String> trackNos = new ArrayList<>();
        trackNos.add("LS961438364NL");
////        trackNos.add("00369744292706509832");
        logisticsQueryVO.setTrackNos(trackNos);
        logisticsQueryVO.setAuthMap(authMap);
        ApiResult<List<LogisticsTrackEntity>> track = track123LogisticsHandler.getTrack(logisticsQueryVO);
        System.out.println(track);
    }

    @Test
    public void getOceanTrack(){
        List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> list = new ArrayList<>();
        LogisticsTrackBaseDTO.OceanTrackRequestDTO oceanTrackRequestDTO = LogisticsTrackBaseDTO.OceanTrackRequestDTO.builder()
                .trackingNo("FSCU8892622")
                .orderNo("577170392674754560")
                .type(MathUtil.THREE)
                .authMap(authMap)
                .build();
        list.add(oceanTrackRequestDTO);
        ApiResult<List<LogisticsTrackEntity>> track = track123LogisticsHandler.getOceanTrack(list);
        System.out.println(track);
    }

    @Test
    public void authorization() {
        ApiResult<Object>ApiResult<Object>= track123LogisticsHandler.authorization(authMap);
        System.out.println(apiResult);
    }
}
