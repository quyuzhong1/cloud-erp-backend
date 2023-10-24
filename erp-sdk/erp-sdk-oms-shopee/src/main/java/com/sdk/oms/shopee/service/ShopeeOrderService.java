package com.sdk.oms.shopee.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.order.request.OrderRequest;
import com.sdk.oms.shopee.dto.order.response.ShopeeOrder;
import com.sdk.oms.shopee.enums.OrderStatusEnum;
import com.sdk.oms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sdk.oms.shopee.constants.ShopeeConstants.*;

/**
 * @author zdy
 * @ClassName GlobalProductServiceImpl
 * @description: TODO
 * @date 2023年10月19日
 * @version: 1.0
 */
@Component
@Slf4j
public class ShopeeOrderService {
    public static void main(String[] args) {
        ShopeeOrderService shopeeOrderService = new ShopeeOrderService();
        long timest = System.currentTimeMillis() / 1000L;
        Long time_from = timest - (3600 * 24 * 14);
        Long time_to = timest;
        //订单列表
        OrderRequest orderRequest = OrderRequest.builder()
                .offset(0)
                .timeFrom(time_from)
                .timeTo(time_to)
                .tmpPartnerKey(tmp_partner_key)
                .partnerId(partner_id)
                .token(shop_access_token)
                .shopId(shop_id)
                .host(host)
                .cursor("")
                .build();
        List<PlatformOrderDTO> platformOrderDTOS = new ArrayList<>();
        shopeeOrderService.getAllOrder(orderRequest, platformOrderDTOS);
        System.out.println(platformOrderDTOS.size());
//        shopeeOrderService.getOrderDetail(host, shop_access_token, shop_id, partner_id, tmp_partner_key,"231019B5QD22UG");
    }

    public void getAllOrder(OrderRequest orderRequest, List<PlatformOrderDTO> platformOrderDTOS) {
        ShopeeResponse shopeeResponse = this.getOrderList(orderRequest);
        JSONObject response = shopeeResponse.getResponse();
        String error = response.getString("error");
        if (StringUtils.isNotEmpty(error)) {
            return;
        }
        JSONArray jsonArray = (JSONArray) response.get("order_list");
        //目录列表
        List<ShopeeOrder> orderList = JSONObject.parseArray(jsonArray.toJSONString(), ShopeeOrder.class);
        //获取item明细
        List<String> orderSns = orderList.stream().map(ShopeeOrder::getOrderSn).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(orderSns)) {
            orderRequest.setOrderSns(StringUtils.join(orderSns, ","));
            ShopeeResponse orderDetail = this.getOrderDetail(orderRequest);
            JSONObject responseBaseInfo = orderDetail.getResponse();
            //循环填充
            JSONArray listBase = responseBaseInfo.getJSONArray("order_list");
            List<PlatformOrderDTO> list = new ArrayList<>();
            listBase.forEach(o -> {
                JSONObject jsonObject = (JSONObject) o;
//                jsonObject.getJSONObject("description_info").getJSONObject("extended_description").getJSONArray("field_list").get(0);
                Long createTime = jsonObject.getLong("create_time");
                Instant instant = Instant.ofEpochSecond(createTime);
                ZoneId zone = ZoneId.systemDefault();
                PlatformOrderDTO orderDTO = new PlatformOrderDTO();
                // 订单日期
                orderDTO.setBillDate(LocalDateTime.ofInstant(instant, zone).toLocalDate());
                // 平台订单号
                orderDTO.setPlatformCode(jsonObject.getString("order_sn"));
                // 销售平台
                orderDTO.setDictPlatform(PlatformDictEnum.SHOPEE.getCode());
                // 店铺ID
                orderDTO.setShopId(String.valueOf(orderRequest.getShopId()));
                // 作废状态（false未作废，true已作废）
                orderDTO.setInvalidStatus(false);
                // 作废类型（manual手动作废，automatic自动作废）
                orderDTO.setInvalidType("");
                // 作废原因
                orderDTO.setInvalidRemark("");
                // 订单状态
                // （soB2cBillStatus字典类型）  SoB2cBillStatusEnum
                //UNPAID/READY_TO_SHIP/PROCESSED/SHIPPED/COMPLETED/IN_CANCEL/CANCELLED/INVOICE_PENDING
                String orderStatus = jsonObject.getString("order_status");
                if (OrderStatusEnum.UNPAID.getCode().equals(orderStatus)){
//                    orderDTO.setBillStatus(SoB2cBillStatusEnum);
                }
                orderDTO.setBillStatus(jsonObject.getString("order_status"));
                // 付款状态（待付款、已付款）
                // （soB2cPayStatus字典类型）
                Long paytime = jsonObject.getLong("pay_time");
                if (Objects.isNull(paytime)) {
                    orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
                } else {
                    orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
                }
//            orderDTO.setPayStatus(sourceOrder.convertPayStatus());
                // 订单金额
                orderDTO.setAmount(jsonObject.getBigDecimal("total_amount"));
                // 币别（原币）
                orderDTO.setCurrency(jsonObject.getString("currency"));
                // 汇率
                orderDTO.setExchangeRate(BigDecimal.ONE);
                // 运费收入
                orderDTO.setShippingFee(jsonObject.getBigDecimal("actual_shipping_fee"));
                // 付款时间
                if (Objects.nonNull(paytime)) {
                    Instant instant2 = Instant.ofEpochSecond(paytime);
                    orderDTO.setPayTime(LocalDateTime.ofInstant(instant2, zone));
                }

                // 付款金额
                orderDTO.setPayAmount(jsonObject.getBigDecimal("total_amount"));
                // 付款方式
                orderDTO.setDictPayMethod(jsonObject.getString("shipping_carrier"));
                // 买家备注
                orderDTO.setBuyerRemark("");
                // 订单备注
                orderDTO.setRemark(jsonObject.getString("note"));
                // 销售组织id
                orderDTO.setOrgId(jsonObject.getString("buyer_user_id"));
                // 销售组织名称
                orderDTO.setOrgName(jsonObject.getString("buyer_username"));
                // 是否拦截
                orderDTO.setIsIntercept(false);
                // 拦截备注
                orderDTO.setInterceptRemark("");
                // 来源类型
                orderDTO.setSourceType("soB2c");
                // 来源id
                orderDTO.setSourceId(jsonObject.getString("order_sn"));
                // 来源编码
                orderDTO.setSourceCode(jsonObject.getString("order_sn"));
                // 标签json
                orderDTO.setLabelJson(jsonObject.toJSONString());
                // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
                orderDTO.setAbnormalType("");
                // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
                orderDTO.setSyncKingdeeStatus("0");

                list.add(orderDTO);
            });
            if (CollectionUtils.isNotEmpty(list)) {
                platformOrderDTOS.addAll(list);
            }
        }
        Boolean more = response.getBoolean("more");
        if (more) {
            String next_cursor = response.getString("next_cursor");
            orderRequest.setCursor(next_cursor);
            //还有订单数据
            this.getAllOrder(orderRequest, platformOrderDTOS);
        }

    }

    private HashMap<String, Object> getOrderCommonParam(OrderRequest orderRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("timestamp", orderRequest.getTimestamp());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(orderRequest.getPath(), orderRequest.getToken(), orderRequest.getPartnerId(),
                orderRequest.getTmpPartnerKey(), orderRequest.getShopId()));
        paramMap.put("shop_id", orderRequest.getShopId());
        paramMap.put("partner_id", orderRequest.getPartnerId());
        paramMap.put("access_token", orderRequest.getToken());
        return paramMap;
    }

    public ShopeeResponse getOrderList(OrderRequest orderRequest) {
        String path = "/api/v2/order/get_order_list";
        orderRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        orderRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(orderRequest);
        //create_time, update_time.
        paramMap.put("time_range_field", "create_time");
        //15天内
        if (Objects.nonNull(orderRequest.getTimeFrom())) {
            paramMap.put("time_from", orderRequest.getTimeFrom());
        }
        if (Objects.nonNull(orderRequest.getTimeTo())) {
            paramMap.put("time_to", orderRequest.getTimeTo());
        }
        paramMap.put("timestamp", timestamp);
        //1-100
        paramMap.put("page_size", pageSize);
        paramMap.put("cursor", orderRequest.getCursor());
        //UNPAID/READY_TO_SHIP/PROCESSED/SHIPPED/COMPLETED/IN_CANCEL/CANCELLED/INVOICE_PENDING
//        paramMap.put("order_status", "PROCESSED");
//        paramMap.put("response_optional_fields", "order_status");
//        paramMap.put("request_order_status_pending", true);
        return ShopeeApiUtils.sendGet(orderRequest.getHost() + path, paramMap);
        /**
         * bodyStr:{"error":"","message":"","response":{"more":false,"next_cursor":"","order_list":[{"order_sn":"231019B5QD22UG","order_status":"PROCESSED"},{"order_sn":"231018906HF759","order_status":"PROCESSED"}]},"request_id":"c9dd8acb2dac5b7cda22a565f4f4f121"}
         */
    }

    public ShopeeResponse getOrderDetail(OrderRequest orderRequest) {
        String path = "/api/v2/order/get_order_detail";
        orderRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        orderRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(orderRequest);
        //The set of order_sn. If there are multiple order_sn, you need to use English comma to connect them. limit [1,50]
        paramMap.put("order_sn_list", orderRequest.getOrderSns());
        paramMap.put("response_optional_fields", "create_time,order_sn,order_status,pay_time,total_amount,currency," +
                "actual_shipping_fee,shipping_carrier,note,buyer_user_id,buyer_username");
        return ShopeeApiUtils.sendGet(orderRequest.getHost() + path, paramMap);
    }
}
