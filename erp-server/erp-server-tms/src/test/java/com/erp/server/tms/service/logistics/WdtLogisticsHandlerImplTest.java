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
public class WdtLogisticsHandlerImplTest {

    @Resource
    private WdtLogisticsHandlerImpl wdtLogisticsHandler;

    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    @Test
    public void getChannel() {
        Map<String,String> authMap = new HashMap<>();
        authMap.put("appToken","44ac3ae1211d416a080858e57833cc14");
        authMap.put("appKey","fa0c90d7dbb434fa2160209756db677c");
        ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
        chanelQueryVO.setAuthMap(authMap);
        ApiResult<List<LogisticsSaleChannelEntity>> result = wdtLogisticsHandler.getChannel(chanelQueryVO);
        for(LogisticsSaleChannelEntity logisticsSaleChannelEntity : result.getData()){
            logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
        }
        System.out.println(result);
    }

    @Test
    public void authorization() {
        Map<String,String> authMap = new HashMap<>();
        authMap.put("appToken","44ac3ae1211d416a080858e57833cc14");
        authMap.put("appKey","fa0c90d7dbb434fa2160209756db677c");
        ApiResult<Object>result = wdtLogisticsHandler.authorization(authMap);
        System.out.println(result);
    }
}