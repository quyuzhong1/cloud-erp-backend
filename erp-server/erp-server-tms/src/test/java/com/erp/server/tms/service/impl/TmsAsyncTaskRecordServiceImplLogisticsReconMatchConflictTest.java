package com.erp.server.tms.service.impl;

import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TmsAsyncTaskMethodTypeEnum;
import com.erp.model.tms.enums.TmsAsyncTaskRecordBusinessTypeEnum;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TmsAsyncTaskRecordServiceImplLogisticsReconMatchConflictTest {

    private TmsAsyncTaskRecordServiceImpl service;

    @Before
    public void setUp() {
        service = new TmsAsyncTaskRecordServiceImpl();
    }

    @Test
    public void extractLogisticsReconBusinessIdsShouldParsePayloadMainId() throws Exception {
        String json = buildEnvelopeJson("2077242700333756417", true);

        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) invokePrivate("extractLogisticsReconBusinessIds",
                new Class[]{String.class}, json);

        assertEquals(Collections.singletonList("2077242700333756417"), ids);
    }

    @Test
    public void extractLogisticsReconBusinessIdsShouldParseLegacyIds() throws Exception {
        TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO payload =
                new TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO();
        payload.setIds(java.util.Arrays.asList("2077242700333756417", "2077281074952192001"));
        payload.setIsConfirm(true);
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = new TmsAsyncTaskRecordDTO.TaskEnvelopeDTO();
        envelope.setBusinessType(TmsAsyncTaskRecordBusinessTypeEnum.LOGISTICS_RECON.getCode());
        envelope.setMethodType(TmsAsyncTaskMethodTypeEnum.LOGISTICS_RECON_MATCH.getCode());
        envelope.setPayloadType("logisticsRecon:logisticsReconMatch");
        envelope.setPayloadVersion(1);
        envelope.setPayloadJson(JSONUtil.toJsonStr(payload));
        String json = JSONUtil.toJsonStr(envelope);

        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) invokePrivate("extractLogisticsReconBusinessIds",
                new Class[]{String.class}, json);

        assertEquals(java.util.Arrays.asList("2077242700333756417", "2077281074952192001"), ids);
    }

    @Test
    public void validateLogisticsReconIdShouldBlockWhenBusinessIdOverlaps() throws Exception {
        String requestJson = buildEnvelopeJson("2077242700333756417", true);
        TmsAsyncTaskRecordEntity runningTask = new TmsAsyncTaskRecordEntity();
        runningTask.setCode("Z280715000035");
        runningTask.setDataJson(buildEnvelopeJson("2077242700333756417", false));

        try {
            invokePrivate("validateLogisticsReconIdTaskNotConflict",
                    new Class[]{String.class, List.class, String.class, String.class, String.class},
                    requestJson,
                    Collections.singletonList(runningTask),
                    TmsAsyncTaskRecordBusinessTypeEnum.LOGISTICS_RECON.getCode(),
                    TmsAsyncTaskMethodTypeEnum.LOGISTICS_RECON_MATCH.getCode(),
                    "所选对账单正在匹配中，请稍后重试或联系管理员");
            fail("expected ServiceException");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("所选对账单正在匹配中"));
        }
    }

    @Test
    public void validateLogisticsReconIdShouldFailWhenRequestMainIdMissing() throws Exception {
        String requestJson = buildEnvelopeJson(null, true);
        TmsAsyncTaskRecordEntity runningTask = new TmsAsyncTaskRecordEntity();
        runningTask.setCode("Z280715000035");
        runningTask.setDataJson(buildEnvelopeJson("2077242700333756417", false));

        try {
            invokePrivate("validateLogisticsReconIdTaskNotConflict",
                    new Class[]{String.class, List.class, String.class, String.class, String.class},
                    requestJson,
                    Collections.singletonList(runningTask),
                    TmsAsyncTaskRecordBusinessTypeEnum.LOGISTICS_RECON.getCode(),
                    TmsAsyncTaskMethodTypeEnum.LOGISTICS_RECON_MATCH.getCode(),
                    "所选对账单正在匹配中，请稍后重试或联系管理员");
            fail("expected ServiceException");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("任务参数缺少对账单业务id"));
            assertTrue(e.getMessage().contains("无法创建任务"));
        }
    }

    @Test
    public void validateLogisticsReconIdShouldAllowWhenBusinessIdsDoNotOverlap() throws Exception {
        String requestJson = buildEnvelopeJson("2077281074952192001", true);
        TmsAsyncTaskRecordEntity runningTask = new TmsAsyncTaskRecordEntity();
        runningTask.setCode("Z280715000035");
        runningTask.setDataJson(buildEnvelopeJson("2077242700333756417", false));

        invokePrivate("validateLogisticsReconIdTaskNotConflict",
                new Class[]{String.class, List.class, String.class, String.class, String.class},
                requestJson,
                Collections.singletonList(runningTask),
                TmsAsyncTaskRecordBusinessTypeEnum.LOGISTICS_RECON.getCode(),
                TmsAsyncTaskMethodTypeEnum.LOGISTICS_RECON_MATCH.getCode(),
                "所选对账单正在匹配中，请稍后重试或联系管理员");
    }

    private String buildEnvelopeJson(String mainId, boolean isConfirm) {
        TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO payload =
                new TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO(mainId, isConfirm);
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = new TmsAsyncTaskRecordDTO.TaskEnvelopeDTO();
        envelope.setBusinessType(TmsAsyncTaskRecordBusinessTypeEnum.LOGISTICS_RECON.getCode());
        envelope.setMethodType(TmsAsyncTaskMethodTypeEnum.LOGISTICS_RECON_MATCH.getCode());
        envelope.setPayloadType("logisticsRecon:logisticsReconMatch");
        envelope.setPayloadVersion(1);
        envelope.setPayloadJson(JSONUtil.toJsonStr(payload));
        return JSONUtil.toJsonStr(envelope);
    }

    private Object invokePrivate(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = TmsAsyncTaskRecordServiceImpl.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        try {
            return method.invoke(service, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }
}
