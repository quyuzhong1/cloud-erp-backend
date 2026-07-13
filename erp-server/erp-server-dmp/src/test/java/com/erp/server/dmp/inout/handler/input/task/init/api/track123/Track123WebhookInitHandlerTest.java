package com.erp.server.dmp.inout.handler.input.task.init.api.track123;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.entity.DmpLogisticsTrackWebhookRecordEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.service.DmpLogisticsTrackWebhookRecordService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Track123WebhookInitHandlerTest {

    private static final String PLATFORM_CODE = PlatformDictEnum.TRACK123.getCode();

    @Test
    public void getApiData_validConfig_claimsRecordsAndBuildsInitDto() throws Exception {
        Track123WebhookInitHandler handler = new Track123WebhookInitHandler();
        DmpLogisticsTrackWebhookRecordService service = mock(DmpLogisticsTrackWebhookRecordService.class);
        setField(handler, "dmpLogisticsTrackWebhookRecordService", service);
        DmpInputApiInitRequest request = new DmpInputApiInitRequest();
        request.setRequestParam("{\"batchSize\":2,\"ingTimeoutMinutes\":9}");
        when(service.claimLatestWaitRecords(PLATFORM_CODE, 2)).thenReturn(Arrays.asList(
                record("r1", "{\"data\":{\"trackNo\":\"T123\",\"transitStatus\":\"DELIVERED\"}}")
        ));

        List<DmpInputTaskInitDTO> result = handler.getApiData(request);

        verify(service).prepareRecords(PLATFORM_CODE, 9);
        verify(service).claimLatestWaitRecords(PLATFORM_CODE, 2);
        assertEquals(1, result.size());
        Map<String, Object> payload = JSON.parseObject(result.get(0).getMsg());
        assertEquals("T123", payload.get("trackNo"));
        assertEquals("r1", payload.get("webhookRecordId"));
    }

    @Test
    public void getApiData_invalidConfigUsesDefaultValues() throws Exception {
        Track123WebhookInitHandler handler = new Track123WebhookInitHandler();
        DmpLogisticsTrackWebhookRecordService service = mock(DmpLogisticsTrackWebhookRecordService.class);
        setField(handler, "dmpLogisticsTrackWebhookRecordService", service);
        DmpInputApiInitRequest request = new DmpInputApiInitRequest();
        request.setRequestParam("{bad");

        List<DmpInputTaskInitDTO> result = handler.getApiData(request);

        verify(service).prepareRecords(PLATFORM_CODE, 30);
        verify(service).claimLatestWaitRecords(PLATFORM_CODE, 100);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getApiData_recordParseFailed_marksRecordError() throws Exception {
        Track123WebhookInitHandler handler = new Track123WebhookInitHandler();
        DmpLogisticsTrackWebhookRecordService service = mock(DmpLogisticsTrackWebhookRecordService.class);
        setField(handler, "dmpLogisticsTrackWebhookRecordService", service);
        when(service.claimLatestWaitRecords(PLATFORM_CODE, 100)).thenReturn(Arrays.asList(
                record("r2", "{\"data\":{\"transitStatus\":\"DELIVERED\"}}")
        ));

        List<DmpInputTaskInitDTO> result = handler.getApiData(new DmpInputApiInitRequest());

        assertTrue(result.isEmpty());
        verify(service).markError(PLATFORM_CODE, "r2", "清洗解析失败：Track123 webhook 原始数据缺少trackNo");
    }

    private DmpLogisticsTrackWebhookRecordEntity record(String id, String rawData) {
        DmpLogisticsTrackWebhookRecordEntity entity = new DmpLogisticsTrackWebhookRecordEntity();
        entity.setId(id);
        entity.setRawData(rawData);
        return entity;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
