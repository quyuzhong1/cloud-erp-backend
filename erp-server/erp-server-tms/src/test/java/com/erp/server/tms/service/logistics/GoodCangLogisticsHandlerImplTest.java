package com.erp.server.tms.service.logistics;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class GoodCangLogisticsHandlerImplTest {

    @Resource
    private GoodCangLogisticsHandlerImpl goodCangLogisticsHandler;

    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    @Test
    public void getChannel() {
        Map<String,String> authMap = new HashMap<>();
        authMap.put("appToken","7013991264f611e98ea200e01b680258");
        authMap.put("appKey","6ff50abf64f611e98ea200e01b680258");
        ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
        chanelQueryVO.setAuthMap(authMap);
        ApiResult<List<LogisticsSaleChannelEntity>> result = goodCangLogisticsHandler.getChannel(chanelQueryVO);
        for(LogisticsSaleChannelEntity logisticsSaleChannelEntity : result.getData()){
            logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
        }
        System.out.println(result);
    }

    @Test
    public void authorization() {
        Map<String,String> authMap = new HashMap<>();
        authMap.put("appToken","7013991264f611e98ea200e01b680258");
        authMap.put("appKey","6ff50abf64f611e98ea200e01b680258");
        ApiResult<Object>result = goodCangLogisticsHandler.authorization(authMap);
        System.out.println(result);
    }
}