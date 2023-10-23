package com.sdk.oms.shopee.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.order.response.ShopeeOrder;
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
        //订单列表
        shopeeOrderService.getOrderList(host, shop_access_token, shop_id, partner_id, tmp_partner_key, "");
//        shopeeOrderService.getOrderDetail(host, shop_access_token, shop_id, partner_id, tmp_partner_key,"231019B5QD22UG");
    }

    public List<PlatformOrderDTO> getAllOrder(String host, String token, long shopId, long partner_id, String tmp_partner_key){
//        ShopeeProductService shopeeProductService = new ShopeeProductService();
        String cursor = "";
        ShopeeResponse shopeeResponse = this.getOrderList(host, token, shopId, partner_id, tmp_partner_key,cursor);
        JSONObject response = shopeeResponse.getResponse();
        Boolean more =response.getBoolean("more");
        if (more){
            //还有订单数据
        }
        String next_cursor =response.getString("next_cursor");
        JSONArray jsonArray = (JSONArray) response.get("order_list");
        //目录列表
        List<ShopeeOrder> orderList = JSONObject.parseArray(jsonArray.toJSONString(), ShopeeOrder.class);
        //获取item明细
        List<String> orderSns = orderList.stream().map(ShopeeOrder::getOrderSn).collect(Collectors.toList());
        ShopeeResponse orderDetail = this.getOrderDetail(host, token, shopId, partner_id, tmp_partner_key, StringUtils.join(orderSns, ","));
        JSONObject responseBaseInfo = orderDetail.getResponse();
        //循环填充
        JSONArray listBase = responseBaseInfo.getJSONArray("order_list");
        List<PlatformOrderDTO> list = new ArrayList<>();
        listBase.forEach(o->{
            JSONObject jsonObject = (JSONObject) o;
            jsonObject.getJSONObject("description_info").getJSONObject("extended_description").getJSONArray("field_list").get(0);
            Long createTime = jsonObject.getLong("create_time");
            Instant instant = Instant.ofEpochMilli(createTime);
            ZoneId zone = ZoneId.systemDefault();
            PlatformOrderDTO orderDTO = new PlatformOrderDTO();
            // 订单日期
            orderDTO.setBillDate(LocalDateTime.ofInstant(instant, zone).toLocalDate());
            // 平台订单号
            orderDTO.setPlatformCode(jsonObject.getString("order_sn"));
            // 销售平台
            orderDTO.setDictPlatform(PlatformDictEnum.SHOPEE.getCode());
            // 店铺ID
            orderDTO.setShopId(String.valueOf(shopId));
            // 作废状态（false未作废，true已作废）
            orderDTO.setInvalidStatus(false);
            // 作废类型（manual手动作废，automatic自动作废）
            orderDTO.setInvalidType("");
            // 作废原因
            orderDTO.setInvalidRemark("");
            // 订单状态
            // （soB2cBillStatus字典类型）
            orderDTO.setBillStatus(jsonObject.getString("order_status"));
            // 付款状态（待付款、已付款）
            // （soB2cPayStatus字典类型）
            Long paytime = jsonObject.getLong("pay_time");
            if (Objects.isNull(paytime)){
                orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            }else {
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
            if (Objects.nonNull(paytime)){
                Instant instant2 = Instant.ofEpochMilli(paytime);
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
        return list;
    }
    private HashMap<String, Object> getOrderCommonParam(String path, String access_token, long shopId, long partner_id, String tmp_partner_key) {
        //
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(path, access_token, partner_id, tmp_partner_key, shopId));
        paramMap.put("shop_id", shopId);
        paramMap.put("partner_id", partner_id);
        paramMap.put("access_token", access_token);
        return paramMap;
    }

    public ShopeeResponse getOrderList(String host, String access_token, long shopId, long partner_id, String tmp_partner_key, String cursor) {
        String path = "/api/v2/order/get_order_list";
        HashMap<String, Object> paramMap = getOrderCommonParam(path, access_token, shopId, partner_id, tmp_partner_key);
        //create_time, update_time.
        paramMap.put("time_range_field", "create_time");

        long timest = System.currentTimeMillis() / 1000L;
        Long time_from = timest - (3600 * 24 * 14);
        Long time_to = timest;
        //15天内
        paramMap.put("time_from", time_from);
        paramMap.put("time_to", time_to);
        paramMap.put("timestamp", timest);
        //1-100
        paramMap.put("page_size", "100");
        paramMap.put("cursor", cursor);
        //UNPAID/READY_TO_SHIP/PROCESSED/SHIPPED/COMPLETED/IN_CANCEL/CANCELLED/INVOICE_PENDING
//        paramMap.put("order_status", "PROCESSED");
//        paramMap.put("response_optional_fields", "order_status");
//        paramMap.put("request_order_status_pending", true);
        return ShopeeApiUtils.sendGet(host + path, paramMap);
        /**
         * bodyStr:{"error":"","message":"","response":{"more":false,"next_cursor":"","order_list":[{"order_sn":"231019B5QD22UG","order_status":"PROCESSED"},{"order_sn":"231018906HF759","order_status":"PROCESSED"}]},"request_id":"c9dd8acb2dac5b7cda22a565f4f4f121"}
         */
    }
    public ShopeeResponse getOrderDetail(String host, String access_token, long shopId, long partner_id, String tmp_partner_key, String orderSn){
        String path = "/api/v2/order/get_order_detail";
        HashMap<String, Object> paramMap = getOrderCommonParam(path, access_token, shopId, partner_id, tmp_partner_key);
        //The set of order_sn. If there are multiple order_sn, you need to use English comma to connect them. limit [1,50]
        paramMap.put("order_sn_list", orderSn);
        paramMap.put("response_optional_fields", "buyer_user_id,buyer_username,estimated_shipping_fee");
        return ShopeeApiUtils.sendGet(host + path, paramMap);
    }
}
