package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.erp.server.wms.ErpServerWmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class PlatformInboundConsumerServiceTest {

    @Resource
    private PlatformInboundConsumerService service;

    @Test
    public void handleTest() {
        PlatformInboundDTO dto = new PlatformInboundDTO();
        dto.setWarehousePlatformType(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.code);
        dto.setReceivingCode("test123456");
        dto.setProvider("iml");
        dto.setPlatform("iml");
        dto.setDownloadTime(LocalDateTime.now());
        dto.setReceivingStatus(OverseasInstockStatusEnum.FINISH.getCode());
        List<PlatformInboundDTO.Item> itemList = new ArrayList<>();
        dto.setItems(itemList);
        PlatformInboundDTO.Item item = new PlatformInboundDTO.Item();
        item.setProductSku("2807");
        item.setReceivedQuantity(100);
        itemList.add(item);
        dto.setHasReceivedData(true);
        List<PlatformInboundDTO.Receiving> receivings = new ArrayList<>();
        LocalDateTime localDateTime = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
        PlatformInboundDTO.Receiving receiving = new PlatformInboundDTO.Receiving();
        receiving.setProductSku("2807");
        receiving.setReceiveQty(100);
        receiving.setReceiveTime(localDateTime);
        receivings.add(receiving);
        dto.setReceivingDataList(receivings);
        service.handle(JSONUtil.parse(dto));
    }
}