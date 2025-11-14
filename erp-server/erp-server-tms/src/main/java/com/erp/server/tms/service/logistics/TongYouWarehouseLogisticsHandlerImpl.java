package com.erp.server.tms.service.logistics;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.sdk.wms.tongyou.dto.response.TongYouBaseResp;
import com.sdk.wms.tongyou.service.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 通邮海外仓物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.TONG_YOU_WAREHOUSE)
public class TongYouWarehouseLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private TongYouService tongYouService;

    @Resource
    private LogisticsAuthFieldService logisticsAuthFieldService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return success();
    }

    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        Map<String,Object> map = new HashMap<>();
        map.put("token",authMap.get("appToken"));
        TongYouBaseResp<String> authResp = tongYouService.getLogisticsChannel(map);
        if(!isSuccess(authResp)){
            throw new ServiceException(getPlatForm().getName() +"授权失败,"+authResp.getContent());
        }
        return success();
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.TONG_YOU_WAREHOUSE;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }

    public <T> boolean isSuccess(TongYouBaseResp<T> resp){
        return CharSequenceUtil.equals(resp.getError(),"T");
    }
}
