package com.sdk.oms.shopify.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.enums.PlatformDictEnum;
import com.sdk.oms.shopify.api.rest.model.ShopifyLineItem;
import com.sdk.oms.shopify.api.rest.model.ShopifyOrder;
import com.sdk.oms.shopify.api.rest.model.ShopifyShippingLine;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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


    public PlatformShopifyOrderDTO(JobTaskDTO dto, ShopifyOrder shopifyOrder) {
        this.shopifyOrder = shopifyOrder;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.SHOPIFY.getCode());
        this.setUniqueId(shopifyOrder.getId());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(PlatformShopifyOrderDTO dto) {
        // 原订单信息
        ShopifyOrder sourceOrder = dto.getShopifyOrder();

        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        // 订单日期
        orderDTO.setBillDate(sourceOrder.getCreatedAt().toLocalDate());
        // 平台订单号
        orderDTO.setPlatformCode(sourceOrder.getId());
        // 销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.SHOPIFY.getCode());
        // TODO 店铺？
        orderDTO.setShopId("");
        // 作废状态（false未作废，true已作废）
        orderDTO.setInvalidStatus(false);
        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType("");
        // 作废原因
        orderDTO.setInvalidRemark("");
        // TODO 订单状态
        // SoB2cBillStatusEnum
        orderDTO.setBillStatus("");
        // 付款状态（待付款、已付款）
        // TODO
        orderDTO.setPayStatus(sourceOrder.getFinancialStatus());
        // 订单金额
        orderDTO.setAmount(sourceOrder.getTotalPrice());
        // 币别（原币）
        orderDTO.setCurrency(sourceOrder.getCurrency().getCurrencyCode());
        // TODO 汇率
        orderDTO.setExchangeRate(BigDecimal.ZERO);
        // 运费收入
        BigDecimal shippingFee = sourceOrder.getShippingLines().stream()
                .map(ShopifyShippingLine::getPrice)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        orderDTO.setShippingFee(shippingFee);
        // TODO 付款时间?
        orderDTO.setPayTime(null);
        // 付款金额
        orderDTO.setPayAmount(sourceOrder.getSubtotalPrice());
        // 付款方式
        orderDTO.setDictPayMethod("?");
        // 买家备注
        orderDTO.setBuyerRemark("");
        // 订单备注
        orderDTO.setRemark(sourceOrder.getNote());
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
        // TODO 来源id ?
        orderDTO.setSourceId(sourceOrder.getId());
        // 来源编码
        orderDTO.setSourceCode("");
        // 标签json
        orderDTO.setLabelJson("{}");
        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(sourceOrder);
        orderDTO.setDetails(details);
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
        // TODO 卖家sku编号?
        detailDTO.setSellerSkuNo("");
        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getVariantId());
        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        // TODO 库存是否扣除？
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
        // TODO 汇率
        detailDTO.setExchangeRate(BigDecimal.ZERO);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(item.getId());
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
