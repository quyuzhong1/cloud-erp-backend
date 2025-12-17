package com.erp.server.plm;

import cn.hutool.json.JSONObject;

import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
class ErpServerPlmApplicationTests {


    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Test
    void contextLoads() {
        //读取配置，初始化SDK
        KingdeeApiUtils soApiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_SALEORDER.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject soJson = kingdeeCommonService.view(soApiUtils, "",new HashMap<>());
        List<Map<String, Object>> resultList = (List<Map<String, Object>>) soJson.get("SaleOrderEntry");
        for (Map<String, Object> item : resultList) {
            String id = item.get("Id").toString();
            System.out.println(id);
            Map<String,Object> MaterialMap = (Map<String, Object>) item.get("MaterialId");
            System.out.println(MaterialMap.get("Number"));


        }



    }

}
