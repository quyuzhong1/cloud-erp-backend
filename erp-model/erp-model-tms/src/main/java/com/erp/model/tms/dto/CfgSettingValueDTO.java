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
     * 生成设置
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
        /**
         * 头程分摊类型，/wms/dict/drop/down?type=reconciliationType
         */
        @NotBlank(message = "头程分摊类型不能为空")
        private String firstMileAllocationType;

        /**
         * 头程分摊日期
         */
        private Integer firstMileAllocationDate;

//        /**
//         * 小包分摊类型，/wms/dict/drop/down?type=reconciliationType
//         */
//        @NotBlank(message = "小包报关对账类型不能为空")
//        private String packageAllocationType;
//
//        /**
//         * 小包分摊日期
//         */
//        private Integer packageAllocationDate;
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
         * 物流单生成时机，/tms/drop/down/dict/list?key=billGenerateTiming
         */
        private String logisticsGenerateTiming;

        /**
         * 头程报关自动生成
         */
        private Boolean isAutoFirstMileDeclare;

        /**
         * 头程报关生成时机，/tms/drop/down/dict/list?key=billGenerateTiming
         */
        private String firstMileDeclareGenerateTiming;

        /**
         * B2B报关自动生成
         */
        private Boolean isAutoB2BDeclare;

        /**
         * B2B报关生成时机，/tms/drop/down/dict/list?key=billGenerateTiming
         */
        private String b2BDeclareGenerateTiming;
    }

    /**
     * 分摊设置
     */
    @Data
    @NoArgsConstructor
    public static class AllocationSettingDTO {


        /**
         * 重量分摊-头程费用配置
         * http://172.16.100.11:3002/project/128/interface/api/25522   key=weightAllocation
         *
         */

        private String weightFirstAllocation;
        /**
         * 重量分摊-小包费用配置
         */
        private String weightPackageAllocation;

        /**
         * 费用分摊-头程-运费
         * http://172.16.100.11:3002/project/128/interface/api/25522   key= firstMileCostAllocation
         *
         */
        private String firstShippingCost;
        //费用分摊-头程-关税费用
        private String firstTariffFee;
        //费用分摊-头程-其他税费
        private String firstOtherTaxFee;
        //费用分摊-头程-其他费用
        private String firstOtherFee;

        /**
         * 费用分摊-小包-运费
         * http://172.16.100.11:3002/project/128/interface/api/25522   key=packageCostAllocation
         */
        private String packageShippingCost;
        //费用分摊-小包-关税费用
        private String packageTariffFee;
        //费用分摊-小包-其他费用
        private String packageOtherFee;
    }
}