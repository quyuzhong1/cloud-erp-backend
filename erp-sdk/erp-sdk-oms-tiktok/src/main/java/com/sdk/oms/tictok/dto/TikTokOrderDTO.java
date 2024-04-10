package com.sdk.oms.tictok.dto;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.enums.MercadoOrderLogisticTypeEnum;
import com.erp.model.oms.enums.OrderLogisticTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.sdk.oms.tictok.dto.tiktok.order.view.OrderViewDTO;
import com.sdk.oms.tictok.dto.tiktok.order.view.OrdersBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TikTokOrderDTO extends CleanBaseDTO {

    private OrderViewDTO orderViewDTO;

    private String shopId;

    /**
     * 初始化
     */
    public TikTokOrderDTO(OrderViewDTO orderViewDTO, JobTaskDTO dto, String shopId) {
        this.orderViewDTO = orderViewDTO;
        this.shopId = shopId;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.MERCADOLIBRE.getCode());
        this.setUniqueId(combineUnique(orderViewDTO.getData().getOrders().get(0).getFid(), this.shopId));
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    public static String combineUnique(String orderId, String shopId){
        return StrUtil.format("{}_{}", orderId, shopId);
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(TikTokOrderDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformMercadoListingDTO 转换 DTO
     */
    private static PlatformOrderDTO initPlatformProductDTO(TikTokOrderDTO dto) {
        OrderViewDTO orderViewDTO = dto.getOrderViewDTO();
        OrdersBean ordersBean = orderViewDTO.getData().getOrders().get(0);

        //设置对应关系
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();

        //平台订单号
        orderDTO.setPlatformCode(String.valueOf(ordersBean.getFid()));

        //销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.TIK_TOK.getCode());

        // 店铺ID
        orderDTO.setShopId(dto.getShopId());

        //订单金额
        BigDecimal amount = NumberUtil.toBigDecimal(ordersBean.getPayment().getTotalAmount());
        orderDTO.setAmount(amount);
        //币别
        orderDTO.setCurrency(ordersBean.getPayment().getCurrency());

        //付款时间
        //使用 DateTimeFormatter 解析字符串日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        if (CollectionUtils.isNotEmpty(orderBean.getPayments())) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(orderBean.getPayments().get(0).getDateCreated(), formatter);
            // 转换为 LocalDateTime
            LocalDateTime payTime = offsetDateTime.toLocalDateTime();
            orderDTO.setPayTime(payTime);

            //付款方式
            orderDTO.setDictPayMethod(orderBean.getPayments().get(0).getPaymentMethodId());

        }




        //买家备注
        orderDTO.setBuyerRemark(orderBean.getFeedback().getPurchase());

        // 是否拦截
        orderDTO.setIsIntercept(false);

        // 拦截备注
        orderDTO.setInterceptRemark("");

        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());

        // 来源id
        orderDTO.setSourceId(String.valueOf(orderBean.getFid()));

        // 来源编码
        orderDTO.setSourceCode("");
        // 标签json
        Map<String, String> lableMap = new HashMap<>();

        String logisticType = "";
        ShipmentViewDTO shipmentViewDTO = new ShipmentViewDTO();
        if (ObjectUtil.isNotEmpty(orderBean.getShipmentViewDTO())) {
            shipmentViewDTO = orderBean.getShipmentViewDTO();

            if ("me2".equalsIgnoreCase(shipmentViewDTO.getLogistic().getMode()) && MercadoOrderLogisticTypeEnum.FULFILLMENT.getCode().equalsIgnoreCase(shipmentViewDTO.getLogistic().getType())) {
                //如果是平台仓，状态审核通过
                orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getCode());
                lableMap.put("logisticType", shipmentViewDTO.getLogistic().getType());
                logisticType = OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode();
            } else if ("me2".equalsIgnoreCase(shipmentViewDTO.getLogistic().getMode())
                    && (MercadoOrderLogisticTypeEnum.DROP_OFF.getCode().equals(shipmentViewDTO.getLogistic().getType()) || MercadoOrderLogisticTypeEnum.CROSS_DOCKING.getCode().equalsIgnoreCase(shipmentViewDTO.getLogistic().getType()))
            ){
                //中转发货
                lableMap.put("logisticType", shipmentViewDTO.getLogistic().getType());
                logisticType = OrderLogisticTypeEnum.TRANSIT_WAREHOUSE.getCode();
            } else if ("me1".equalsIgnoreCase(shipmentViewDTO.getLogistic().getMode())) {
                //自发货
                lableMap.put("logisticType", MercadoOrderLogisticTypeEnum.DEFAULT.getCode());
                logisticType = OrderLogisticTypeEnum.SELF_SHIPMENT.getCode();
            }

            orderDTO.setLabelJson(JSONUtil.toJsonStr(lableMap));
            //扩展字段
            JSONObject extendDataJson = new JSONObject();
            extendDataJson.put("mode",shipmentViewDTO.getLogistic().getMode());
            extendDataJson.put("logisticType", shipmentViewDTO.getLogistic().getType());
            extendDataJson.put("shipmentId", orderBean.getShipping().getFid());
            orderDTO.setExtendData(extendDataJson.toString());
        }

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");

        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        orderDTO.setInvalidStatus(Boolean.FALSE);
        // 订单状态，详情金额汇总
        if (ObjectUtil.isNotEmpty(orderBean.getShipmentViewDTO())) {
            shipmentViewDTO = orderBean.getShipmentViewDTO();

            if ("handling".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE_ING.getStatus());
                orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            } else if ("ready_to_ship".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE_ING.getStatus());
                orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            } else if ("shipped".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
                orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            } else if ("cancelled".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
                orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                orderDTO.setInvalidStatus(Boolean.TRUE);
                orderDTO.setRemark("平台取消");
            } else if ("delivered".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
                orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            } else if ("not_delivered".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
                orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            }

        }

        if ("invalid".equals(orderBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            orderDTO.setInvalidStatus(Boolean.TRUE);
            orderDTO.setRemark("平台无效订单");
        }

        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(orderBean);
        orderDTO.setDetails(details);

        //B2C销售订单买家信息表
        orderDTO.setReceiver(parseReceiver(orderBean));
        //B2C销售订单物流信息表
        orderDTO.setLogisticsList(parseLogistics(orderBean, logisticType));
        //B2C销售订单财务信息表
        orderDTO.setFinances(parseFinances(orderBean));
        // 平台类型
        orderDTO.setPlatform(dto.getPlatform());
        // 唯一ID
        orderDTO.setUniqueId(dto.getUniqueId());
        // 同步任务ID
        orderDTO.setDmpSyncTaskId(dto.getDmpSyncTaskId());
        return orderDTO;
    }
}
