package com.erp.server.tms.service.logistics;

import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.service.LogisticsBaseService;
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
public class WalmartLogisticsHandlerImplTest {
    @Resource
    private LogisticsBaseService logisticsBaseService;


    @Test
    public void createChannelData() {
        logisticsBaseService.syncSingleChannel(PlatformDictEnum.WALMART.getCode());
    }
}
