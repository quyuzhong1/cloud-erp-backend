package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.enums.ApiError;
import com.erp.model.dmp.entity.DmpLogisticsTrackWebhookRecordEntity;
import com.erp.model.dmp.enums.DmpLogisticsTrackWebhookRecordStatusEnum;
import com.erp.server.dmp.mapper.DmpLogisticsTrackWebhookRecordMapper;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DmpLogisticsTrackWebhookRecordServiceImplTest {

    private static final String TRACK123_PLATFORM_CODE = PlatformDictEnum.TRACK123.getCode();

    @Test
    public void saveTrack123RawRecord_validRawData_savesAndUpdatesWaitRecord() throws Exception {
        DmpLogisticsTrackWebhookRecordMapper mapper = mock(DmpLogisticsTrackWebhookRecordMapper.class);
        DmpLogisticsTrackWebhookRecordServiceImpl service = serviceWithMapper(mapper);
        List<DmpLogisticsTrackWebhookRecordEntity> insertedSnapshots = new ArrayList<>();
        List<DmpLogisticsTrackWebhookRecordEntity> updatedSnapshots = new ArrayList<>();
        when(mapper.insert(org.mockito.Matchers.any(DmpLogisticsTrackWebhookRecordEntity.class))).thenAnswer(invocation -> {
            insertedSnapshots.add(copyRecord((DmpLogisticsTrackWebhookRecordEntity) invocation.getArguments()[0]));
            return 1;
        });
        when(mapper.updateById(org.mockito.Matchers.any(DmpLogisticsTrackWebhookRecordEntity.class))).thenAnswer(invocation -> {
            updatedSnapshots.add(copyRecord((DmpLogisticsTrackWebhookRecordEntity) invocation.getArguments()[0]));
            return 1;
        });
        String rawData = "{\"data\":{\"trackNo\":\"T123\",\"transitStatus\":\"DELIVERED\"}}";

        DmpLogisticsTrackWebhookRecordEntity actualEntity = service.saveTrack123RawRecord(rawData);

        DmpLogisticsTrackWebhookRecordEntity insertedEntity = insertedSnapshots.get(0);
        assertEquals(TRACK123_PLATFORM_CODE, insertedEntity.getPlatformCode());
        assertEquals("", insertedEntity.getTrackNo());
        assertEquals(DmpLogisticsTrackWebhookRecordStatusEnum.WAIT.getCode(), insertedEntity.getStatus());
        assertEquals(rawData, insertedEntity.getRawData());

        DmpLogisticsTrackWebhookRecordEntity updatedEntity = updatedSnapshots.get(0);
        assertEquals("T123", updatedEntity.getTrackNo());
        assertEquals(DmpLogisticsTrackWebhookRecordStatusEnum.WAIT.getCode(), updatedEntity.getStatus());
        assertEquals("", updatedEntity.getRemark());
        assertEquals("T123", actualEntity.getTrackNo());
    }

    @Test
    public void saveTrack123RawRecord_missingTrackNo_savesErrorRecord() throws Exception {
        DmpLogisticsTrackWebhookRecordMapper mapper = mock(DmpLogisticsTrackWebhookRecordMapper.class);
        DmpLogisticsTrackWebhookRecordServiceImpl service = serviceWithMapper(mapper);
        ArgumentCaptor<DmpLogisticsTrackWebhookRecordEntity> updateCaptor =
                ArgumentCaptor.forClass(DmpLogisticsTrackWebhookRecordEntity.class);
        when(mapper.insert(org.mockito.Matchers.any(DmpLogisticsTrackWebhookRecordEntity.class))).thenReturn(1);
        when(mapper.updateById(updateCaptor.capture())).thenReturn(1);
        String rawData = "{\"data\":{\"transitStatus\":\"DELIVERED\"}}";

        DmpLogisticsTrackWebhookRecordEntity actualEntity = service.saveTrack123RawRecord(rawData);

        DmpLogisticsTrackWebhookRecordEntity updatedEntity = updateCaptor.getValue();
        assertEquals(DmpLogisticsTrackWebhookRecordStatusEnum.ERROR.getCode(), updatedEntity.getStatus());
        assertEquals(ApiError.DMP_TRACK123_WEBHOOK_TRACK_NO_REQUIRED.getMsg(), updatedEntity.getRemark());
        assertEquals(updatedEntity, actualEntity);
    }

    @Test
    public void prepareRecords_positiveTimeout_usesConfiguredTimeout() throws Exception {
        DmpLogisticsTrackWebhookRecordMapper mapper = mock(DmpLogisticsTrackWebhookRecordMapper.class);
        DmpLogisticsTrackWebhookRecordServiceImpl service = serviceWithMapper(mapper);

        service.prepareRecords(TRACK123_PLATFORM_CODE, 9);

        verify(mapper).skipCoveredRecords(TRACK123_PLATFORM_CODE, 9, "");
        verify(mapper).recoverTimeoutIngRecords(TRACK123_PLATFORM_CODE, 9, "");
    }

    @Test
    public void prepareRecords_nonPositiveTimeout_usesDefaultTimeout() throws Exception {
        DmpLogisticsTrackWebhookRecordMapper mapper = mock(DmpLogisticsTrackWebhookRecordMapper.class);
        DmpLogisticsTrackWebhookRecordServiceImpl service = serviceWithMapper(mapper);

        service.prepareRecords(TRACK123_PLATFORM_CODE, 0);

        verify(mapper).skipCoveredRecords(TRACK123_PLATFORM_CODE, 30, "");
        verify(mapper).recoverTimeoutIngRecords(TRACK123_PLATFORM_CODE, 30, "");
    }

    @Test
    public void claimLatestWaitRecords_limitExceedsMax_capsLimit() throws Exception {
        DmpLogisticsTrackWebhookRecordMapper mapper = mock(DmpLogisticsTrackWebhookRecordMapper.class);
        DmpLogisticsTrackWebhookRecordServiceImpl service = serviceWithMapper(mapper);
        List<DmpLogisticsTrackWebhookRecordEntity> expectedRecords =
                Collections.singletonList(new DmpLogisticsTrackWebhookRecordEntity());
        when(mapper.claimLatestWaitRecords(TRACK123_PLATFORM_CODE, 500)).thenReturn(expectedRecords);

        List<DmpLogisticsTrackWebhookRecordEntity> actualRecords =
                service.claimLatestWaitRecords(TRACK123_PLATFORM_CODE, 999);

        assertEquals(expectedRecords, actualRecords);
        verify(mapper).claimLatestWaitRecords(TRACK123_PLATFORM_CODE, 500);
    }

    @Test
    public void claimLatestWaitRecords_nonPositiveLimit_returnsEmptyList() throws Exception {
        DmpLogisticsTrackWebhookRecordMapper mapper = mock(DmpLogisticsTrackWebhookRecordMapper.class);
        DmpLogisticsTrackWebhookRecordServiceImpl service = serviceWithMapper(mapper);

        List<DmpLogisticsTrackWebhookRecordEntity> actualRecords =
                service.claimLatestWaitRecords(TRACK123_PLATFORM_CODE, 0);

        assertTrue(actualRecords.isEmpty());
        verify(mapper, never()).claimLatestWaitRecords(eq(TRACK123_PLATFORM_CODE), org.mockito.Matchers.anyInt());
    }

    @Test
    public void markFinish_emptyIds_doesNothing() throws Exception {
        DmpLogisticsTrackWebhookRecordMapper mapper = mock(DmpLogisticsTrackWebhookRecordMapper.class);
        DmpLogisticsTrackWebhookRecordServiceImpl service = serviceWithMapper(mapper);

        service.markFinish(TRACK123_PLATFORM_CODE, Collections.emptyList());

        verifyNoMoreInteractions(mapper);
    }

    @Test
    public void markFinish_validIds_updatesIngToFinish() throws Exception {
        DmpLogisticsTrackWebhookRecordMapper mapper = mock(DmpLogisticsTrackWebhookRecordMapper.class);
        DmpLogisticsTrackWebhookRecordServiceImpl service = serviceWithMapper(mapper);
        Collection<String> ids = Arrays.asList("r1", "r2");

        service.markFinish(TRACK123_PLATFORM_CODE, ids);

        verify(mapper).updateStatusByIds(ids,
                TRACK123_PLATFORM_CODE,
                DmpLogisticsTrackWebhookRecordStatusEnum.ING.getCode(),
                DmpLogisticsTrackWebhookRecordStatusEnum.FINISH.getCode(),
                "");
    }

    @Test
    public void markError_blankId_doesNothing() throws Exception {
        DmpLogisticsTrackWebhookRecordMapper mapper = mock(DmpLogisticsTrackWebhookRecordMapper.class);
        DmpLogisticsTrackWebhookRecordServiceImpl service = serviceWithMapper(mapper);

        service.markError(TRACK123_PLATFORM_CODE, "", "error");

        verifyNoMoreInteractions(mapper);
    }

    private DmpLogisticsTrackWebhookRecordServiceImpl serviceWithMapper(DmpLogisticsTrackWebhookRecordMapper mapper)
            throws Exception {
        DmpLogisticsTrackWebhookRecordServiceImpl service = new DmpLogisticsTrackWebhookRecordServiceImpl();
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(service, mapper);
        return service;
    }

    private DmpLogisticsTrackWebhookRecordEntity copyRecord(DmpLogisticsTrackWebhookRecordEntity source) {
        DmpLogisticsTrackWebhookRecordEntity target = new DmpLogisticsTrackWebhookRecordEntity();
        target.setPlatformCode(source.getPlatformCode());
        target.setTrackNo(source.getTrackNo());
        target.setStatus(source.getStatus());
        target.setRawData(source.getRawData());
        target.setRemark(source.getRemark());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }
}
