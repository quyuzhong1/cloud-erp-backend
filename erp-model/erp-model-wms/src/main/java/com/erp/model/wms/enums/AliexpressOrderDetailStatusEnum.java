package com.erp.model.wms.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 速卖通订单明细状态
 */
@Getter
@AllArgsConstructor
public enum AliexpressOrderDetailStatusEnum implements EnumMessage {
    PLACE_ORDER_SUCCESS("PLACE_ORDER_SUCCESS","等待买家付款"),
    IN_CANCEL("IN_CANCEL","买家申请取消"),
    WAIT_SELLER_SEND_GOODS("WAIT_SELLER_SEND_GOODS","等待您发货"),
    SELLER_PART_SEND_GOODS("SELLER_PART_SEND_GOODS","部分发货"),
    WAIT_BUYER_ACCEPT_GOODS("WAIT_BUYER_ACCEPT_GOODS","等待买家收货"),
    FUND_PROCESSING("FUND_PROCESSING","买卖家达成一致，资金处理中"),
    IN_ISSUE("IN_ISSUE","含纠纷中的订单"),
    IN_FROZEN("IN_FROZEN","冻结中的订单"),
    WAIT_SELLER_EXAMINE_MONEY("WAIT_SELLER_EXAMINE_MONEY","等待您确认金额"),
    RISK_CONTROL("RISK_CONTROL","订单处于风控24小时中，从买家在线支付完成后开始，持续24小时"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private final String code;
    /**
     * 名称
     */
    private final String name;
}
