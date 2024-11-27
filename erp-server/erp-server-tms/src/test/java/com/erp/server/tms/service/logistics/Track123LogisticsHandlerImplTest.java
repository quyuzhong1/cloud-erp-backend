package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
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
import org.python.antlr.ast.Str;
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
        String json = "{\"logisticsRegisterVOS\":[{\"trackNo\":\"YT2433021901004313\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901000962\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901000708\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433021901004387\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901000960\",\"phoneSuffix\":\"+31623433511\"},{\"trackNo\":\"YT2433021901004119\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433021901004364\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3113103033913\",\"phoneSuffix\":\"\"},{\"trackNo\":\"LP00695519487119\",\"phoneSuffix\":\"569537478\"},{\"trackNo\":\"LP00694979999789\",\"phoneSuffix\":\"204185750\"},{\"trackNo\":\"SF3109341258386\",\"phoneSuffix\":\"\"},{\"trackNo\":\"LP00695766897734\",\"phoneSuffix\":\"7898090\"},{\"trackNo\":\"SF3109747258881\",\"phoneSuffix\":\"\"},{\"trackNo\":\"WSHBR3314701891YQ\",\"phoneSuffix\":\"1158125033\"},{\"trackNo\":\"YT2433021901003622\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78857995484173\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF1393946229492\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3100904358482\",\"phoneSuffix\":\"\"},{\"trackNo\":\"RB351879079SG\",\"phoneSuffix\":\"95****812\"},{\"trackNo\":\"YT2433021901003628\",\"phoneSuffix\":\"+32468373498\"},{\"trackNo\":\"YT2433021901004132\",\"phoneSuffix\":\"\"},{\"trackNo\":\"CGD00007944452\",\"phoneSuffix\":\"53****137\"},{\"trackNo\":\"SF3121250373725\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433021901004158\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78858205883457\",\"phoneSuffix\":\"\"},{\"trackNo\":\"LP00696521563426\",\"phoneSuffix\":\"9084585459\"},{\"trackNo\":\"SF3135793645385\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433021901003646\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF1397243200402\",\"phoneSuffix\":\"\"},{\"trackNo\":\"76445459425134\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433021901004513\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3129050928791\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78463121024718\",\"phoneSuffix\":\"\"},{\"trackNo\":\"LP278826240SG\",\"phoneSuffix\":\"0104999****\"},{\"trackNo\":\"YT2433021901004149\",\"phoneSuffix\":\"+16178938819\"},{\"trackNo\":\"YT2433021901004147\",\"phoneSuffix\":\"+17864515467\"},{\"trackNo\":\"78858203567974\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433021901004113\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78858201303486\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3135882045383\",\"phoneSuffix\":\"\"},{\"trackNo\":\"RLV09041247\",\"phoneSuffix\":\"925*****75\"},{\"trackNo\":\"YT2433021901000635\",\"phoneSuffix\":\"\"},{\"trackNo\":\"NM667285840BR\",\"phoneSuffix\":\"119*****530\"},{\"trackNo\":\"NM667634995BR\",\"phoneSuffix\":\"119*****209\"},{\"trackNo\":\"SF3134603345087\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3127450798762\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3110271159319\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78858199964618\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3106634166480\",\"phoneSuffix\":\"\"},{\"trackNo\":\"76445459523900\",\"phoneSuffix\":\"\"},{\"trackNo\":\"76445456577957\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3129856476790\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78463121032543\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78463142878089\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78858219280867\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901001074\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901000876\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901000731\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901001056\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901000965\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901000740\",\"phoneSuffix\":\"+4917623300181\"},{\"trackNo\":\"SF3127258576960\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF1393446439414\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3109342258384\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3113502433813\",\"phoneSuffix\":\"\"},{\"trackNo\":\"RK319822939LV\",\"phoneSuffix\":\"67****474\"},{\"trackNo\":\"SF3113600033015\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78462890715802\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78858216954516\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78858196026512\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78858204386340\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901001100\",\"phoneSuffix\":\"+494060780887\"},{\"trackNo\":\"SF1393746339406\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3137031745383\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3135903245089\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78858210278137\",\"phoneSuffix\":\"\"},{\"trackNo\":\"76445459334854\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901000798\",\"phoneSuffix\":\"+4917657699317\"},{\"trackNo\":\"YT2433121901000972\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3133790348787\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78462890781706\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78857996093611\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF1393046949495\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78857993836249\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3113898333019\",\"phoneSuffix\":\"\"},{\"trackNo\":\"76445175925402\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78857996015971\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3129958216927\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78857994468952\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3103146258787\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3113004933918\",\"phoneSuffix\":\"\"},{\"trackNo\":\"YT2433121901001061\",\"phoneSuffix\":\"\"},{\"trackNo\":\"JDVC27434471852\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3127458016962\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3130677148987\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3103216858783\",\"phoneSuffix\":\"\"},{\"trackNo\":\"78857995402959\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3113909833813\",\"phoneSuffix\":\"\"},{\"trackNo\":\"76445458255821\",\"phoneSuffix\":\"\"},{\"trackNo\":\"SF3134090145385\",\"phoneSuffix\":\"\"}],\"authMap\":{\"clientId\":\"Yg4Zf06w_sxZs3A5D\",\"logisticsPlatform\":\"TRACK123\",\"clientSecret\":\"579cf53f55694d89aef0887d81886aec\",\"id\":\"1727135808492032002\"}}";
        RegisterTrackVO registerTrackVO = JSONUtil.toBean(json, RegisterTrackVO.class);
//        RegisterTrackVO registerTrackVO = RegisterTrackVO.builder()
//                .authMap(authMap)
//                .logisticsRegisterVOS(registerVOS)
//                .build();
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
    }
}
