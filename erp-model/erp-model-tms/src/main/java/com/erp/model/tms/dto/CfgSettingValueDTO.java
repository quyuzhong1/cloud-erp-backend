package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 系统配置管理请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-01-08
*/
@Data
@NoArgsConstructor
public class CfgSettingValueDTO implements Serializable {

    /**
     * 申报规则
     */
    @Data
    @NoArgsConstructor
    public static class LogisticsProductDestDeclarePrice{
        /**
         * 含税采购价（>）
         */
        @Digits(integer = 12, fraction = 4, message = "含税采购价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal startPrice;
        /**
         * 含税采购价(<=)
         */
        @Digits(integer = 12, fraction = 4, message = "含税采购价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal endPrice;
        /**
         * 比例
         */
        @Digits(integer = 12, fraction = 2, message = "比例整数位不能超过12位，小数位不能超过2位")
        private BigDecimal rate;
    }

    /**
     * 通知管理
     */
    @Data
    @NoArgsConstructor
    public static class NoticeDTO{

        /**
         * 在途异常岗位id集合
         */
        private List<String> inTransitPostIdList;

        /**
         * 在途异常抄送人id集合，/api/plm/common/findUserList
         */
        private List<String> inTransitUserIdList;

        /**
         * 渠道更换岗位id集合
         */
        private List<String> channelPostIdList;

        /**
         * 渠道更换抄送人id集合，/api/plm/common/findUserList
         */
        private List<String> channelUserIdList;

        /**
         * 在途异常是否使用店铺负责人
         */
        private Boolean isInTransitShopCharge;

        /**
         * 渠道更换是否使用店铺负责人
         */
        private Boolean isChannelShopCharge;

        /**
         * 备案通知岗位id集合
         */
        private List<String> productRegistrationPostIdList;

        /**
         * 备案通知抄送人id集合，/api/plm/common/findUserList
         */
        private List<String> productRegistrationUserIdList;
    }

    /**
     * 对账周期
     */
    @Data
    @NoArgsConstructor
    public static class ReconciliationCycleDTO{

        /**
         * 头程对账类型，/wms/dict/drop/down?type=reconciliationType
         */
        @NotBlank(message = "头程对账类型不能为空")
        private String firstMileReconciliationType;

        /**
         * 头程对账日期
         */
        private Integer firstMileReconciliationDate;

        /**
         * 报关对账类型，/wms/dict/drop/down?type=reconciliationType
         */
        @NotBlank(message = "报关对账类型不能为空")
        private String declareReconciliationType;

        /**
         * 报关对账日期
         */
        private Integer declareReconciliationDate;
    }

    /**
     * 对账周期
     */
    @Data
    @NoArgsConstructor
    public static class BillAutoAddDTO{

        /**
         * 物流单自动生成
         */
        private Boolean isAutoLogistics;

        /**
         * 头程报关自动生成
         */
        private Boolean isAutoFirstMileDeclare;

        /**
         * 头程报关自动生成
         */
        private Boolean isAutoB2BDeclare;
    }
}