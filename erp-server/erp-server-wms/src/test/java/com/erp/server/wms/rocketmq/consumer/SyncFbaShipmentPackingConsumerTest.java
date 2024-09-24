package com.erp.server.wms.rocketmq.consumer;

import com.erp.model.wms.dto.FbaShipmentPackingDTO;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.impl.FbaShipmentPackingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
class SyncFbaShipmentPackingConsumerTest {

    @Resource
    private FbaShipmentPackingServiceImpl service;

    @Test
    void handle() {
        FbaShipmentPackingDTO.PackingDTO data = new FbaShipmentPackingDTO.PackingDTO();
        data.setFbaShipmentCode("FBA1864D6XDS");
        data.setBoxNo("FBA1864D6XDSU00001");
        List<FbaShipmentPackingDTO.PackingDetailDTO> detailDTOList = new ArrayList<>();
        FbaShipmentPackingDTO.PackingDetailDTO detailDTO = new FbaShipmentPackingDTO.PackingDetailDTO();
        detailDTO.setAsin("B089ZS8CC6");
        detailDTO.setMsku("2134-CA10");
        detailDTO.setFnSku("X0045BE6E7");
        detailDTO.setQty(20);
        detailDTOList.add(detailDTO);
        data.setDetailDTOList(detailDTOList);
        service.handle(data);
    }
}