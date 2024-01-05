package com.erp.oms.aliexpress.dto;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.*;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.response.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-29 10:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformAliExpressOrderDTO extends CleanBaseDTO {

    /**
     * 速卖通订单信息
     */
    private AliExpressOrder aliExpressOrder;


    private AliExpressShopInfoDTO aliExpressShopInfoDTO;


    public PlatformAliExpressOrderDTO(JobTaskDTO dto, AliExpressOrder aliExpressOrder, AliExpressShopInfoDTO aliExpressShopInfoDTO) {
        this.aliExpressOrder = aliExpressOrder;
        this.aliExpressShopInfoDTO = aliExpressShopInfoDTO;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        this.setUniqueId(aliExpressOrder.getOrderId());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 将订单的数据转化成想要的数据
     *
     * @param dto 原始订单数据
     * @return
     * @author yl
     * @date 2023-11-29 15:47
     */
    public static PlatformOrderDTO convertDTO(PlatformAliExpressOrderDTO dto) {
        // 原订单信息
        AliExpressOrder sourceOrder = dto.getAliExpressOrder();
        // 本ERP店铺信息
        AliExpressShopInfoDTO shopInfoDTO = dto.getAliExpressShopInfoDTO();


        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        // 平台类型
        orderDTO.setPlatform(dto.getPlatform());
        // 唯一ID
        orderDTO.setUniqueId(dto.getUniqueId());
        // 同步任务ID
        orderDTO.setDmpSyncTaskId(dto.getDmpSyncTaskId());
        String fl = "";
        // 订单日期
        orderDTO.setBillDate(LocalDateUtil.parseStrToLocalDate(sourceOrder.getGmtCreate()));
        // 平台订单号
        orderDTO.setPlatformCode(sourceOrder.getOrderId());
        // 销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        // 店铺ID
        orderDTO.setShopId(shopInfoDTO.getId());
        // 作废状态（false未作废，true已作废）
        orderDTO.setInvalidStatus(false);
        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType("");
        // 作废原因
        orderDTO.setInvalidRemark("");
        // 订单状态
        // （soB2cBillStatus字典类型）
        orderDTO.setBillStatus(sourceOrder.convertBillStatus());

        // 付款状态（待付款、已付款）
        // （soB2cPayStatus字典类型）
        orderDTO.setPayStatus(sourceOrder.convertPayStatus());
        String amountStr = sourceOrder.getPayAmount().getAmount();
        String amountCurrency = sourceOrder.getPayAmount().getCurrencyCode();
        // 订单金额
        orderDTO.setAmount(new BigDecimal(amountStr));
        // 币别（原币）
        orderDTO.setCurrency(amountCurrency);
        // 汇率
        orderDTO.setExchangeRate(BigDecimal.ONE);


        AliExpressOrderDetail detail = sourceOrder.getDetail();

        AmountInfo logisticsAmount =detail.getLogisticsAmount();

        //物流成本
        BigDecimal logisticsCost = BigDecimal.ZERO;
        if(Objects.nonNull(logisticsAmount)){
            //物流成本
            String logisticsCostStr = logisticsAmount.getAmount();
            if(StringUtils.isNotBlank(logisticsCostStr)){
                logisticsCost=new BigDecimal(logisticsCostStr);
            }
        }



        BigDecimal shippingFee=BigDecimal.ZERO;
        Boolean detailIsNull = Objects.nonNull(detail);
        if (detailIsNull) {
            AmountInfo shippingAmount=detail.getLogisticsAmount();
            if(Objects.nonNull(shippingAmount)){
                String shippingFeeStr = shippingAmount.getAmount();
                if(StringUtils.isNotBlank(shippingFeeStr)){
                    shippingFee=new BigDecimal(shippingFeeStr);
                }
            }


        }
        orderDTO.setShippingFee(shippingFee);
        // 付款时间
        orderDTO.setPayTime(LocalDateUtil.parseStrToLocalTime(sourceOrder.getGmtPayTime()));
        // 付款金额
        orderDTO.setPayAmount(new BigDecimal(amountStr));
        // 付款方式
        orderDTO.setDictPayMethod(sourceOrder.getPaymentType());
        // 买家备注
        orderDTO.setBuyerRemark(Objects.nonNull(detail) ? detail.getMemo() : "");
        // 订单备注
        orderDTO.setRemark("");
        // 销售组织id
        orderDTO.setOrgId("");
        // 销售组织名称
        orderDTO.setOrgName("");
        // 是否拦截
        orderDTO.setIsIntercept(false);
        // 拦截备注
        orderDTO.setInterceptRemark("");
        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
        // 来源id
        orderDTO.setSourceId(sourceOrder.getOrderId());
        // 来源编码
        orderDTO.setSourceCode(sourceOrder.getOrderId());

        // 标签json
        Map<String, Object> lableMap = new HashMap<>();
        lableMap.put("aliexpressStatus", sourceOrder.getOrderStatus());

        //订单明细
        List<OrderItemDetail> orderItemDetailList = sourceOrder.getDetail().getChildOrderList();
        Boolean isAliexpressPlatformWarehouseOrder = Boolean.FALSE;
        if (CollectionUtils.isNotEmpty(orderItemDetailList)) {
            long count = orderItemDetailList.stream().
                    filter(o -> AliexpressConstants.CAINIAO_INTERNATIONAL_WAREHOUSE.equals(o.getLogisticsWarehouseType())).count();
            isAliexpressPlatformWarehouseOrder = count > 0;
        }
        lableMap.put("logisticsWarehouseType", orderItemDetailList.stream().map(OrderItemDetail::getLogisticsWarehouseType).collect(Collectors.joining(",")));
        lableMap.put("isAliexpressPlatformWarehouseOrder", isAliexpressPlatformWarehouseOrder);
        // 标签json
        orderDTO.setLabelJson(JSONUtil.toJsonStr(lableMap));
        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailList(detailIsNull ? detail.getChildOrderList() : Collections.emptyList());
        orderDTO.setDetails(details);

        PlatformOrderReceiverDTO receiverDTO = new PlatformOrderReceiverDTO();
        if (detailIsNull) {
            //收货信息
            ReceiptInfo receiptInfo = detail.getReceiptInfo();
            BuyerInfo buyerInfo = detail.getBuyerInfo();
            receiverDTO.setLoginId(buyerInfo.getLoginId());
            receiverDTO.setCountry(receiptInfo.getCountry());
            receiverDTO.setName(sourceOrder.getBuyerSignerFullname());
            receiverDTO.setEmail("");
            receiverDTO.setFirstAddress(receiptInfo.getAddress());
            receiverDTO.setSecondAddress(receiptInfo.getAddress2());
            receiverDTO.setFullAddress(receiptInfo.getDetailAddress());
            receiverDTO.setCityName(receiptInfo.getCity());
            receiverDTO.setCountryName("");
            receiverDTO.setDistrictName("");
            receiverDTO.setProvinceName(receiptInfo.getProvince());
            receiverDTO.setReceiverName(receiptInfo.getContactPerson());
            receiverDTO.setReceiverTelNumber(receiptInfo.getMobileNo());
            receiverDTO.setTelNumber("");
            receiverDTO.setPostCode(receiptInfo.getZip());

        } else {
            receiverDTO.setCountry("");
            receiverDTO.setName(sourceOrder.getBuyerSignerFullname());
            receiverDTO.setEmail("");
            receiverDTO.setFirstAddress("");
            receiverDTO.setSecondAddress("");
            receiverDTO.setFullAddress("");
            receiverDTO.setCityName("");
            receiverDTO.setCountryName("");
            receiverDTO.setDistrictName("");
            receiverDTO.setPostCode("");
        }
        receiverDTO.setCustomerId(sourceOrder.getBuyerLoginId());
        receiverDTO.setLoginId(sourceOrder.getBuyerLoginId());


        orderDTO.setReceiver(receiverDTO);
        // 订单财务信息
        PlatformOrderFinanceDTO financeDTO = new PlatformOrderFinanceDTO();
        financeDTO.setShippingCost(shippingFee);
        financeDTO.setLogisticsCost(logisticsCost);
        orderDTO.setFinances(financeDTO);
        return orderDTO;
    }

    /**
     * 批量转明细
     *
     * @param detailList
     * @return
     */
    private static List<PlatformOrderDetailDTO> parseDetailList(List<OrderItemDetail> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyList();
        }
        return detailList.stream()
                .map(e -> intPlatformOrderDetailDTO(e))
                .collect(Collectors.toList());
    }

    /**
     * 转换明细
     */
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(OrderItemDetail item) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        // 图片URL
        detailDTO.setImageUrl(item.getProductImgUrl());
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");


        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getSkuCode());

        // 平台sku编号
        detailDTO.setPlatformSpuNo(item.getProductId().toString());
        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        detailDTO.setWarehouseId("");
        Integer qty=item.getProductCount();
        // 数量
        detailDTO.setQty(qty);

        String priceStr = item.getProductPrice().getAmount();

        BigDecimal price=new BigDecimal(priceStr);

        // 单价
        detailDTO.setPrice(price);
        // 金额
        BigDecimal amount = MathUtil.multiply(price,qty);
        // 金额
        String currency = item.getProductPrice().getCurrencyCode();

        detailDTO.setAmount(amount);
        detailDTO.setCurrency(currency);
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ONE);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(item.getChildOrderId());
        // 标签json
        Map<String, Object> lableMap = new HashMap<>();
        lableMap.put("alreadyTaxed", item.getAlreadyTaxed());
        lableMap.put("logisticsWarehouseType", item.getLogisticsWarehouseType());
        lableMap.put("tagList", item.getTags());

        // 标签json
        detailDTO.setLabelJson(JSONUtil.toJsonStr(lableMap));
        // 库存组织id
        detailDTO.setWarehouseOrgId("");
        // 库存组织名称
        detailDTO.setWarehouseOrgName("");
        // 库位
        detailDTO.setWarehouseLocation("");
        return detailDTO;
    }
}
