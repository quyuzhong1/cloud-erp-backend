package com.erp.sdk.oms.amz.spapi.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.sdk.oms.amz.spapi.model.orders.Money;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.sdk.oms.amz.spapi.model.orders.OrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台亚马逊订单DTO
 *
 * @Author Cloud
 * @Date 2023/8/31 16:01
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformAmazonOrderDTO extends CleanBaseDTO {

    private Order order;

    private String shopId;

    public PlatformAmazonOrderDTO(Order order, ShopInfoEntity shopInfoEntity) {
        this.order = order;
        this.shopId = shopInfoEntity.getId();
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(PlatformAmazonOrderDTO dto, Boolean isSendMq) {
        // 原订单信息
        Order sourceOrder = dto.getOrder();

        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        // 订单日期
        LocalDateTime purchaseLocalDateTime = sourceOrder.convertPurchaseSystemTime();
        orderDTO.setBillDate(purchaseLocalDateTime.toLocalDate());
        // 平台订单号
        orderDTO.setPlatformCode(sourceOrder.getAmazonOrderId());
        // 销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
        // 店铺ID
        orderDTO.setShopId(dto.getShopId());
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
        // 订单金额
        orderDTO.setAmount(new BigDecimal(sourceOrder.getOrderTotal().getAmount()));
        // 币别（原币）
        orderDTO.setCurrency(sourceOrder.getOrderTotal().getCurrencyCode());
        // 汇率
        orderDTO.setExchangeRate(BigDecimal.ONE);
        // TODO 运费收入
        BigDecimal shippingFee =BigDecimal.ZERO;
        orderDTO.setShippingFee(shippingFee);
        // 付款时间
        orderDTO.setPayTime(purchaseLocalDateTime);
        // 付款金额
        orderDTO.setPayAmount(new BigDecimal(sourceOrder.getOrderTotal().getAmount()));
        // 付款方式
        orderDTO.setDictPayMethod(sourceOrder.getPaymentMethod().getValue());
        // 买家备注
        orderDTO.setBuyerRemark("");
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
        orderDTO.setSourceId(sourceOrder.getAmazonOrderId());
        // 来源编码
        orderDTO.setSourceCode("");
        // 标签json
        orderDTO.setLabelJson("{}");
        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        // 数据下载状态:
        // 0=详情数据需要更新(不发送MQ)
        // 1=详情数据已更新(发送MQ)
        orderDTO.setDownloadStatus(isSendMq ? 1 : 0);
        return orderDTO;
    }

    /**
     * 转换明细
     */
    public static PlatformOrderDetailDTO intPlatformOrderDetailDTO(OrderItem item) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        // 图片URL
        detailDTO.setImageUrl("");
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");
        // 卖家sku编号
        detailDTO.setSellerSkuNo("");
        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getSellerSKU());
        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(item.getQuantityOrdered());
        // 单价
        Money itemPrice = item.getItemPrice();
        detailDTO.setPrice(new BigDecimal(itemPrice.getAmount()));
        // 金额
        BigDecimal amount = detailDTO.getPrice().multiply(BigDecimal.valueOf(item.getQuantityOrdered()));
        detailDTO.setAmount(amount);
        // 币别（原币）
        detailDTO.setCurrency(itemPrice.getCurrencyCode());
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ONE);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(item.getOrderItemId());
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
