package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.UniqueDto;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.oms.dto.OmsMongoDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.mongo.MongoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class MongoServiceTest {

    @Resource
    private MongoService mongoService;

    @Test
    public void getTask() {
        UniqueDto uniqueDto = UniqueDto.getUniqId("FBA17DQYF9HR");
        List<PlatformAmazonFbaShipmentDTO> mongoData = mongoService.findMongoData(uniqueDto, 0, 0, MongoTableNameContant.THIRD_SYSTEM_AMAZON_FBA_SHIPMENT, PlatformAmazonFbaShipmentDTO.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }



        PlatformAmazonFbaShipmentDTO mongoDatum = mongoData.get(0);
        // 指定字段更新
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mapUtil.put("downloadStatus", 0);

        OmsMongoDTO updateDto = new OmsMongoDTO(mongoDatum.getUniqueId());
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.THIRD_SYSTEM_AMAZON_FBA_SHIPMENT, PlatformAmazonFbaShipmentDTO.class);
    }
}