package com.erp.server.dmp.pull.schedule;

import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformDataDTO;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.sdk.wms.goodcang.dto.response.GoodCangOutboundResp;
import com.sdk.wms.goodcang.dto.response.GoodCangReceiptBatchResp;
import com.sdk.wms.goodcang.handle.GoodCangInboundHandler;
import com.sdk.wms.goodcang.handle.GoodCangOutboundHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class PullGoodCangJobTest {

    @Resource
    private PullGoodCangJob pullGoodCangJob;

    @Resource
    private GoodCangOutboundHandler goodCangOutboundHandler;

    @Resource
    private GoodCangInboundHandler goodCangInboundHandler;

    @Test
    public void execute() {
        pullGoodCangJob.execute();
    }


    @Test
    public void goodCangOutboundHandlerTest(){
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appToken","7013991264f611e98ea200e01b680258");
        authMap.put("appKey","6ff50abf64f611e98ea200e01b680258");
        ThirdWarehouseContext.setAuthMap(authMap);
        JobTaskDTO data = new JobTaskDTO();
        data.setLastTime(LocalDateTime.of(2020,12,20, 0, 0, 0));
        data.setNextTime(LocalDateTime.of(2021,12,20, 0, 0, 0));
        data.setApiParam(authMap);
        PlatformDataDTO<GoodCangReceiptBatchResp, PlatformInboundDTO> result = goodCangInboundHandler.pullHandle(data);
        System.out.println(result);
    }

}