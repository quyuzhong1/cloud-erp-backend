package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.erp.tms.batong.model.label.base.BaseData;
import com.erp.tms.batong.service.BaTongService;
import com.sdk.tms.yanwen.dto.response.YanWenChannel;
import com.sdk.tms.yanwen.dto.response.YanWenResponse;
import com.sdk.tms.yanwen.server.YanWenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Lambda
 * @Classname BtLogisticsHandlerImpl
 * @Description 巴通物流商对接
 * @Date 2024-01-09 15:07
 * @Created by yl
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.BaTong)
public class BaTongLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private BaTongService baTongService;

    @Resource
    private LogisticsOperateService logisticsOperateService;


    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();

        return null;

    }

    /**
     * 获取物流原始渠道信息
     *
     * @param chanelQueryVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            List<BaseData> baseList = baTongService.listShippingMethod(chanelQueryVO.getAuthMap());
            List<LogisticsSaleChannelEntity> list = new ArrayList<>(baseList.size());
            if(CollectionUtils.isEmpty(baseList)) {
                return success(list);
            }
            list=LogisticsChannelConverter.INSTANCE.channelConvertByBaTong(baseList);
            logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(baseList));
          return success(list);
        }catch (Exception e){
            log.error("获取物流原始渠道信息异常 {}",e.getMessage());
        }

        return success(Collections.emptyList());
    }


    /**
     * 取消订单
     * @param logisticsCancelOrderList
     * @return
     */
    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsCancelOrderList) {
        LogisticsCancelOrderVO logisticsCancelOrderVO = logisticsCancelOrderList.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        Map<String,String> authMap=logisticsCancelOrderVO.getAuthMap();
        if(Objects.isNull(authMap)){
            return failure("缺少授权信息");
        }
        Boolean isSuccess = true;
        List<CancelResponseVO> responseList = new ArrayList<>(logisticsCancelOrderList.size());
        for(LogisticsCancelOrderVO item:logisticsCancelOrderList){
            try {
                CancelResponseVO responseVO = new CancelResponseVO();
               Boolean result= baTongService.deleteOrder(authMap,item.getDeliveryNo());
               if(result){
                   responseVO.setDeliveryNo(item.getDeliveryNo());
               }
                responseList.add(responseVO);
            }catch (Exception e){
                logisticsOperateService.pushOperateLog(item.getAuthMap().get("id"),
                        item.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(item), JSONUtil.toJsonStr(e));
                isSuccess = false;
            }

        }
        return isSuccess ? success(responseList) : failure(responseList);

    }
}
