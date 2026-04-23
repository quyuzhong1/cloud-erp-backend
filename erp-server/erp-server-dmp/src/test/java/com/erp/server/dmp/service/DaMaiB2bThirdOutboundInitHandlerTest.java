package com.erp.server.dmp.service;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.server.dmp.inout.handler.input.task.init.api.damai.DaMaiB2bThirdOutboundInitHandler;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DaMaiB2bThirdOutboundInitHandlerTest {

    @Test
    public void shouldUseDaMaiActionStatuses() throws Exception {
        DaMaiB2bThirdOutboundInitHandler handler = new DaMaiB2bThirdOutboundInitHandler();
        setProviderCode(handler, "damai");

        ThirdWarehouseQueryFbaOutboundResponse response = new ThirdWarehouseQueryFbaOutboundResponse();
        response.setStatus("SUCCESS");
        assertTrue((Boolean) invoke(handler, "isActionStatus", response));

        response.setStatus("SUBMIT");
        assertFalse((Boolean) invoke(handler, "isActionStatus", response));
    }

    @Test
    public void shouldUseDaMaiErrorTypeInResult() throws Exception {
        DaMaiB2bThirdOutboundInitHandler handler = new DaMaiB2bThirdOutboundInitHandler();
        setProviderCode(handler, "damai");

        ThirdWarehouseQueryFbaOutboundResponse response = new ThirdWarehouseQueryFbaOutboundResponse();
        response.setStatus("BLOCK");
        response.setCode("SFFH260330000002");
        response.setPlatformOrderCode("FBA001");
        response.setErrorType("大卖异常原因");
        response.setDeliveryTimeStr("2026-05-08 10:00:00");
        response.setPlatformCreateTimeStr("2026-05-07 09:00:00");
        response.setWarehouseCode("DM-WH");
        response.setShippingMethod("UPS");
        response.setCarrierName("DAMAI");
        JSONObject result = (JSONObject) invoke(handler, "toResult", response);
        assertEquals("BLOCK", result.getString("orderStatus"));
        assertEquals("大卖异常原因", result.getString("abnormalProblemReason"));
        assertEquals("2026-05-08 10:00:00", result.getString("dateShippingStr"));
        assertEquals("2026-05-07 09:00:00", result.getString("platformCreateTimeStr"));
        assertEquals("DM-WH", result.getString("warehouseCode"));
        assertEquals("UPS", result.getString("shippingMethod"));
        assertEquals("DAMAI", result.getString("carrierName"));
        assertEquals("B2B", result.getString("orderType"));
    }

    private Object invoke(Object target, String methodName, Object arg) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, arg.getClass());
        method.setAccessible(true);
        return method.invoke(target, arg);
    }

    private void setProviderCode(DaMaiB2bThirdOutboundInitHandler handler, String providerCode) throws Exception {
        DmpBasicSystemEntity entity = new DmpBasicSystemEntity();
        entity.setCode(providerCode);
        Field field = findField(handler.getClass(), "dmpBasicSystemEntity");
        field.setAccessible(true);
        field.set(handler, entity);
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
