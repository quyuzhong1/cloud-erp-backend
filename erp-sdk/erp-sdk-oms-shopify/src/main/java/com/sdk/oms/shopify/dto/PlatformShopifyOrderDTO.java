package com.sdk.oms.shopify.dto;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.*;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.sdk.oms.shopify.api.rest.model.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 平台Shopify订单DTO
 *
 * @Author Jim
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformShopifyOrderDTO extends CleanBaseDTO {
    /**
     * Shopify SDK 订单信息
     */
    private ShopifyOrder shopifyOrder;

    /**
     * Shopify SDK 订单信息
     */
    @Panno(findType = PannoEnum.EQ,field = "shopId")
    private String shopId;

    /**
     * 详情或其他数据下载状态
     * 0 详情数据需要更新
     * 1 详情数据已更新
     */
    @Panno(findType = PannoEnum.EQ,field = "downloadStatus")
    private Integer downloadStatus;

    /**
     * 付款时间
     */
    private LocalDateTime payTime;

    /**
     * 付款方式
     */
    private String dictPayMethod;


    public static PlatformShopifyOrderDTO getByDownloadStatus(Integer status) {
        PlatformShopifyOrderDTO orderMongoDTO = new PlatformShopifyOrderDTO();
        orderMongoDTO.setDownloadStatus(status);
        return orderMongoDTO;
    }

    public PlatformShopifyOrderDTO(JobTaskDTO dto, ShopifyOrder shopifyOrder, ShopifyShopInfoDTO shopInfoDTO) {
        this.shopifyOrder = shopifyOrder;
        this.setIsClean(CleanStatusEnum.NONE.getCode());
        this.setPlatform(PlatformDictEnum.SHOPIFY.getCode());
//        this.setLastPushTime(dto.getNextTime());
        this.shopId = dto.getShopId();
        this.setUniqueId(combineUnique(shopifyOrder.getOrderId(), this.shopId));
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
        this.downloadStatus = 0;
    }

    public static String combineUnique(String orderId, String shopId){
        return StrUtil.format("{}_{}", orderId, shopId);
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(PlatformShopifyOrderDTO dto) {
        // 原订单信息
        ShopifyOrder sourceOrder = dto.getShopifyOrder();

        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        // 平台类型
        orderDTO.setPlatform(dto.getPlatform());
        // 唯一ID
        orderDTO.setUniqueId(dto.getUniqueId());
        // 同步任务ID
        orderDTO.setDmpSyncTaskId(dto.getDmpSyncTaskId());

        // 订单日期
        orderDTO.setBillDate(sourceOrder.getCreatedAt().toLocalDate());
        // 平台订单创建时间
        orderDTO.setPlatformOrderCreateTime(sourceOrder.getCreatedAt());

        // 平台订单号
        orderDTO.setPlatformCode(sourceOrder.getOrderId());
        // 销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.SHOPIFY.getCode());
        // 店铺ID
        orderDTO.setShopId(dto.getShopId());
        // 平台订单原始状态
        orderDTO.setPlatformOrderStatus(sourceOrder.getFulfillmentStatus());
        // 平台订单原始取消状态
        orderDTO.setIsCancel(sourceOrder.convertInvalidStatus());

        // 作废状态（false未作废，true已作废）
        orderDTO.setInvalidStatus(sourceOrder.convertInvalidStatus());
        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType(orderDTO.getInvalidStatus() ? "automatic" : "");
        // 作废原因
        orderDTO.setInvalidRemark("");
        // 订单状态
        // （soB2cBillStatus字典类型）
        orderDTO.setBillStatus(sourceOrder.convertBillStatus());
        // 付款状态（待付款、已付款）
        // （soB2cPayStatus字典类型）
        orderDTO.setPayStatus(sourceOrder.convertPayStatus());
        // 订单金额
        orderDTO.setAmount(sourceOrder.getTotalPrice());
        // 币别（原币）
        orderDTO.setCurrency(sourceOrder.getCurrency().getCurrencyCode());
        // 汇率
        orderDTO.setExchangeRate(BigDecimal.ONE);
        // 运费收入
        BigDecimal shippingFee = sourceOrder.getShippingLines().stream()
                .map(ShopifyShippingLine::getPrice)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        orderDTO.setShippingFee(shippingFee);
        // 审核状态
        orderDTO.setApproveStatusStr(sourceOrder.convertApproveStatusStr());

        // 付款时间
        orderDTO.setPayTime("paid".equalsIgnoreCase(orderDTO.getPayStatus())? dto.getPayTime() : null);
        // 付款金额
        orderDTO.setPayAmount(sourceOrder.getSubtotalPrice());
        // 付款方式
        orderDTO.setDictPayMethod(dto.getDictPayMethod());
        // 买家备注
        String buyerRemark  = "";
        if (StringUtils.isBlank(sourceOrder.getNote())){
            if (null != sourceOrder.getCustomer()){
                String note = sourceOrder.getCustomer().getNote();
                if (StringUtils.isNotBlank(note)){
                    buyerRemark = note;
                }
            }
        } else {
            buyerRemark = sourceOrder.getNote();
        }

        orderDTO.setBuyerRemark(buyerRemark);
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
        orderDTO.setSourceType("soB2c");
        // 来源id
        orderDTO.setSourceId(sourceOrder.getOrderId());
        // 来源编码
        orderDTO.setSourceCode("");
        // 标签json
        orderDTO.setLabelJson("{}");
        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        // 来源状态
        orderDTO.setPlatformOrderStatus(sourceOrder.getFinancialStatus());
        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(sourceOrder);
        orderDTO.setDetails(details);

        // 订单买家信息
        ShopifyCustomer customer = dto.getShopifyOrder().getCustomer();
        ShopifyAddress shippingAddress = dto.getShopifyOrder().getShippingAddress();
        if (null != customer) {
            PlatformOrderReceiverDTO receiverDTO = new PlatformOrderReceiverDTO();
            receiverDTO.setName(
                    (StringUtils.isBlank(customer.getFirstName()) ? "" : customer.getFirstName()) +
                            (StringUtils.isBlank(customer.getFirstName()) ? "" : " " + customer.getLastname())
            );
            receiverDTO.setCustomerId(StringUtils.isBlank(customer.getId())? "" : customer.getId());
            receiverDTO.setReceiverTelNumber(StringUtils.isBlank(customer.getPhone()) ? "" :customer.getPhone());
            receiverDTO.setTelNumber(StringUtils.isBlank(customer.getPhone())? "" : customer.getPhone());
            receiverDTO.setEmail(StringUtils.isBlank(customer.getEmail()) ? "" : customer.getEmail());
            if (null != shippingAddress){
                receiverDTO.setProvinceName(StringUtils.isBlank(shippingAddress.getProvince()) ? "" : shippingAddress.getProvince());
                receiverDTO.setFirstAddress(StringUtils.isBlank(shippingAddress.getAddress1()) ? "" : shippingAddress.getAddress1());
                receiverDTO.setSecondAddress(StringUtils.isBlank(shippingAddress.getAddress2()) ? "" : shippingAddress.getAddress2());

                receiverDTO.setCityName(StringUtils.isBlank(shippingAddress.getCity()) ? "" : shippingAddress.getCity());
//                receiverDTO.setCountryName(StringUtils.isBlank(shippingAddress.getCountryCode()) ? "" : shippingAddress.getCountryCode());
                receiverDTO.setCountry(StringUtils.isBlank(shippingAddress.getCountryCode()) ? "" : shippingAddress.getCountryCode());
                receiverDTO.setReceiverName(StringUtils.isBlank(shippingAddress.getName()) ? "" : shippingAddress.getName());
                receiverDTO.setFullAddress("");
                receiverDTO.setPostCode(shippingAddress.getZip());
            }
            orderDTO.setReceiver(receiverDTO);
        }

        // 订单财务信息

        PlatformOrderFinanceDTO financeDTO = new PlatformOrderFinanceDTO();
        List<ShopifyShippingLine> shippingLines = dto.getShopifyOrder().getShippingLines();
        BigDecimal totalShippingPrice = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(shippingLines)){
            // 合计
            totalShippingPrice = dto.getShopifyOrder().getShippingLines()
                    .stream()
                    .map(ShopifyShippingLine::getPrice)
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }
        financeDTO.setShippingCost(totalShippingPrice);
        orderDTO.setFinances(financeDTO);
        // 设置下载其他详情
        orderDTO.setDownloadStatus(0);
        return orderDTO;

    }

    /**
     * 批量转换明细
     */
    public static List<PlatformOrderDetailDTO> parseDetailDto(ShopifyOrder sourceOrder) {
        return sourceOrder.getLineItems().stream()
                .map(e -> intPlatformOrderDetailDTO(e, sourceOrder))
                .collect(Collectors.toList());
    }

    /**
     * 转换明细
     */
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(ShopifyLineItem item, ShopifyOrder sourceOrder) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        // 图片URL
        detailDTO.setImageUrl("");
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");

        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getSku());
        //平台产品id
        detailDTO.setPlatformSpuNo(item.getProductId());

        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(item.getQuantity().intValue());
        // 单价
        detailDTO.setPrice(item.getPrice());
        // 金额
        BigDecimal amount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        detailDTO.setAmount(amount);
        // 币别（原币）
        detailDTO.setCurrency(sourceOrder.getCurrency().getCurrencyCode());
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ONE);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(item.getLineItemId());
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
