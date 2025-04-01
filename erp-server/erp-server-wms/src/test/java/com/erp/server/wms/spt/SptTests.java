package com.erp.server.wms.spt;

import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.impl.SptHandlerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class SptTests {

    @Resource
    private SptHandlerServiceImpl sptHandlerServiceImpl;

    public SptTests(){
        Map<String,Object> authMap = new HashMap<>();
        //生产
//        authMap.put("appToken","bee4c5e37009b3f4d8a18f7ce44a2365");
//        authMap.put("appKey","674f5f45f2ffdaa6705d88d38d5d04a3");
        //测试
        authMap.put("appToken","b704c637a0ecfed5dbda799a987291d4");
        authMap.put("appKey","ae60ef5fd2ba671c510d3eb1ae788046");
        ThirdWarehouseContext.setAuthMap(authMap);
    }

    @Test
    public void getSkuList() {

    }

}
