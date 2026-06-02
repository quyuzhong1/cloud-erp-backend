package com.erp.server.tms.service.logistics;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WEGO 物流接口处理器
 * <p>
 * WEGO 授权属于特殊场景，无需调用 WEGO 第三方授权接口，
 * 仅需将入参中的 appSecret、appToken 落库到物流授权字段表。
 * 字段落库由 {@code LogisticsAuthServiceImpl#add} 中的
 * {@code logisticsAuthFieldService.saveOrUpdateAuthField} 统一完成，
 * 这里只需校验必填字段后返回授权成功，避免控制器走回滚分支。
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.WEGO)
public class WegoLogisticsHandlerImpl extends AbstractLogisticsHandler {

    private static final String APP_SECRET = "appSecret";
    private static final String APP_TOKEN = "appToken";

    @Override
    public ApiResult<Object> authorization(Map<String, String> authMap) {
        if (authMap == null) {
            throw new ServiceException("授权信息不能为空");
        }
        String appSecret = authMap.get(APP_SECRET);
        String appToken = authMap.get(APP_TOKEN);
        if (CharSequenceUtil.isBlank(appSecret)) {
            throw new ServiceException("appSecret不能为空");
        }
        if (CharSequenceUtil.isBlank(appToken)) {
            throw new ServiceException("appToken不能为空");
        }
        return success("授权成功");
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return success(new ArrayList<>());
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.WEGO;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }
}
