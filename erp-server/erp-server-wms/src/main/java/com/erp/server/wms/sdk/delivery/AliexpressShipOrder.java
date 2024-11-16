package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.OrderDeliveryMarkTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrderDetail;
import com.erp.oms.aliexpress.dto.response.OrderItemDetail;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.wms.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.ALI_EXPRESS)
public class AliexpressShipOrder extends AbstractShipOrder {

    public static final String ALI_API_FAIL = "速卖通API标记发货失败:";
    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Resource
    private DictBasicService dictBasicService;

    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        // 查询所有信息
        Tuple tuple = super.allSourceOrderInfo(dto);
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = tuple.get(1);
        // 当前单据物流信息
        SoB2cLogisticsEntity logisticsEntity = tuple.get(2);
        //渠道
        String channelId = logisticsEntity.getLogisticsChannelId();

        //获取销售渠道信息
        LogisticsChannelDTO.SignShipDTO tmsSignShipDTO = logisticsFeign.getScaleChannelByChannelById(
                channelId,
                PlatformDictEnum.ALI_EXPRESS.getCode()
        );
        if (null == tmsSignShipDTO) {
            throw new ServiceException("找不到渠道信息");
        }

        List<String> signShippedDetailList = new ArrayList<>();
        for (SoB2cEntity mainEntity : sourceOrderList) {
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> currentDetailEntityList = soB2cDetailEntityListMap.get(mainEntity.getId());
            if (CollectionUtils.isEmpty(currentDetailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            // 校验捆绑商品拆分
            // 来源明细ID为空代表是手工添加的明细忽略
            currentDetailEntityList = currentDetailEntityList.stream()
                    .filter(e -> CharSequenceUtil.isNotBlank(e.getSourceDetailId()))
                    .collect(Collectors.toList());
            List<SoB2cDetailEntity> detailEntityList = super.handleSplit(currentDetailEntityList, dto.isFalseDeliveryFlag());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                log.warn("【速卖通标记发货】订单【{}】所有明细来源ID为空,不请求速卖通接口", mainEntity.getCode());
                continue;
            }

            //获取渠道标发单号
            String standardOrderType = tmsSignShipDTO.checkAndGetOrderDeliveryMarkType();
            String logisticsNo = CharSequenceUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(), standardOrderType)
                    ? logisticsEntity.getCode() : logisticsEntity.getTrackNo();
            if (CharSequenceUtil.isBlank(logisticsNo)) {
                throw new ServiceException("【速卖通标记发货】操作失败，渠道标发单号为空");
            }
            // 查询订单详情(获取子声明下标)
            AliExpressOrderDetail orderDetail = aliExpressOrderService.getOrderDetailByOrderIdAndShopId(mainEntity.getPlatformCode(), mainEntity.getShopId());
            if (null == orderDetail){
                log.error("【速卖通标记发货】订单【{}】查询订单详情为空", mainEntity.getPlatformCode());
                throw new ServiceException("查询订单详情为空");
            }
            List<OrderItemDetail> childOrderList = orderDetail.getChildOrderList();
            if (CollectionUtils.isEmpty(childOrderList)){
                log.error("【速卖通标记发货】订单【{}】订单明细列表为空", mainEntity.getPlatformCode());
                throw new ServiceException("订单明细列表为空");
            }
            // 得到当前标记的子订单下标
            List<String> sourceDetailIds = detailEntityList.stream().map(SoB2cDetailEntity::getSourceDetailId).collect(Collectors.toList());
            List<String> subOrderIndexList = childOrderList.stream()
                    .filter(e -> sourceDetailIds.contains(e.getChildOrderId()))
                    .map(OrderItemDetail::getOrderSortId)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(subOrderIndexList)){
                log.error("【速卖通标记发货】订单【{}】数据异常未匹配到有效子订单下标: 需要标记的sourceDetailIds={}, 子订单={}",
                        mainEntity.getPlatformCode(),
                        sourceDetailIds,
                        subOrderIndexList
                );
                throw new ServiceException("【速卖通标记发货】订单【{}】数据异常未匹配到有效子订单下标");
            }
            // 都是全部发货
            String sendType = "all";

            DeclareDeliverRequest request = DeclareDeliverRequest.builder()
                    .outRef(mainEntity.getPlatformCode())
                    .logisticsNo(logisticsNo)
                    .shopId(mainEntity.getShopId())
                    .shopName(mainEntity.getShopName())
                    .serviceName(tmsSignShipDTO.getCode())
                    .sendType(sendType)
                    .actualCarrier(tmsSignShipDTO.getCarrierCode())
                    .trackingWebSite(tmsSignShipDTO.getLogisticsTrackUrl())
                    .subTradeOrderIndexList(subOrderIndexList)
                    .build();

            // 非线上环境需要指定订单ID
            if (!BusinessCommonConstants.hasProfile("prod")) {
                List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("aliexpressAllowShipOrderId");
                if (CollectionUtils.isEmpty(warehouseTypes)) {
                    log.warn("【速卖通标记发货】【{}】不存在指定的订单ID配置,不请求速卖通接口:请求参数={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(request));
                    signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
                    continue;
                }
                // 允许通过的ID
                DictBasicDTO.ListDTO configAllowPlatformOrderDTO = warehouseTypes.stream().filter(e -> mainEntity.getPlatformCode().equalsIgnoreCase(e.getValue())).findFirst().orElse(null);
                if (null == configAllowPlatformOrderDTO) {
                    log.warn("【速卖通标记发货】【{}】不属于配置指定的订单ID,不请求速卖通接口:请求参数={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(request));
                    signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
                    continue;
                }
            }

            try {
                aliExpressOrderService.subDeclareDeliver(request);
                signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            } catch (ServiceException e){
                if (-353 == e.getCode()) {
                    log.warn("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货API提示重复操作(忽略) >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                    return signShippedDetailList;
                }
                if (-999 == e.getCode() && e.getMessage().contains("系统已经重新路由")){
                    //更新跟踪号，重新申明下单
                    return reShipOrder(e, detailEntityList, request, signShippedDetailList, mainEntity);
                }
                log.error("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货API提示异常 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                throw new ServiceException(ALI_API_FAIL + e.getMessage());
            } catch (Exception e) {
                log.error("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货失败 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                throw new ServiceException("速卖通标记发货失败:" + e.getMessage());
            }
        }
        return signShippedDetailList;
    }

    /**
     * 重新进行声明发货
     * @param serviceException
     * @param detailEntityList
     * @param request
     * @param signShippedDetailList
     * @param mainEntity
     * @return
     */
    private List<String> reShipOrder(ServiceException serviceException, List<SoB2cDetailEntity> detailEntityList,
                                     DeclareDeliverRequest request,List<String> signShippedDetailList,SoB2cEntity mainEntity) {
        //重置参数 更新物流记录
        //系统已经重新路由，旧单号[CNG00665032598857]暂无法使用，请使用新单号[UN055958577MU]声明发货
        String message = serviceException.getMessage();
        // 正则表达式，匹配方括号及其内部的内容
        String regex = "\\[(.*?)\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        // 提取第一个方括号内的内容
        String firstContent = "";
        if (matcher.find()) {
            firstContent = matcher.group(1); // 获取第一个方括号内部的内容
        }
        // 提取第二个方括号内的内容
        String secondContent = "";
        if (matcher.find()) {
            secondContent = matcher.group(1); // 获取第二个方括号内部的内容
        }
        if (!firstContent.equals(request.getLogisticsNo()) || CharSequenceUtil.isBlank(firstContent) || CharSequenceUtil.isBlank(secondContent)){
            //整合不符合格式则抛出异常
            log.error("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货API更新物流单异常 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), message);
            throw new ServiceException(ALI_API_FAIL + serviceException.getMessage());
        }
        //更新物流记录
        soB2cFeign.updateLogisticsBySoId(mainEntity.getId(), secondContent);
        //重置发货声明订单号
        request.setTrackingWebSite(secondContent);
        try {
            aliExpressOrderService.subDeclareDeliver(request);
            signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            return signShippedDetailList;
        } catch (ServiceException e){
            if (-353 == e.getCode()) {
                log.warn("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货API提示重复操作(忽略) >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                return signShippedDetailList;
            }
            log.error("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货API提示异常 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
            throw new ServiceException(ALI_API_FAIL + e.getMessage());
        } catch (Exception e) {
            log.error("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货失败 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
            throw new ServiceException("速卖通标记发货失败:" + e.getMessage());
        }
    }

    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        Boolean isCancel = dto.getOldIsCancel();
        if (isCancel) {
            //订单拦截
            soB2cFeign.deliveryIntercept(new SoB2cDTO.RemarkDTO(dto.getSoB2cId(), "平台取消或退款"));
        }
        return isCancel;
    }


    @Override
    public Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList) {
        return false;
    }
}
