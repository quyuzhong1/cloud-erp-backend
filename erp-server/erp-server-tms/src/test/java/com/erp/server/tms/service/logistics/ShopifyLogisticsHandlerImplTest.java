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

/**
 * @author zdy
 * @ClassName UBILogisticsHandlerImplTest
 * @date 2023年11月16日
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class ShopifyLogisticsHandlerImplTest {
    @Resource
    private ShopifyLogisticsHandlerImpl shopifyLogisticsHandler;
    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    private Map<String, String> authMap = new HashMap<>();


    @Test
    public void getChannel() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = shopifyLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        System.out.println(channel);
    }

    @Test
    public void createChannelData() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = shopifyLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        List<LogisticsSaleChannelEntity> entityList = channel.getData();
        entityList.forEach(logisticsSaleChannelEntity -> {
            logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
        });
    }
}
