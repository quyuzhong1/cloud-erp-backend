package com.erp.oms.aliexpress.handler;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
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
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
     * 下载数据
     *
     * @param data
     * @return
     */
    @Override
    public List<PlatformAliExpressOrderDTO> download(JobTaskDTO data) {
        String apiName = AliexpressConstants.LIST_ORDER;
        AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(data.getShopId());
        if (null == shopInfoDTO) {
            log.error("[速卖通订单下载]  获取 token 失败: shopId={}", data.getShopId());
            return Collections.emptyList();
        }
        // 上次执行时间
        LocalDateTime lastTime = data.getLastTime();
        // 下次执行时间
        LocalDateTime nextTime = data.getNextTime();
        String formatStr = DateUtil.fmt;
        OrderRequest orderRequest = OrderRequest.builder().
                clientId(shopInfoDTO.getClientId()).
                clientSecret(shopInfoDTO.getClientSecret()).
                startTime(LocalDateUtil.formatTime(lastTime, formatStr)).
                endTime(LocalDateUtil.formatTime(nextTime, formatStr)).
                baseUrl(shopInfoDTO.getBaseUrl()).
                apiName(apiName).
                currentPage(1).
                token(shopInfoDTO.getToken()).build();
        List<AliExpressOrder> orderList = new ArrayList<>(20);
        try {
            aliExpressOrderService.listOrder(orderRequest,orderList);
        } catch (Exception e) {
            log.error("获取速卖通订单数据异常:{}", e.getMessage());
        }
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }

        //查询发货单，获取发货仓库
        getDeliveryList(shopInfoDTO, orderList);


        return orderList.stream()
                .map(e -> new PlatformAliExpressOrderDTO(data, e, shopInfoDTO))
                .collect(Collectors.toList());

    }

    /**
     * 查询平台发货单
     * @param shopInfoDTO
     * @param orderList
     */
    private void getDeliveryList(AliExpressShopInfoDTO shopInfoDTO, List<AliExpressOrder> orderList) {
        String deliveryQueryAPiName = AliexpressConstants.ALIEXPRESS_ASCP_FFO_QUERY;
        OrderRequest deliveryRequest = OrderRequest.builder().
                clientId(shopInfoDTO.getClientId()).
                clientSecret(shopInfoDTO.getClientSecret()).
                baseUrl(shopInfoDTO.getBaseUrl()).
                apiName(deliveryQueryAPiName).
                token(shopInfoDTO.getToken()).build();
        List<ErpFulfillmentForwardDtoBean> deliveryList = new ArrayList<>();
        List<String> orderIdList = orderList.stream().map(req -> req.getOrderId()).distinct().collect(Collectors.toList());
        //20个分片拆分，平台只支持一次查询20个;
        List<List<String>> partition = Lists.partition(orderIdList, 20);
        for (List<String> list : partition) {
            deliveryList.addAll(aliExpressOrderService.listDeliveryQuery(deliveryRequest, list));
        }
        for (AliExpressOrder aliExpressOrder : orderList) {
            List<LogisitcsDTO> logisticInfoList = aliExpressOrder.getDetail().getLogisticInfoList();
            for (LogisitcsDTO logisitcsDTO : logisticInfoList) {
                ErpFulfillmentForwardDtoBean erpFulfillmentForwardDtoBean = deliveryList.stream().filter(req -> req.getTradeOrderNo().equals(aliExpressOrder.getOrderId()) && req.getTrackingNo().equals(logisitcsDTO.getLogisticsNo())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(erpFulfillmentForwardDtoBean)) {
                    erpFulfillmentForwardDtoBean.setWarehouseName(erpFulfillmentForwardDtoBean.getWarehouseName());
                }
            }
        }
    }

    @Override
    public List<PlatformOrderDTO> convert(List<PlatformAliExpressOrderDTO> sourceDataList) {
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
     * @param dto
     * @return
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
            log.error("[速卖通地址下载]  获取 token 失败: shopId={}", shopId);
            return null;
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
}
