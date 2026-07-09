package com.erp.server.tms.service.logistics;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.vo.request.LogisticsRegisterVO;
import com.erp.model.tms.vo.request.RegisterTrackVO;
import com.erp.model.tms.vo.response.RegisterResponseVO;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.kuaidi100.model.request.Kuaidi100SubscribeParam;
import com.sdk.tms.kuaidi100.model.response.Kuaidi100SubscribeResponse;
import com.sdk.tms.kuaidi100.service.Kuaidi100Service;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class Kuaidi100LogisticsHandlerImplTest {

    @Test
    public void registerLogisticsNumberSubscribesEachWaybillAndTreatsDuplicateAsSuccess() throws Exception {
        Kuaidi100LogisticsHandlerImpl handler = new Kuaidi100LogisticsHandlerImpl();
        Kuaidi100Service kuaidi100Service = spy(new Kuaidi100Service());
        CfgSettingService cfgSettingService = mock(CfgSettingService.class);
        LogisticsOperateService logisticsOperateService = mock(LogisticsOperateService.class);
        setField(handler, "kuaidi100Service", kuaidi100Service);
        setField(handler, "cfgSettingService", cfgSettingService);
        setField(handler, "logisticsOperateService", logisticsOperateService);
        doReturn("https://erp.test/").when(cfgSettingService).getKuaidi100CallBackUrl();
        doReturn(response(true, "200", "success"),
                response(false, "501", "duplicate"),
                response(false, "400", "bad request"))
                .when(kuaidi100Service).subscribe(any(Kuaidi100SubscribeParam.class));

        ApiResult<List<RegisterResponseVO>> result = handler.registerLogisticsNumber(RegisterTrackVO.builder()
                .authMap(authMap())
                .logisticsRegisterVOS(Arrays.asList(registerVO("YT001"), registerVO("YT002"), registerVO("YT003")))
                .build());

        assertTrue(result.isSuccess());
        assertEquals(3, result.getData().size());
        assertTrue(result.getData().get(0).getTrackStatus());
        assertEquals("200", result.getData().get(0).getCode());
        assertTrue(result.getData().get(1).getTrackStatus());
        assertEquals("501", result.getData().get(1).getCode());
        assertFalse(result.getData().get(2).getTrackStatus());
        assertEquals("400", result.getData().get(2).getCode());
        ArgumentCaptor<Kuaidi100SubscribeParam> captor = ArgumentCaptor.forClass(Kuaidi100SubscribeParam.class);
        verify(kuaidi100Service, org.mockito.Mockito.times(3)).subscribe(captor.capture());
        assertEquals("https://erp.test/webhook/kuaidi100/push", captor.getAllValues().get(0).getParameters().getCallbackurl());
        assertEquals(Kuaidi100Service.SALT, captor.getAllValues().get(0).getParameters().getSalt());
    }

    @Test
    public void registerLogisticsNumberMissingEnvReturnsFailuresWithoutSubscribe() throws Exception {
        Kuaidi100LogisticsHandlerImpl handler = new Kuaidi100LogisticsHandlerImpl();
        Kuaidi100Service kuaidi100Service = mock(Kuaidi100Service.class);
        CfgSettingService cfgSettingService = mock(CfgSettingService.class);
        setField(handler, "kuaidi100Service", kuaidi100Service);
        setField(handler, "cfgSettingService", cfgSettingService);
        doReturn("").when(cfgSettingService).getKuaidi100CallBackUrl();

        ApiResult<List<RegisterResponseVO>> result = handler.registerLogisticsNumber(RegisterTrackVO.builder()
                .authMap(authMap())
                .logisticsRegisterVOS(Collections.singletonList(registerVO("YT001")))
                .build());

        assertTrue(result.isSuccess());
        assertFalse(result.getData().get(0).getTrackStatus());
        assertEquals("ENV_URL_EMPTY", result.getData().get(0).getCode());
        verify(kuaidi100Service, never()).subscribe(any(Kuaidi100SubscribeParam.class));
    }

    @Test
    public void registerLogisticsNumberMissingCourierCodeReturnsParamFailure() throws Exception {
        Kuaidi100LogisticsHandlerImpl handler = new Kuaidi100LogisticsHandlerImpl();
        Kuaidi100Service kuaidi100Service = mock(Kuaidi100Service.class);
        CfgSettingService cfgSettingService = mock(CfgSettingService.class);
        setField(handler, "kuaidi100Service", kuaidi100Service);
        setField(handler, "cfgSettingService", cfgSettingService);
        doReturn("https://erp.test").when(cfgSettingService).getKuaidi100CallBackUrl();

        ApiResult<List<RegisterResponseVO>> result = handler.registerLogisticsNumber(RegisterTrackVO.builder()
                .authMap(authMap())
                .logisticsRegisterVOS(Collections.singletonList(LogisticsRegisterVO.builder()
                        .trackNo("YT001")
                        .courierCode("")
                        .build()))
                .build());

        assertTrue(result.isSuccess());
        assertFalse(result.getData().get(0).getTrackStatus());
        assertEquals("PARAM_EMPTY", result.getData().get(0).getCode());
        verify(kuaidi100Service, never()).subscribe(any(Kuaidi100SubscribeParam.class));
    }

    @Test
    public void registerLogisticsNumberMissingAuthKeyReturnsFailuresWithoutSubscribe() throws Exception {
        Kuaidi100LogisticsHandlerImpl handler = new Kuaidi100LogisticsHandlerImpl();
        Kuaidi100Service kuaidi100Service = mock(Kuaidi100Service.class);
        CfgSettingService cfgSettingService = mock(CfgSettingService.class);
        setField(handler, "kuaidi100Service", kuaidi100Service);
        setField(handler, "cfgSettingService", cfgSettingService);
        doReturn("https://erp.test").when(cfgSettingService).getKuaidi100CallBackUrl();

        ApiResult<List<RegisterResponseVO>> result = handler.registerLogisticsNumber(RegisterTrackVO.builder()
                .authMap(Collections.emptyMap())
                .logisticsRegisterVOS(Collections.singletonList(registerVO("YT001")))
                .build());

        assertTrue(result.isSuccess());
        assertFalse(result.getData().get(0).getTrackStatus());
        assertEquals("AUTH_KEY_EMPTY", result.getData().get(0).getCode());
        verify(kuaidi100Service, never()).subscribe(any(Kuaidi100SubscribeParam.class));
    }

    @Test
    public void registerLogisticsNumberSubscribeReturnsNullReturnsSubscribeException() throws Exception {
        Kuaidi100LogisticsHandlerImpl handler = new Kuaidi100LogisticsHandlerImpl();
        Kuaidi100Service kuaidi100Service = spy(new Kuaidi100Service());
        CfgSettingService cfgSettingService = mock(CfgSettingService.class);
        LogisticsOperateService logisticsOperateService = mock(LogisticsOperateService.class);
        setField(handler, "kuaidi100Service", kuaidi100Service);
        setField(handler, "cfgSettingService", cfgSettingService);
        setField(handler, "logisticsOperateService", logisticsOperateService);
        doReturn("https://erp.test").when(cfgSettingService).getKuaidi100CallBackUrl();
        doReturn(null).when(kuaidi100Service).subscribe(any(Kuaidi100SubscribeParam.class));

        ApiResult<List<RegisterResponseVO>> result = handler.registerLogisticsNumber(RegisterTrackVO.builder()
                .authMap(authMap())
                .logisticsRegisterVOS(Collections.singletonList(registerVO("YT001")))
                .build());

        assertTrue(result.isSuccess());
        assertFalse(result.getData().get(0).getTrackStatus());
        assertEquals("SUBSCRIBE_EXCEPTION", result.getData().get(0).getCode());
    }

    @Test
    public void registerLogisticsNumberSubscribeThrowsExceptionReturnsSubscribeException() throws Exception {
        Kuaidi100LogisticsHandlerImpl handler = new Kuaidi100LogisticsHandlerImpl();
        Kuaidi100Service kuaidi100Service = spy(new Kuaidi100Service());
        CfgSettingService cfgSettingService = mock(CfgSettingService.class);
        LogisticsOperateService logisticsOperateService = mock(LogisticsOperateService.class);
        setField(handler, "kuaidi100Service", kuaidi100Service);
        setField(handler, "cfgSettingService", cfgSettingService);
        setField(handler, "logisticsOperateService", logisticsOperateService);
        doReturn("https://erp.test").when(cfgSettingService).getKuaidi100CallBackUrl();
        org.mockito.Mockito.doThrow(new RuntimeException("network error"))
                .when(kuaidi100Service).subscribe(any(Kuaidi100SubscribeParam.class));

        ApiResult<List<RegisterResponseVO>> result = handler.registerLogisticsNumber(RegisterTrackVO.builder()
                .authMap(authMap())
                .logisticsRegisterVOS(Collections.singletonList(registerVO("YT001")))
                .build());

        assertTrue(result.isSuccess());
        assertFalse(result.getData().get(0).getTrackStatus());
        assertEquals("SUBSCRIBE_EXCEPTION", result.getData().get(0).getCode());
        assertEquals("network error", result.getData().get(0).getMsg());
    }

    private LogisticsRegisterVO registerVO(String trackNo) {
        return LogisticsRegisterVO.builder()
                .trackNo(trackNo)
                .courierCode("yuantong")
                .phoneSuffix("13800138000")
                .build();
    }

    private Map<String, String> authMap() {
        Map<String, String> authMap = new HashMap<>();
        authMap.put("key", "appKey");
        return authMap;
    }

    private Kuaidi100SubscribeResponse response(Boolean result, String returnCode, String message) {
        return Kuaidi100SubscribeResponse.builder()
                .result(result)
                .returnCode(returnCode)
                .message(message)
                .build();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
