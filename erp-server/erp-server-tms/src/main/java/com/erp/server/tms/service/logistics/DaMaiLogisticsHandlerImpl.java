package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.wms.damai.dto.response.DaMaiBaseResp;
import com.sdk.wms.damai.dto.response.DaMaiChannelResp;
import com.sdk.wms.damai.dto.response.DaMaiWarehouseResp;
import com.sdk.wms.damai.service.DaMaiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;


/**
 * 极风物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.DA_MAI)
public class DaMaiLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private DaMaiService daMaiService;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        Map<String, String> authMap = chanelQueryVO.getAuthMap();
        Map<String,Object> map = new HashMap<>();
        map.put("appToken", authMap.get("appToken"));
        map.put("appKey", authMap.get("appKey"));
        DaMaiBaseResp<List<DaMaiChannelResp>> authResp = daMaiService.getChannel(map);
        if(!isSuccess(authResp)){
            return failure("获取渠道失败:" + authResp.getMsg());
        }
        List<LogisticsSaleChannelEntity> response = new ArrayList<>();
        List<DaMaiChannelResp> daMaiChannelResps = authResp.getData();
        for (DaMaiChannelResp daMaiChannelResp : daMaiChannelResps) {
            LogisticsSaleChannelEntity logisticsSaleChannelEntity = new LogisticsSaleChannelEntity();
            logisticsSaleChannelEntity.setPlatformChannelId(daMaiChannelResp.getCarriersCode());
            logisticsSaleChannelEntity.setCode(daMaiChannelResp.getCarriersCode());
            logisticsSaleChannelEntity.setCnName(daMaiChannelResp.getCarriersName());
            logisticsSaleChannelEntity.setEnName(daMaiChannelResp.getCarriersName());
            logisticsSaleChannelEntity.setLogisticsPlatform(OmsPlatformEnum.DA_MAI.getCode());
            response.add(logisticsSaleChannelEntity);
        }
        return success(response);
    }

    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        Map<String,Object> map = new HashMap<>();
        map.put("appToken", authMap.get("appToken"));
        map.put("appKey", authMap.get("appKey"));
        DaMaiBaseResp<List<DaMaiWarehouseResp>> authResp = daMaiService.getWarehouseList(map);
        if(!isSuccess(authResp)){
            return failure("授权失败:" + authResp.getMsg());
        }
        return success();
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.DA_MAI;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }

    public <T> boolean isSuccess(DaMaiBaseResp<T> resp){
        return resp.getStatus() != null && resp.getStatus().equals("success");
    }

}
