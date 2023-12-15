package com.erp.server.wms.service.impl;

import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDetailDTO;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.SoB2cDeliveryService;
import net.sf.cglib.core.Local;
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
public class PackingInspectionServiceImplTest {

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Test
    public void scan() {
        SoB2cDeliveryDTO.AddDTO dto = new SoB2cDeliveryDTO.AddDTO();
        dto.setSourceCode("XSD23101700024");
        dto.setSourceType("123");
        dto.setSourceId("1714182964738265090");
        dto.setDeliveryTime(LocalDateTime.now());
        dto.setSoCode("XSD23101700024");
        List<SoB2cDeliveryDetailDTO.AddDTO> details = new ArrayList<>();
        SoB2cDeliveryDetailDTO.AddDTO detail = new SoB2cDeliveryDetailDTO.AddDTO();
        detail.setSkuId("1727572624252342273");
        detail.setSourceDetailId("1727572624252342273");
        detail.setDeliveryQty(10);
        details.add(detail);
        dto.setDetailList(details);
        soB2cDeliveryService.add(dto);
    }
}