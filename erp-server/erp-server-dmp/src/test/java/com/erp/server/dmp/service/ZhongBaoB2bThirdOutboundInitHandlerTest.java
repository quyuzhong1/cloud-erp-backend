package com.erp.server.dmp.service;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.server.dmp.inout.handler.input.task.init.api.zhongbao.ZhongBaoB2bThirdOutboundInitHandler;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ZhongBaoB2bThirdOutboundInitHandlerTest {

    @Test
    public void shouldUseZhongBaoActionStatuses() throws Exception {
        ZhongBaoB2bThirdOutboundInitHandler handler = new ZhongBaoB2bThirdOutboundInitHandler();
        setProviderCode(handler, "zhongbao");

        ThirdWarehouseQueryFbaOutboundResponse response = new ThirdWarehouseQueryFbaOutboundResponse();
        response.setStatus("-1");
        assertTrue((Boolean) invoke(handler, "isActionStatus", response));

        response.setStatus("1");
        assertFalse((Boolean) invoke(handler, "isActionStatus", response));
    }

    @Test
    public void shouldUseZhongBaoErrorReasonAndOutboundTime() throws Exception {
        ZhongBaoB2bThirdOutboundInitHandler handler = new ZhongBaoB2bThirdOutboundInitHandler();
        setProviderCode(handler, "zhongbao");

        ThirdWarehouseQueryFbaOutboundResponse response = new ThirdWarehouseQueryFbaOutboundResponse();
        response.setStatus("-2");
        response.setCode("SFFH260330000001");
        response.setPlatformOrderCode("WB-001");
        response.setErrorReason("众包异常原因");
        response.setDeliveryTimeStr("2026-05-07 15:15:26");
        JSONObject result = (JSONObject) invoke(handler, "toResult", response);
        assertEquals("众包异常原因", result.getString("abnormalProblemReason"));
        assertEquals("2026-05-07 15:15:26", result.getString("dateShippingStr"));
        assertEquals("-2", result.getString("orderStatus"));
    }

    private Object invoke(Object target, String methodName, Object arg) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, arg.getClass());
        method.setAccessible(true);
        return method.invoke(target, arg);
    }

    private void setProviderCode(ZhongBaoB2bThirdOutboundInitHandler handler, String providerCode) throws Exception {
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
