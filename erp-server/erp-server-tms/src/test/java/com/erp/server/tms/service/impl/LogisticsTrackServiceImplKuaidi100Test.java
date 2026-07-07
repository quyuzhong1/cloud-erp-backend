package com.erp.server.tms.service.impl;

import com.common.business.enums.LogisticsTransportTypeEnum;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.sdk.tms.kuaidi100.service.Kuaidi100Service;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class LogisticsTrackServiceImplKuaidi100Test {

    @Test
    public void webhookByKuaidi100ConvertsAndSavesValidTracks() throws Exception {
        LogisticsTrackServiceImpl service = spy(new LogisticsTrackServiceImpl());
        LogisticsBillDetailService billDetailService = mock(LogisticsBillDetailService.class);
        setField(service, "kuaidi100Service", new Kuaidi100Service());
        setField(service, "logisticsBillDetailService", billDetailService);
        doNothing().when(service).saveIncrementTrackData(anyString(), anyList());

        service.webhookByKuaidi100(dto("YT123", "3", Arrays.asList(
                detail("2026-07-07 10:00:00", "in transit", "Hangzhou"),
                detail("2026-07-07 12:00:00", "signed", "Shanghai"))));

        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(service).saveIncrementTrackData(org.mockito.Mockito.eq("YT123"), listCaptor.capture());
        List<LogisticsTrackEntity> savedList = listCaptor.getValue();
        assertEquals(2, savedList.size());
        assertEquals("YT123", savedList.get(0).getTrackNo());
        assertEquals(LogisticTrackStatusEnum.SIGN.getCode(), savedList.get(0).getStatus());
        assertEquals("in transit", savedList.get(0).getContent());
        assertEquals("Hangzhou", savedList.get(0).getAddress());
        assertEquals(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode(), savedList.get(0).getTransportType());
        assertNotNull(savedList.get(0).getMd5());
        ArgumentCaptor<LogisticsTrackEntity> maxTrackCaptor = ArgumentCaptor.forClass(LogisticsTrackEntity.class);
        verify(billDetailService).updateLogisticsBillDetailByTrackNo(maxTrackCaptor.capture());
        assertEquals(LocalDateTime.of(2026, 7, 7, 12, 0, 0), maxTrackCaptor.getValue().getTrackTime());
        assertEquals(LogisticTrackStatusEnum.SIGN.getCode(), maxTrackCaptor.getValue().getOrderStatus());
    }

    @Test
    public void webhookByKuaidi100EmptyDataDoesNotSave() throws Exception {
        LogisticsTrackServiceImpl service = spy(new LogisticsTrackServiceImpl());
        LogisticsBillDetailService billDetailService = mock(LogisticsBillDetailService.class);
        setField(service, "kuaidi100Service", new Kuaidi100Service());
        setField(service, "logisticsBillDetailService", billDetailService);

        service.webhookByKuaidi100(dto("YT123", "3", Collections.emptyList()));

        verify(service, never()).saveIncrementTrackData(anyString(), anyList());
        verify(billDetailService, never()).updateLogisticsBillDetailByTrackNo(any(LogisticsTrackEntity.class));
    }

    @Test
    public void webhookByKuaidi100InvalidTrackDetailDoesNotSave() throws Exception {
        LogisticsTrackServiceImpl service = spy(new LogisticsTrackServiceImpl());
        LogisticsBillDetailService billDetailService = mock(LogisticsBillDetailService.class);
        setField(service, "kuaidi100Service", new Kuaidi100Service());
        setField(service, "logisticsBillDetailService", billDetailService);

        service.webhookByKuaidi100(dto("YT123", "3", Collections.singletonList(detail("bad-time", "signed", "Shanghai"))));

        verify(service, never()).saveIncrementTrackData(anyString(), anyList());
        verify(billDetailService, never()).updateLogisticsBillDetailByTrackNo(any(LogisticsTrackEntity.class));
    }

    private LogisticsTrackDTO.Kuaidi100WebHookDTO dto(String trackNo, String state, List<LogisticsTrackDTO.Kuaidi100TrackDetailDTO> data) {
        LogisticsTrackDTO.Kuaidi100LastResultDTO lastResult = new LogisticsTrackDTO.Kuaidi100LastResultDTO();
        lastResult.setNu(trackNo);
        lastResult.setState(state);
        lastResult.setData(data);
        LogisticsTrackDTO.Kuaidi100WebHookDTO dto = new LogisticsTrackDTO.Kuaidi100WebHookDTO();
        dto.setLastResult(lastResult);
        return dto;
    }

    private LogisticsTrackDTO.Kuaidi100TrackDetailDTO detail(String time, String context, String areaName) {
        LogisticsTrackDTO.Kuaidi100TrackDetailDTO detail = new LogisticsTrackDTO.Kuaidi100TrackDetailDTO();
        detail.setTime(time);
        detail.setContext(context);
        detail.setAreaName(areaName);
        return detail;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
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
