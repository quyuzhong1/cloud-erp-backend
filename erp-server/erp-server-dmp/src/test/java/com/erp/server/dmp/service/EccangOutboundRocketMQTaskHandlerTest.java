package com.erp.server.dmp.service;

import com.common.business.dto.PlatformOutboundDTO;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.erp.server.dmp.inout.handler.output.task.mq.eccang.EccangOutboundRocketMQTaskHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EccangOutboundRocketMQTaskHandlerTest {

    private final TestEccangOutboundRocketMQTaskHandler handler = new TestEccangOutboundRocketMQTaskHandler();

    @Test
    public void shouldConvertSnakeCaseDetailListJsonToItems() {
        DmpThirdOutboundEntity entity = new DmpThirdOutboundEntity();
        entity.setId("2069348059523330049");
        entity.setSourcePlatform("antu");
        entity.setOrderCode("A001-260623-0001");
        entity.setOrderStatus("D");
        entity.setDetailListJson("[{\"product_sku\":\"3PL-1C-TEST\",\"quantity\":\"3\"},{\"product_sku\":\"3836959\",\"quantity\":\"2\"}]");

        PlatformOutboundDTO dto = handler.convert(entity, "cfg-output-id");

        assertNotNull(dto);
        assertEquals(2, dto.getItems().size());
        assertEquals("3PL-1C-TEST", dto.getItems().get(0).getProductSku());
        assertEquals(Integer.valueOf(3), dto.getItems().get(0).getActualQty());
        assertEquals("3836959", dto.getItems().get(1).getProductSku());
        assertEquals(Integer.valueOf(2), dto.getItems().get(1).getActualQty());
    }

    private static class TestEccangOutboundRocketMQTaskHandler extends EccangOutboundRocketMQTaskHandler {
        @Override
        protected boolean validateDataBlack(Object object, String cfgOutputId) {
            return false;
        }
    }
}
