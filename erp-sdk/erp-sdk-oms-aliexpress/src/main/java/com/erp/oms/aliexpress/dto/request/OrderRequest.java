package com.erp.oms.aliexpress.dto.request;

import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Lambda
 * @Classname OrderRequest
 * @Description
 * @Date 2023-11-29 14:32
 * @Created by yl
 */
@Data
@Builder
public class OrderRequest implements Serializable {


    private String clientId;

    private String clientSecret;

    private String baseUrl;

    private String apiName;

    /**
     * 更新开始时间
     */
    private String startTime;

    /**
     * 更新结束时间
     */
    private String endTime;

    private String token;

    /**
     * 订单状态： PLACE_ORDER_SUCCESS:等待买家付款; IN_CANCEL:买家申请取消; WAIT_SELLER_SEND_GOODS:等待您发货; SELLER_PART_SEND_GOODS:部分发货; WAIT_BUYER_ACCEPT_GOODS:等待买家收货; FUND_PROCESSING:买卖家达成一致，资金处理中； IN_ISSUE:含纠纷中的订单; IN_FROZEN:冻结中的订单; WAIT_SELLER_EXAMINE_MONEY:等待您确认金额; RISK_CONTROL:订单处于风控24小时中，从买家在线支付完成后开始，持续24小时。 以上状态查询可分别做单独查询，不传订单状态查询订单信息不包含（FINISH，已结束订单状态） FINISH:已结束的订单，需单独查询。
     */
    private String orderStatus;

    /**
     * 订单状态： PLACE_ORDER_SUCCESS:等待买家付款; IN_CANCEL:买家申请取消; WAIT_SELLER_SEND_GOODS:等待您发货; SELLER_PART_SEND_GOODS:部分发货; WAIT_BUYER_ACCEPT_GOODS:等待买家收货; FUND_PROCESSING:买卖家达成一致，资金处理中； IN_ISSUE:含纠纷中的订单; IN_FROZEN:冻结中的订单; WAIT_SELLER_EXAMINE_MONEY:等待您确认金额; RISK_CONTROL:订单处于风控24小时中，从买家在线支付完成后开始，持续24小时。 以上状态查询可分别做单独查询，不传订单状态查询订单信息不包含（FINISH，已结束订单状态） FINISH:已结束的订单，需单独查询。
     */
    private List<String> orderStatusList;

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 订单创建开始时间
     */
    private String createDateStart;



    public static OrderRequest builderByShopInfo(String apiName, AliExpressShopInfoDTO shopInfoDTO) {
        return OrderRequest.builder().
                clientId(shopInfoDTO.getClientId()).
                clientSecret(shopInfoDTO.getClientSecret()).
                baseUrl(shopInfoDTO.getBaseUrl()).
                apiName(apiName).
                currentPage(1).
                token(shopInfoDTO.getToken()).build();
    }

    /**
     * 更新开始时间3个月前日期
     */
    public String convertStartTime3MonthAgo() {
        LocalDateTime startLocalDateTime = LocalDateTime.parse(this.startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return LocalDateUtil.formatTime(startLocalDateTime.minusMonths(3), DateUtil.fmt);
    }
}
