package com.erp.server.tms.service.logistics;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.service.LogisticsSaleChannelService;
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
public class AmazonLogisticsHandlerImplTest {
    @Resource
    private AmazonLogisticsHandlerImpl amazonLogisticsHandler;
    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    private Map<String, String> authMap = new HashMap<>();


    @Test
    public void getChannel() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = amazonLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        System.out.println(channel);
    }

    @Test
    public void createChannelData() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = amazonLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        List<LogisticsSaleChannelEntity> entityList = channel.getData();
        entityList.forEach(logisticsSaleChannelEntity -> {
            logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
        });
    }
}
