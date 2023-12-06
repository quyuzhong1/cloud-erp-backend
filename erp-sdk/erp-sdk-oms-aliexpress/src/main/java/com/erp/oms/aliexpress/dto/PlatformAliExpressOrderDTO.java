package com.erp.oms.aliexpress.dto;

import com.common.business.dto.*;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.oms.aliexpress.dto.response.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

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
        // 运费收入
        BigDecimal shippingFee = BigDecimal.ZERO;
        AliExpressOrderDetail detail = sourceOrder.getDetail();
        Boolean detailIsNull = Objects.nonNull(detail);
        if (detailIsNull) {
            String shippingFeeStr = detail.getLogisticsAmount().getAmount();
            shippingFee = new BigDecimal(shippingFeeStr);
        }
        orderDTO.setShippingFee(shippingFee);
        // 付款时间
        orderDTO.setPayTime(LocalDateUtil.parseStrToLocalTime(sourceOrder.getGmtCreate()));
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
        orderDTO.setLabelJson("{}");
        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailList(detailIsNull ? detail.getChildOrderList() : Collections.emptyList());
        orderDTO.setDetails(details);
        // 订单买家信息
        List<PlatformOrderReceiverDTO> receiverList = new ArrayList<>(1);
        PlatformOrderReceiverDTO receiverDTO = new PlatformOrderReceiverDTO();
        if (detailIsNull) {
            //收货信息
            ReceiptInfo receiptInfo = detail.getReceiptInfo();
            BuyerInfo buyerInfo = detail.getBuyerInfo();

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
        receiverList.add(receiverDTO);

        orderDTO.setReceiverList(receiverList);
        // 订单财务信息
        List<PlatformOrderFinanceDTO> financesList = new ArrayList<>(1);
        PlatformOrderFinanceDTO financeDTO = new PlatformOrderFinanceDTO();
        financeDTO.setShippingCost(shippingFee);
        financesList.add(financeDTO);
        orderDTO.setFinancesList(financesList);

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

        // 卖家sku编号
        detailDTO.setSellerSkuNo("");
        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getSkuCode());
        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        detailDTO.setWarehouseId("");

        // 数量
        detailDTO.setQty(item.getProductCount());
        String priceStr = item.getProductPrice().getAmount();
        // 单价
        detailDTO.setPrice(new BigDecimal(priceStr));
        // 金额
        String amountStr = item.getProductPrice().getAmount();
        // 金额
        String currency = item.getProductPrice().getCurrencyCode();
        // 金额
        BigDecimal amount = new BigDecimal(amountStr);
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
        detailDTO.setLabelJson("");
        // 库存组织id
        detailDTO.setWarehouseOrgId("");
        // 库存组织名称
        detailDTO.setWarehouseOrgName("");
        // 库位
        detailDTO.setWarehouseLocation("");
        return detailDTO;
    }
}
