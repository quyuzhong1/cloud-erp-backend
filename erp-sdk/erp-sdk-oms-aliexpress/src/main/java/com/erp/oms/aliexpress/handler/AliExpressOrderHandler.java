package com.erp.oms.aliexpress.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.PlatformAliExpressOrderDTO;
import com.erp.oms.aliexpress.dto.request.AddressRequest;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.*;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-29 10:11
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.ALI_EXPRESS)
@BusinessType(BusinessTypeEnum.ORDER)
public class AliExpressOrderHandler extends AbstractOrderHandler<PlatformAliExpressOrderDTO, PlatformOrderDTO> {


    @Resource
    private AliExpressOrderService aliExpressOrderService;


    /**
     * 下载主数据
     */
    @Override
    public List<PlatformAliExpressOrderDTO> download(JobTaskDTO data) {
//        String apiName = AliexpressConstants.LIST_ORDER;
        String apiName = data.getApiCode();
        AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(data.getShopId());
        if (null == shopInfoDTO) {
            log.error("[速卖通订单下载]  获取 token 失败: shopId={}", data.getShopId());
            String msg = StrUtil.format("[速卖通订单下载]  获取 token 失败: shopId={}", data.getShopId());
            throw new ServiceException(msg);
        }
        // 订单状态
        String orderStatus = "";
        List<String> orderStatusList = new LinkedList<>();
        // 是否请求历史订单(请求历史订单不带更新时间区间)
        boolean historyQuery = false;
        Map<String, Object> apiParam = data.getApiParam();
        if (!apiParam.isEmpty() && apiParam.containsKey("order_status")){
            orderStatus = (String) apiParam.get("order_status");
        }
        if (!apiParam.isEmpty() && apiParam.containsKey("order_status_list")) {
            Object listObj = apiParam.get("order_status_list");
            if (null != listObj){
                orderStatusList = (List<String>) listObj;
            }
        }
        if (!apiParam.isEmpty() && apiParam.containsKey("history_query")) {
            Object historyQueryObj = apiParam.get("history_query");
            if (null != historyQueryObj){
                historyQuery = (Boolean) historyQueryObj;
            }
        }

//        性能问题：
//        aliexpress.trade.seller.orderlist.get 获取卖家已结束的订单
//        必须显式提供已结束状态作为order_status入参，同时加上创建时间作为查询条件。请注意：如果要使用修改时间作为入参，必须加上创建时间，且创建开始时间和创建结束时间的范围不能超过30天（大促期间可能会缩小）。
//        由于卖家已结束的订单量比较大，建议把订单切分成按创建时间的天获取，减少单次请求对数据库的记录扫描量，以提升效率。
//        aliexpress.trade.seller.orderlist.get 获取增量订单（不包括已结束的订单）
//        建议显式指定order_status入参，同时加上修改时间作为查询条件，以减少数据库的记录扫描量。请注意：如果要使用修改时间作为入参，必须加上创建时间，且创建开始时间和创建结束时间的范围不能超过180天（大促期间可能会缩小）。
//        https://open.aliexpress.com/doc/doc.htm#/?docId=641

        String formatStr = DateUtil.fmt;
        // 上次执行时间(数据提前10分钟)
        // 格式: yyyy-mm-dd hh:mm:ss。此时间为美国太平洋时间
        LocalDateTime lastTime = data.getLastTime()
                .minusMinutes(10)
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .atZoneSameInstant(ZoneId.of(AliExpressOrderService.ALIEXPRESS_TIME_ZONE))
                .toLocalDateTime()
                ;
        // 下次执行时间
        // 格式: yyyy-mm-dd hh:mm:ss。此时间为美国太平洋时间
        LocalDateTime nextTime = data.getNextTime()
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .atZoneSameInstant(ZoneId.of(AliExpressOrderService.ALIEXPRESS_TIME_ZONE))
                .toLocalDateTime();
        // 创建开始时间
        String createDateStart;
        // 创建结束时间
        String createDateEnd;
        // 请求的更新开始时间
        String modifyStartTime = "";
        // 请求的更新结束时间
        String modifyEndTime = "";

        if (historyQuery){
            // 请求历史订单不带更新时间区间
            // 创建开始时间
            createDateStart = LocalDateUtil.formatTime(lastTime, formatStr);
            // 创建结束时间
            createDateEnd = LocalDateUtil.formatTime(nextTime, formatStr);
        } else {
            // 请求增量订单必须带更新时间区间和创建时间区间(完结订单:间隔不超过30天)
            // 请求的更新开始时间
            modifyStartTime = LocalDateUtil.formatTime(lastTime, formatStr);
            // 请求的更新结束时间
            modifyEndTime = LocalDateUtil.formatTime(nextTime, formatStr);

            // 设置开始时间和结束时间
            // 创建开始时间
            // 完结订单：更新结束时间 - 30 天 = 创建开始时间
            LocalDateTime startLocalDateTime = nextTime.minusDays(30);
            createDateStart =  LocalDateUtil.formatTime(startLocalDateTime, formatStr);
            // 创建结束时间 = 更新结束时间
            createDateEnd = modifyEndTime;
        }

        OrderRequest orderRequest = OrderRequest.builder().
                clientId(shopInfoDTO.getClientId()).
                clientSecret(shopInfoDTO.getClientSecret())
                .startTime(modifyStartTime)
                .endTime(modifyEndTime)
                .baseUrl(shopInfoDTO.getBaseUrl())
                .apiName(apiName)
                .currentPage(1)
                .token(shopInfoDTO.getToken())
                .orderStatus(orderStatus)
                .orderStatusList(orderStatusList)
                .createDateStart(createDateStart)
                .createDateEnd(createDateEnd)
                .build();
        List<AliExpressOrder> orderList;
        try {
            orderList = aliExpressOrderService.allOrder(orderRequest);
        } catch (Exception e) {
            log.error("获取速卖通订单数据异常:{}", ExceptionUtil.stacktraceToString(e));
            throw new RuntimeException(e);
        }
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }

        return orderList.stream()
                .map(e -> new PlatformAliExpressOrderDTO(data, e, shopInfoDTO))
                .collect(Collectors.toList());

    }

    /**
     * 查询平台发货单
     */
    public List<PlatformAliExpressOrderDTO> getDeliveryList(AliExpressShopInfoDTO shopInfoDTO, List<PlatformAliExpressOrderDTO> orderList) {
        String deliveryQueryAPiName = AliexpressConstants.ALIEXPRESS_ASCP_FFO_QUERY;
        OrderRequest deliveryRequest = OrderRequest.builder().
                clientId(shopInfoDTO.getClientId()).
                clientSecret(shopInfoDTO.getClientSecret()).
                baseUrl(shopInfoDTO.getBaseUrl()).
                apiName(deliveryQueryAPiName).
                token(shopInfoDTO.getToken()).build();
        List<ErpFulfillmentForwardDtoBean> deliveryList = new ArrayList<>();
        List<String> orderIdList = orderList.stream().map(req -> req.getAliExpressOrder().getOrderId()).distinct().collect(Collectors.toList());
        fillOrderPlatform(orderList, shopInfoDTO);
        //20个分片拆分，平台只支持一次查询20个;
        List<List<String>> partition = Lists.partition(orderIdList, 20);
        for (List<String> list : partition) {
            deliveryList.addAll(aliExpressOrderService.listDeliveryQuery(deliveryRequest, list));
        }
        if (CollectionUtils.isEmpty(deliveryList)){
            return orderList;
        }
        Map<String, List<ErpFulfillmentForwardDtoBean>> deliveryMap = deliveryList.stream()
                .collect(Collectors.groupingBy(ErpFulfillmentForwardDtoBean::getTradeOrderNo));

        for (PlatformAliExpressOrderDTO platformAliExpressOrderDTO : orderList) {
            List<ErpFulfillmentForwardDtoBean> curDeliveryList = deliveryMap.get(platformAliExpressOrderDTO.getAliExpressOrder().getOrderId());
            if (!CollectionUtils.isEmpty(curDeliveryList)){
                platformAliExpressOrderDTO.setAliExpressDeliveryDTOList(curDeliveryList);
                // 已下载
                platformAliExpressOrderDTO.setDownloadDeliveryStatus(1);
                platformAliExpressOrderDTO.setDownloadDeliveryDetailStatus(0);
            } else {
                // 无发货单
                platformAliExpressOrderDTO.setDownloadDeliveryStatus(-1);
                platformAliExpressOrderDTO.setDownloadDeliveryDetailStatus(-1);
            }
            List<LogisitcsDTO> logisticInfoList = platformAliExpressOrderDTO.getAliExpressOrder().getDetail().getLogisticInfoList();
            if (!CollectionUtils.isEmpty(logisticInfoList)){
                for (LogisitcsDTO logisitcsDTO : logisticInfoList) {
                    ErpFulfillmentForwardDtoBean erpFulfillmentForwardDtoBean = deliveryList.stream()
                            .filter(e -> StringUtils.isNotBlank(e.getTradeOrderNo()) && StringUtils.isNotBlank(e.getTrackingNo()) )
                            .filter(req -> req.getTradeOrderNo().equals(platformAliExpressOrderDTO.getAliExpressOrder().getOrderId()) && req.getTrackingNo().equals(logisitcsDTO.getLogisticsNo()))
                            .findFirst().orElse(null);
                    if (null != erpFulfillmentForwardDtoBean) {
                        logisitcsDTO.setWarehouseName(erpFulfillmentForwardDtoBean.getWarehouseName());
                    } else {
                        logisitcsDTO.setWarehouseName("");
                    }
                }
//                List<ErpFulfillmentForwardDtoBean> erpFulfillmentForwardDtoBeanList = deliveryList.stream().filter(req -> req.getTradeOrderNo().equals(platformAliExpressOrderDTO.getAliExpressOrder().getOrderId())).collect(Collectors.toList());
//                platformAliExpressOrderDTO.setAliExpressDeliveryDetailList(new ArrayList<>());
//                for (ErpFulfillmentForwardDtoBean erpFulfillmentForwardDtoBean : erpFulfillmentForwardDtoBeanList) {
//                    //封装发货明细
//                    List<AliExpressDeliveryDetail> aliExpressDeliveryDetailList = aliExpressOrderService.listDeliveryDetailQuery(deliveryRequest,erpFulfillmentForwardDtoBean.getFulfillmentOrderNo());
//                    aliExpressDeliveryDetailList.forEach(v->v.setWarehouseName(erpFulfillmentForwardDtoBean.getWarehouseName()));
//                    platformAliExpressOrderDTO.getAliExpressDeliveryDetailList().addAll(aliExpressDeliveryDetailList);
//                }
            }
        }
        return orderList;
    }

    private void fillOrderPlatform(List<PlatformAliExpressOrderDTO> orderList, AliExpressShopInfoDTO shopInfoDTO) {
        if (CollectionUtils.isEmpty(orderList) || Objects.isNull(shopInfoDTO)) {
            return;
        }
        for (PlatformAliExpressOrderDTO orderDTO : orderList) {
            orderDTO.setAliExpressShopInfoDTO(shopInfoDTO);
            if (StringUtils.isNotBlank(shopInfoDTO.getDictPlatform())) {
                orderDTO.setPlatform(shopInfoDTO.getDictPlatform());
            }
        }
    }

    @Override
    public List<PlatformOrderDTO> convert(List<PlatformAliExpressOrderDTO> sourceDataList) {
//        if(!CollectionUtils.isEmpty(sourceDataList)){
//            getDeliveryList(sourceDataList.get(0).getAliExpressShopInfoDTO(), sourceDataList);
//        }
        return sourceDataList.stream()
                // 组装
                .map(PlatformAliExpressOrderDTO::convertDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }


    /**
     * 下载地址信息
     */
    public PlatformAliExpressOrderDTO downloadAddress(PlatformAliExpressOrderDTO dto) {
        AliExpressOrder order = dto.getAliExpressOrder();
        String orderId = order.getOrderId();
        AliExpressOrderDetail orderDetail = dto.getAliExpressOrder().getDetail();
        if (Objects.isNull(orderDetail)) {
            return dto;
        }
        String shopId=dto.getShopId();
        AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(shopId);
        if (null == shopInfoDTO) {
            log.error("[速卖通订单地址明细下载]  获取 token 失败: shopId={}", shopId);
            String msg = StrUtil.format("[速卖通订单地址明细下载]  获取 token 失败: shopId={}", shopId);
            throw new ServiceException(msg);
        }
        dto.setAliExpressShopInfoDTO(shopInfoDTO);
        if (StringUtils.isNotBlank(shopInfoDTO.getDictPlatform())) {
            dto.setPlatform(shopInfoDTO.getDictPlatform());
        }
        String oaid = orderDetail.getOaid();
        //加密id
        if (StringUtils.isBlank(oaid)) {
            return dto;
        }
        AddressRequest request=AddressRequest.builder().
                clientId(shopInfoDTO.getClientId()).
                clientSecret(shopInfoDTO.getClientSecret()).
                baseUrl(shopInfoDTO.getBaseUrl()).
                token(shopInfoDTO.getToken()).
                oaid(oaid).
                orderId(orderId).
                build();
        try {
            BuyerTradeAddress address=aliExpressOrderService.getBuyerTradeAddress(request);
            if(Objects.nonNull(address)){
                order.setBuyerSignerFullname(address.getBuyerSignerFullname());
                orderDetail.setBuyerSignerFullname(address.getBuyerSignerFullname());
                ReceiptInfo receiptInfo=orderDetail.getReceiptAddress();
                receiptInfo.setAddress2(address.getAddress2());
                receiptInfo.setContactPerson(address.getContactPerson());
                receiptInfo.setDetailAddress(address.getDetailAddress());
                receiptInfo.setPhoneNumber(address.getPhoneNumber());
                receiptInfo.setMobileNo(address.getMobileNo());
                orderDetail.setReceiptAddress(receiptInfo);
                BuyerInfo buyerInfo= orderDetail.getBuyerInfo();
                buyerInfo.setFirstName(address.getFirstName());
                orderDetail.setBuyerInfo(buyerInfo);
                order.setDetail(orderDetail);
            }
            dto.setAliExpressOrder(order);
            return dto;
        }catch (Exception e){
            throw new ServiceException("查询速卖通订单地址失败"+ JSONUtil.toJsonStr(e));
        }
    }

    @Override
    public PlatformAliExpressOrderDTO downloadDetail(PlatformAliExpressOrderDTO dto, JSONObject extendObj) {
        String shopId = dto.getShopId();
        String apiName = AliexpressConstants.LIST_ORDER;
        AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(shopId);
        if (null == shopInfoDTO) {
            log.error("[速卖通订单明细下载]  获取 token 失败: shopId={}", shopId);
            String msg = StrUtil.format("[速卖通订单下载]  获取 token 失败: shopId={}", shopId);
            throw new ServiceException(msg);
        }
        dto.setAliExpressShopInfoDTO(shopInfoDTO);
        if (StringUtils.isNotBlank(shopInfoDTO.getDictPlatform())) {
            dto.setPlatform(shopInfoDTO.getDictPlatform());
        }
        OrderRequest orderRequest = OrderRequest.builderByShopInfo(apiName, shopInfoDTO);
        try {
            AliExpressOrderDetail orderDetail = aliExpressOrderService.getOrderDetail(dto.getAliExpressOrder().getOrderId(), orderRequest);
            dto.getAliExpressOrder().setDetail(orderDetail);
            return dto;
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发货单明细下载
     */
    public PlatformAliExpressOrderDTO downloadDeliveryDetail(PlatformAliExpressOrderDTO dto) {
        List<ErpFulfillmentForwardDtoBean> expressDeliveryDTOList = dto.getAliExpressDeliveryDTOList();
        if (CollectionUtils.isEmpty(expressDeliveryDTOList)){
            log.error("[速卖通发货单明细下载] 数据异常:未找到发货单数据: orderId={}", dto.getAliExpressOrder().getOrderId());
            String msg = StrUtil.format("[速卖通发货单明细下载] 数据异常:未找到发货单数据: orderId={}", dto.getAliExpressOrder().getOrderId());
            throw new ServiceException(msg);
        }
        String shopId = dto.getShopId();
        AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(shopId);
        if (null == shopInfoDTO) {
            log.error("[速卖通发货单明细下载]  获取 token 失败: shopId={}", shopId);
            String msg = StrUtil.format("[速卖通订单下载]  获取 token 失败: shopId={}", shopId);
            throw new ServiceException(msg);
        }
        dto.setAliExpressShopInfoDTO(shopInfoDTO);
        if (StringUtils.isNotBlank(shopInfoDTO.getDictPlatform())) {
            dto.setPlatform(shopInfoDTO.getDictPlatform());
        }
        String deliveryQueryAPiName = AliexpressConstants.ALIEXPRESS_ASCP_FFO_ITEM_QUERY;;
        OrderRequest deliveryRequest = OrderRequest.builder().
                clientId(shopInfoDTO.getClientId()).
                clientSecret(shopInfoDTO.getClientSecret()).
                baseUrl(shopInfoDTO.getBaseUrl()).
                apiName(deliveryQueryAPiName).
                token(shopInfoDTO.getToken()).build();

        List<AliExpressDeliveryDetail> resultDetailList = new LinkedList<>();
        for (ErpFulfillmentForwardDtoBean erpFulfillmentForwardDtoBean : expressDeliveryDTOList) {
            //封装发货明细
            List<AliExpressDeliveryDetail> aliExpressDeliveryDetailList = aliExpressOrderService.listDeliveryDetailQuery(deliveryRequest, erpFulfillmentForwardDtoBean.getFulfillmentOrderNo());
            aliExpressDeliveryDetailList.forEach(v -> v.setWarehouseName(erpFulfillmentForwardDtoBean.getWarehouseName()));
            resultDetailList.addAll(aliExpressDeliveryDetailList);
        }
        dto.setAliExpressDeliveryDetailList(resultDetailList);
        return dto;
    }

    @Override
    public Boolean getIsSendMq() {
        return false;
    }

    public static void main(String[] args) {
        OffsetDateTime offsetDateTime = LocalDateTime.of(2024, 5, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toOffsetDateTime();
        LocalDateTime localDateTime = offsetDateTime.atZoneSameInstant(ZoneId.of("America/Tijuana")).toLocalDateTime();
        System.out.println(localDateTime.toString());
    }
}
