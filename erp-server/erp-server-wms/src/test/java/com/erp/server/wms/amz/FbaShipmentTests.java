package com.erp.server.wms.amz;

import cn.hutool.json.JSONUtil;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import com.erp.server.wms.service.FbaShipmentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.OffsetDateTime;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class FbaShipmentTests {

    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private FbaShipmentReceiveService fbaShipmentReceiveService;



    @Test
    public void delFind() {
        FbaShipmentEntity entity = fbaShipmentService.getByFbaShipmentId("FBA16GQW2VDC");
        System.out.println(JSONUtil.toJsonStr(entity));
    }

    @Test
    public void getById() {
        String id = "1722870061489410053";
        FbaShipmentReceiveEntity entity = fbaShipmentReceiveService.getById(id);
        System.out.println("实体结果");
        System.out.println(JSONUtil.toJsonStr(entity));
        System.out.println("时区字段结果");
//        OffsetDateTime receiveDateLocale = entity.getReceiveDateLocale();
        String dateString = "2024-01-20T00:00:00+04:00";
        // 将字符串解析为 OffsetDateTime
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString);

//        if (null != receiveDateLocale){
//            System.out.println(receiveDateLocale);
//        }else {
//          entity.setReceiveDateLocale(entity.getReceiveDate().atOffset(ZoneOffset.of("+8")));
            entity.setReceiveLocaleDate(offsetDateTime);
            boolean result = fbaShipmentReceiveService.updateById(entity);
            FbaShipmentReceiveEntity updateEntity = fbaShipmentReceiveService.getById(id);
            if (null != updateEntity.getReceiveLocaleDate()) {
                System.out.println(updateEntity.getReceiveLocaleDate());
            }
//        }
    }




}
