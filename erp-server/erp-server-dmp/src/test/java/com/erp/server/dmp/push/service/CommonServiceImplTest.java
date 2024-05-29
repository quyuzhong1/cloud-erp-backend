package com.erp.server.dmp.push.service;

import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.ErpServerDmpApplication;
import org.apache.groovy.util.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class CommonServiceImplTest {

    @Resource
    private CommonService commonService;
    @Resource
    private com.erp.server.dmp.push.service.kingdee.KingdeeCommonService KingdeeCommonService;

    @Test
    public void test(){
        PlatformEntity platformEntity = KingdeeCommonService.getPlatformEntity(PlatformEnum.KINGDEE.getDesc());
        List<Map<String,Object>> list = new ArrayList<>();
        list.add(Maps.of("supplierCode",200, "pricingUserCode", 100));
        list.add(Maps.of("price","100","maxQty", 10));
        List<Map<String, Object>> maps = commonService.makeApiFieldList(list, platformEntity.getId(), 9);
        System.out.println(maps);
    }

}