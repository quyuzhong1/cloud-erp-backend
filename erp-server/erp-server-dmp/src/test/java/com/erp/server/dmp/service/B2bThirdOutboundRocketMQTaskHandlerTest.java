package com.erp.server.dmp.service;

import com.common.business.dto.PlatformOutboundDTO;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import com.erp.server.dmp.inout.handler.output.task.mq.B2bThirdOutboundRocketMQTaskHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class B2bThirdOutboundRocketMQTaskHandlerTest {

    private final TestB2bThirdOutboundRocketMQTaskHandler handler = new TestB2bThirdOutboundRocketMQTaskHandler();

    @Test
    public void shouldConvertDaMaiStatus() {
        DmpThirdOutboundEntity entity = buildEntity("damai", "BLOCK");

        PlatformOutboundDTO dto = handler.convert(entity, "cfg-output-id");

        assertEquals(ThirdDeliveryStatusEnum.INTERCEPTING.getCode(), dto.getOrderStatus());
        assertEquals("BLOCK", dto.getThirdOrderStatus());
        assertEquals("damai", dto.getProvider());
    }

    @Test
    public void shouldConvertZhongBaoStatus() {
        DmpThirdOutboundEntity entity = buildEntity("zhongbao", "-2");
        entity.setAbnormalProblemReason("众包异常");

        PlatformOutboundDTO dto = handler.convert(entity, "cfg-output-id");

        assertEquals(ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode(), dto.getOrderStatus());
        assertEquals("-2", dto.getThirdOrderStatus());
        assertEquals("众包异常", dto.getAbnormalProblemReason());
    }

    @Test
    public void shouldIgnoreZhongBaoNoActionStatus() {
        DmpThirdOutboundEntity entity = buildEntity("zhongbao", "1");

        PlatformOutboundDTO dto = handler.convert(entity, "cfg-output-id");

        assertNull(dto);
    }

    private DmpThirdOutboundEntity buildEntity(String sourcePlatform, String orderStatus) {
        DmpThirdOutboundEntity entity = new DmpThirdOutboundEntity();
        entity.setId("1");
        entity.setSourcePlatform(sourcePlatform);
        entity.setReferenceNo("SFFH260330000001");
        entity.setOrderCode("ORDER-001");
        entity.setOrderStatus(orderStatus);
        entity.setTrackingNo("TRACK-001");
        return entity;
    }

    private static class TestB2bThirdOutboundRocketMQTaskHandler extends B2bThirdOutboundRocketMQTaskHandler {
        @Override
        protected boolean validateDataBlack(Object object, String cfgOutputId) {
            return false;
        }
    }
}
