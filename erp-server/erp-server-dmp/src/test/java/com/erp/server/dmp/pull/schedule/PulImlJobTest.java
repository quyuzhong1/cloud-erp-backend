package com.erp.server.dmp.pull.schedule;

import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformDataDTO;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.sdk.wms.goodcang.dto.response.GoodCangOutboundResp;
import com.sdk.wms.goodcang.handle.GoodCangOutboundHandler;
import com.sdk.wms.iml.dto.response.ImlOutboundResp;
import com.sdk.wms.iml.handle.ImlOutboundHandler;
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
public class PulImlJobTest {

    @Resource
    private PullImlJob pullImlJob;

    @Resource
    private ImlOutboundHandler imlOutboundHandler;

    @Test
    public void execute() {
        pullImlJob.execute();
    }


    @Test
    public void imlOutboundHandlerTest(){
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appToken","44ac3ae1211d416a080858e57833cc14");
        authMap.put("appKey","fa0c90d7dbb434fa2160209756db677c");
        ThirdWarehouseContext.setAuthMap(authMap);
        JobTaskDTO data = new JobTaskDTO();
        data.setLastTime(LocalDateTime.of(2020,12,20, 0, 0, 0));
        data.setNextTime(LocalDateTime.of(2021,1,10, 0, 0, 0));
        data.setApiParam(authMap);
        PlatformDataDTO<ImlOutboundResp, PlatformOutboundDTO> result = imlOutboundHandler.pullHandle(data);
        System.out.println(result);
    }

}