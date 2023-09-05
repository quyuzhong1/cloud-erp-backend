package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Lambda
 * @Classname RefundOrderDTO
 * @Date 2023-08-25 12:27
 * @Created by yl
 */
public class RefundOrderDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 单据编号
         */
        private String code;

        /**
         * 平台集合
         */
        private List<String> dictPlatformList;

        /**
         * 店铺集合
         */
        private List<String> shopIdList;

        /**
         * 平台订单号
         */
        private String platformOrderNo;


        /**
         * 平台退款号
         */
        private String platformRefundNo;

        /**
         * 退款原因
         */
        private String reason;

        /**
         * 状态
         */
        private List<String> statusList;



        /**
         * 创建时间集合
         */
        private List<LocalDateTime> createTimeList;


        /**
         * 退款时间
         */
        private List<LocalDateTime> refundTimeList;

    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 单据单号
         */
        private String code;

        /**
         * 平台
         */
        private String dictPlatform;


        /**
         * 平台名称
         */
        private String platformName;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 平台订单号
         */
        private String platformOrderNo;

        /**
         * 平台退款单号
         */
        private String platformRefundNo;

        /**
         * 状态
         */
        private String status;

        /**
         * 状态名
         */
        private String statusName;

        /**
         * 退款金额
         */
        private BigDecimal refundAmount;

        /**
         * 退款人名币
         */
        private BigDecimal refundCnyAmount;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 退款原因
         */
        private String reason;

        /**
         * 退款时间
         */
        private Date refundTime;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间时间
         */
        private LocalDateTime createTime;


        /**
         * 修改人
         */
        private String updateUserName;

        /**
         * 创建时间时间
         */
        private LocalDateTime updateTime;


    }
}
