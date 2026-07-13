package com.erp.server.dmp.inout.handler.input.task.finish;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.handler.Track123WebhookPayloadParser;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.service.DmpLogisticsTrackWebhookRecordService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class Track123WebhookFinishHandlerTest {

    private static final String PLATFORM_CODE = PlatformDictEnum.TRACK123.getCode();

    @Test
    public void afterToDoUpdateTaskStatus_finishSuccessMarksWebhookRecords() throws Exception {
        TestableTrack123WebhookFinishHandler handler = new TestableTrack123WebhookFinishHandler();
        DmpLogisticsTrackWebhookRecordService service = mock(DmpLogisticsTrackWebhookRecordService.class);
        setField(handler, "dmpLogisticsTrackWebhookRecordService", service);
        setUpdateTaskStatus(handler, DmpInputTaskStatusEnum.FINISH);
        DmpInputFinishResponse response = new DmpInputFinishResponse();
        Map<String, Object> row1 = new HashMap<>();
        row1.put(Track123WebhookPayloadParser.WEBHOOK_RECORD_ID, "r1");
        Map<String, Object> row2 = new HashMap<>();
        row2.put(Track123WebhookPayloadParser.WEBHOOK_RECORD_ID, "r2");
        response.getConvertInputMongoEntityListMaps()
                .put(new DmpCfgInputConvertEntity(), Arrays.asList(row1, row2, row1));

        handler.invokeAfter(new DmpInputTaskRequest(), response, true);

        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(service).markFinish(org.mockito.Matchers.eq(PLATFORM_CODE), captor.capture());
        assertEquals(new HashSet<>(Arrays.asList("r1", "r2")), new HashSet<>(captor.getValue()));
    }

    @Test
    public void afterToDoUpdateTaskStatus_updateFailedDoesNotMarkWebhookRecords() throws Exception {
        TestableTrack123WebhookFinishHandler handler = new TestableTrack123WebhookFinishHandler();
        DmpLogisticsTrackWebhookRecordService service = mock(DmpLogisticsTrackWebhookRecordService.class);
        setField(handler, "dmpLogisticsTrackWebhookRecordService", service);
        setUpdateTaskStatus(handler, DmpInputTaskStatusEnum.FINISH);

        handler.invokeAfter(new DmpInputTaskRequest(), new DmpInputFinishResponse(), false);

        verifyNoMoreInteractions(service);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void setUpdateTaskStatus(Object target, DmpInputTaskStatusEnum status) throws Exception {
        Field field = com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler.class
                .getDeclaredField("updateTaskStatus");
        field.setAccessible(true);
        field.set(target, status);
    }

    private static class TestableTrack123WebhookFinishHandler extends Track123WebhookFinishHandler {
        private void invokeAfter(DmpInputTaskRequest request, DmpInputFinishResponse response, boolean updateSuccess) {
            afterToDoUpdateTaskStatus(request, response, updateSuccess);
        }
    }
}
