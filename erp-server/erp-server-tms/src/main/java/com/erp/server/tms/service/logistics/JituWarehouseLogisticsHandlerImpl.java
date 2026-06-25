package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.wms.goodcang.service.GoodCangService;
import com.sdk.wms.jitu.dto.request.WarehouseRequest;
import com.sdk.wms.jitu.dto.response.WarehouseResponse;
import com.sdk.wms.jitu.service.JituService;
import com.sdk.wms.zhongbao.dto.request.ChannelRequest;
import com.sdk.wms.zhongbao.dto.request.CommonRequest;
import com.sdk.wms.zhongbao.dto.request.PageRequest;
import com.sdk.wms.zhongbao.dto.response.BaseResponse;
import com.sdk.wms.zhongbao.dto.response.ChannelResponse;
import com.sdk.wms.zhongbao.service.ZhongbaoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 谷仓物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.JI_TU)
public class JituWarehouseLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsOperateService logisticsOperateService;

    @Resource
    private GoodCangService goodCangService;
    @Resource
    private ZhongbaoService zhongbaoService;
    @Resource
    private JituService jituService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        try {
            Map<String, Object> authObjMap = authMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (Object) e.getValue()));
            ThirdWarehouseContext.setAuthMap(authObjMap);
            WarehouseRequest request = WarehouseRequest.builder().customerid((String) authObjMap.getOrDefault("customerid", "")).build();
            WarehouseResponse response = jituService.warehouseList(authObjMap, request);
            if ("true".equals(response.getResponseitems().get(0).getSuccess())) {
                return success("授权成功");
            }else {
                return failure("授权失败:" + response.getResponseitems().get(0).getErrorMsg());
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }finally {
            ThirdWarehouseContext.remove();
        }
    }
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.ZHONG_BAO;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
