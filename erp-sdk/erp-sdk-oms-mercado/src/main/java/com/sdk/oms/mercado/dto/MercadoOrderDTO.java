package com.sdk.oms.mercado.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MercadoOrderDTO extends CleanBaseDTO {

    private OrderViewDTO orderBean;

    private String shopId;

    /**
     * 初始化
     */
    public MercadoOrderDTO(OrderViewDTO orderBean, JobTaskDTO dto, String shopId) {
        this.orderBean = orderBean;
        this.shopId = shopId;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.WALMART.getCode());
        this.setUniqueId(String.valueOf(orderBean.getId()));
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(MercadoOrderDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformWalmartListingDTO 转换 DTO
     */
    private static PlatformOrderDTO initPlatformProductDTO(MercadoOrderDTO dto) {
        OrderViewDTO orderBean = dto.getOrderBean();

        //设置对应关系
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();




        //平台订单号
        orderDTO.setPlatformCode(String.valueOf(orderBean.getId()));

        //销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.MERCADO.getCode());

        // 店铺ID
        orderDTO.setShopId(dto.getShopId());
/*

        //付款时间
        Instant instant = Instant.ofEpochMilli(orderBean.getPayments().get(0).getDateLastModified());
        ZoneId zone = ZoneId.systemDefault();
        orderDTO.setPayTime(LocalDateTime.ofInstant(instant, zone));

        // 订单状态，详情金额汇总
        fieldHandler(orderBean.getOrderLines().getOrderLine(), orderDTO, orderBean.getShipNode().getType());

        // 是否拦截
        orderDTO.setIsIntercept(false);

        // 拦截备注
        orderDTO.setInterceptRemark("");

        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());

        // 来源id
        orderDTO.setSourceId(orderBean.getPurchaseOrderId());

        // 来源编码
        orderDTO.setSourceCode("");

        // 标签json
        Map<String, String> lableMap = new HashMap<>();
        lableMap.put("shipNodeType", orderBean.getShipNode().getType());
        orderDTO.setLabelJson(JSONUtil.toJsonStr(lableMap));

        //如果是平台仓，状态审核通过
        if ("WFSFulfilled".equals(orderBean.getShipNode().getType()) || "3PLFulfilled".equals(orderBean.getShipNode().getType())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getCode());
        }
*/

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");

        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");

/*        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(orderBean);
        orderDTO.setDetails(details);

        //B2C销售订单买家信息表
        orderDTO.setReceiver(parseReceiver(orderBean));
        //B2C销售订单物流信息表
        orderDTO.setLogisticsList(parseLogistics(orderBean.getOrderLines().getOrderLine()));
        //B2C销售订单财务信息表
        orderDTO.setFinances(parseFinances(orderBean));
        orderDTO.setPlatform(PlatformDictEnum.WALMART.getCode());
        orderDTO.setUniqueId(orderBean.getPurchaseOrderId());*/
        return orderDTO;
    }
/*
    *//**
     * 批量转换明细
     *//*
    public static List<PlatformOrderDetailDTO> parseDetailDto(OrderBean orderBean) {
        return orderBean.getOrderLines().getOrderLine().stream()
                .map(e -> intPlatformOrderDetailDTO(e, orderBean))
                .collect(Collectors.toList());
    }


    *//**
     * 转换明细
     *//*
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(OrderLineBean orderLineBean, OrderBean orderBean) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        ItemBean item = orderLineBean.getItem();
        // 图片URL
        detailDTO.setImageUrl(item.getImageUrl());
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");

        //平台明细行号
        detailDTO.setPlatformLineNumber(orderLineBean.getLineNumber());

        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getSku());

        //平台产品id
        detailDTO.setPlatformSpuNo(item.getWpid());

        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(orderLineBean.getOrderLineQuantity().getAmount());

        // 金额
        BigDecimal amount = orderLineBean.getCharges().getCharge().stream().filter(req -> "ItemPrice".equals(req.getChargeName()))
                .map(req -> req.getChargeAmount().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        detailDTO.setAmount(amount);
        // 单价
        detailDTO.setPrice(amount);
        // 币别（原币）
        String currency = orderLineBean.getCharges().getCharge().stream().map(req -> req.getChargeAmount().getCurrency()).findFirst().orElse("");
        detailDTO.setCurrency(currency);
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ONE);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(orderBean.getCustomerOrderId() + "-" + orderLineBean.getLineNumber());
        // 标签json
        detailDTO.setLabelJson("");
        // 库存组织id
        detailDTO.setWarehouseOrgId("");
        // 库存组织名称
        detailDTO.setWarehouseOrgName("");
        // 库位
        detailDTO.setWarehouseLocation("");

        return detailDTO;
    }*/

}
