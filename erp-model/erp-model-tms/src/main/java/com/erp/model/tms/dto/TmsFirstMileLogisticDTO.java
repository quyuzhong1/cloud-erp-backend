package com.erp.model.tms.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.*;
import net.sf.cglib.core.Local;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 头程物流单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
*/
@Data
@NoArgsConstructor
public class TmsFirstMileLogisticDTO implements Serializable {


    /**
     * 更新物流状态
     */
    @Data
    @NoArgsConstructor
    public static class UpdateRemarkDTO {

        /**
         * id
         */
        @NotNull(message = "id不能为空")
        private String id;

        /**
         * 备注
         */
        private String remark;

    }
    /**
     * 渠道信息
     */
    @Data
    @NoArgsConstructor
    public static class LogisticsDTO {

        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 渠道名称
         */
        private String logisticsChannelName;

        /**
         * 计费方式
         */
        private String billingMethod;

        /**
         * 计费方式名称
         */
        private String billingMethodName;

        /**
         * 预计时效(天)
         */
        private Integer estimatedDay;

        /**
         * 预计时效描述
         */
        private String estimatedTimeDesc;

        /**
         * 材积设置
         */
        private Integer volumeSetting;

        /**
         * 预计时效单位
         */
        private String estimatedTimeUnit;

        /**
         * 装箱信息
         */
        private List<PackingDTO> packingDTOList;

    }
    /**
     * 发货单信息
     */
    @Data
    @NoArgsConstructor
    public static class DeliveryDTO {

        /**
         * 发货单id
         */
        private String outstockId;

        /**
         * 发货单单号
         */
        private String outstockCode;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 店铺负责人
         */
        private String chargeId;

        /**
         * 物流状态名称
         */
        private String logisticsStatusName;

        /**
         * 轨迹
         */
        private String logisticsTrack;

        /**
         * 实际时效
         */
        private String actualTime;

        /**
         * 发货仓库名称
         */
        private String fromWarehouseName;

        /**
         * 发货国家
         */
        private String fromCountryName;

        /**
         * 发货详细地址
         */
        private String fromAddress;

        /**
         * 目的仓库名称
         */
        private String toWarehouseName;

        /**
         * 目的国家
         */
        private String toCountryName;

        /**
         * 目的详细地址
         */
        private String toAddress;

        /**
         * 装箱信息
         */
        private List<PackingDTO> packingDTOList;
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

    }

    /**
     * 获取可以生成的发货单
     */
    @Data
    @NoArgsConstructor
    public static class CanGenerateDeliveryDTO {

        /**
         * 发货单id
         */
        private String outstockId;

        /**
         * 渠道id
         */
        private String logisticsChannelId;
    }
    /**
     * 历史轨迹
     */
    @Data
    @NoArgsConstructor
    public static class HistoryTrackDTO {
        /**
         * 物流号
         */
        private String code;

        /**
         * 轨迹信息
         */
        private List<TrackDTO> trackList;
    }
    /**
     * 轨迹信息
     */
    @Data
    @NoArgsConstructor
    public static class TrackDTO {
        /**
         * 轨迹时间
         */
        private LocalDateTime trackTime;
        /**
         * 轨迹描述
         */
        private String track;
    }
    /**
     * 下推对账单
     */
    @Data
    @NoArgsConstructor
    public static class GenerateReconciliationDTO {

        /**
         * id集合
         */
        @NotNull(message = "id集合不能为空")
        private List<String> ids;

        /**
         * 对账单id（为空说明是新生成）
         */
        private String reconciliationId;

        /**
         * 周期
         */
        @NotNull(message = "周期不能为空")
        private List<LocalDate> dateList;
    }

    /**
     * 更新渠道
     */
    @Data
    @NoArgsConstructor
    public static class UpdateChannelDTO {

        /**
         * id集合
         */
        @NotNull(message = "id集合不能为空")
        private List<String> ids;

        /**
         * 物流渠道id
         */
        @NotNull(message = "物流渠道id不能为空")
        private String logisticsChannelId;

        /**
         * 物流商id
         */
        @NotNull(message = "物流商id不能为空")
        private String logisticsSupplierId;

        /**
         * 运输方式
         */
        @NotNull(message = "运输方式不能为空")
        private String shippingMethod;
    }

    /**
     * 更新发票状态
     */
    @Data
    @NoArgsConstructor
    public static class UpdateInvoicesStatusDTO {

        /**
         * id集合
         */
        @NotNull(message = "id集合不能为空")
        private List<String> ids;

        /**
         * 发票状态
         */
        @NotNull(message = "发票状态不能为空")
        private String invoicesStatus;
    }

    /**
     * 更新物流状态
     */
    @Data
    @NoArgsConstructor
    public static class UpdateLogisticsStatusDTO {

        /**
         * id集合
         */
        @NotNull(message = "id集合不能为空")
        private List<String> ids;

        /**
         * 物流状态
         */
        @NotNull(message = "物流状态不能为空")
        private String logisticsStatus;

        /**
         * 状态时间
         */
        private LocalDateTime time;

        /**
         * 轨迹
         */
        private String logisticsTrack;
    }

    /**
     * 统计入参
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LogisticStatisticsReq {

        /**
         * 订单类型
         */
        private String orderType;

        /**
         * 物流状态
         */
        private List<String> logisticStatusList;

        /**
         * 起始下单时间
         */
        private LocalDateTime beginOrderTime;

        /**
         * 结束下单时间
         */
        private LocalDateTime endOrderTime;
    }

    /**
     * 统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogisticStatisticsDTO {

        /**
         * 年份
         */
        private Integer year;

        /**
         * 月份
         */
        private Integer month;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 列表统计返回结果
     */
    @Data
    @NoArgsConstructor
    public static class StatisticsVO {

        /**
         * 发货统计
         */
        private DeliveryStatistics deliveryStatistics;

        /**
         * 对账统计
         */
        private ReconciliationStatistics reconciliationStatistics;

        /**
         * 超期统计
         */
        private OverdueStatistics overdueStatistics;

        /**
         * 发货统计
         */
        @Data
        @NoArgsConstructor
        public static class DeliveryStatistics {

            /**
             * 上月发货
             */
            private Integer lastMonthDelivery;

            /**
             * 本月发货
             */
            private Integer thisMonthDelivery;

            /**
             * 上月下单
             */
            private Integer lastMonthOrder;

            /**
             * 本月下单
             */
            private Integer thisMonthOrder;
        }

        /**
         * 发货统计
         */
        @Data
        @NoArgsConstructor
        public static class ReconciliationStatistics {

            /**
             * 待生成
             */
            private Integer toBeCreate;

            /**
             * 待确认
             */
            private Integer toBeConfirm;

            /**
             * 已确认
             */
            private Integer confirm;

            /**
             * 已对账
             */
            private Integer reconciliation;
        }

        /**
         * 超期统计
         */
        @Data
        @NoArgsConstructor
        public static class OverdueStatistics {

            /**
             * 即将超期
             */
            private Integer almostOverdue;

            /**
             * 已超期
             */
            private Integer expired;
        }
    }

    /**
     * 超期统计
     */
    @Data
    @NoArgsConstructor
    public static class OverdueDTO {

        /**
         * 实际时效（单位：小时）
         */
        private Integer actualHour;

        /**
         * 预估时效（单位：天）
         */
        private String effectiveTime;

        /**
         * 剩余时间（小时）
         */
        private Integer remainingTime;

        /**
         * 店铺id
         */
        private String shopId;
    }

    /**
     * 分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingVO {

        /**
         * id
         */
        private String id;

        /**
         * 运单号
         */
        private String transportNo;
        /**
         * 柜号
         */
        private String counterNo;

        /**
         * 业务单号
         */
        private String businessCode;

        /**
         * 来源id(发货id)
         */
        private String outstockId;

        /**
         * 来源单号(发货单号)
         */
        private String outstockCode;

        /**
         * 物流状态
         */
        private String logisticsStatus;

        /**
         * 物流状态名称
         */
        private String logisticsStatusName;
        /**
         * 物流轨迹
         */
        private String logisticsTrack;

        /**
         * 发票状态
         */
        private String invoicesStatus;

        /**
         * 发票状态名称
         */
        private String invoicesStatusName;

        /**
         * 对账状态
         */
        private String reconciliationStatus;

        /**
         * 对账状态名称
         */
        private String reconciliationStatusName;
        /**
         * 运输方式
         */
        private String shippingMethod;

        /**
         * 运输方式名称
         */
        private String shippingMethodName;

        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 渠道名称
         */
        private String logisticsChannelName;

        /**
         * 物流商id
         */
        private String logisticsSupplierId;

        /**
         * 物流商名称
         */
        private String logisticsSupplierName;

        /**
         * 发货仓库名称
         */
        private String fromWarehouseName;

        /**
         * 目的仓库名称
         */
        private String toWarehouseName;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 国家
         */
        private String country;

        /**
         * 重量
         */
        private BigDecimal weight;

        /**
         * 体积重
         */
        private BigDecimal volumeWeight;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 预估费用
         */
        private BigDecimal estimatedFee;

        /**
         * 实际费用
         */
        private BigDecimal actualFee;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 下单时间
         */
        private LocalDateTime orderTime;

        /**
         * 签收时间
         */
        private LocalDateTime signTime;

        /**
         * 预计时效(天)
         */
        private Integer estimatedDay;

        /**
         * 预计时效描述
         */
        private String estimatedTimeDesc;

        /**
         * 预计时效单位
         */
        private String estimatedTimeUnit;

        /**
         * 实际时效(描述)
         */
        private String actualDesc;

        /**
         * 实际时效（小时数）
         */
        private Integer actualHour;

        /**
         * 预警小时数
         */
        private Integer warnHour;

        /**
         * 备注
         */
        private String remark;

        /**
         * 收货地址
         */
        private String toAddress;

        /**
         * 总箱数
         */
        private Integer boxCount;
    }
    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {
        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count = 0;
    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        private String orderType;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class TimeInfoDTO {

        /**
         * 发货审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 物流下单时间
         */
        private LocalDateTime logisticOrderTime;

        /**
         * 开船时间
         */
        private LocalDateTime shipTime;

        /**
         * 运输时间
         */
        private LocalDateTime trackingTime;

        /**
         * 到达时间
         */
        private LocalDateTime arrivedTime;

        /**
         * 签收时间
         */
        private LocalDateTime signTime;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String id;
        /**
         * 明细id
         */
        private String detailId;

        /**
         * 发货单id
         */
        private String outstockId;

        /**
         * 发货单单号
         */
        private String outstockCode;

        /**
         * 店铺Id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 店铺负责人
         */
        private String chargeName;

        /**
         * 计费方式名称
         */
        private String billingMethodName;

        /**
         * 物流状态名称
         */
        private String logisticsStatus;

        /**
         * 物流状态名称
         */
        private String logisticsStatusName;

        /**
         * 轨迹
         */
        private String logisticsTrack;

        /**
         * 预计时效(天)
         */
        private Integer estimatedDay;

        /**
         * 预计时效描述
         */
        private String estimatedTimeDesc;

        /**
         * 预计时效单位
         */
        private String estimatedTimeUnit;

        /**
         * 实际时效(描述)
         */
        private String actualDesc;

        /**
         * 实际时效（小时数）
         */
        private Integer actualHour;

        /**
         * 运输方式
         */
        private String shippingMethod;

        /**
         * 运输方式名称
         */
        private String shippingMethodName;

        /**
         * 下单时间
         */
        private LocalDateTime orderTime;

        /**
         * 开船时间
         */
        private LocalDateTime shipTime;

        /**
         * 签收时间
         */
        private LocalDateTime signTime;

        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 渠道名称
         */
        private String logisticsChannelName;

        /**
         * 物流商id
         */
        private String logisticsSupplierId;

        /**
         * 物流商名称
         */
        private String logisticsSupplierName;

        /**
        * 运单号
        */
        private String transportNo;

        /**
        * 柜号
        */
        private String counterNo;

        /**
        * 备注
        */
        private String remark;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名称
         */
        private List<String> attachmentNameList;

        /**
         * 时间线集合
         */
        private List<TimeLine> timeLineList;

        /**
         * 发货仓库名称
         */
        private String fromWarehouseName;

        /**
         * 发货国家
         */
        private String fromCountryName;

        /**
         * 发货详细地址
         */
        private String fromAddress;

        /**
         * 目的仓库名称
         */
        private String toWarehouseName;

        /**
         * 目的国家
         */
        private String toCountryName;

        /**
         * 目的详细地址
         */
        private String toAddress;

        /**
         * 费用信息
         */
        private List<TmsFirstMileLogisticDTO.FeeViewDTO> feeViewList;

        /**
         * 预估合计费用
         */
        private BigDecimal estimatedFee;

        /**
        * 实际重量
        */
        private BigDecimal actualWeight;

        /**
        * 实际体积重
        */
        private BigDecimal actualVolumeWeight;

        /**
         * 装箱信息
         */
        private List<PackingDTO> packingDTOList;

        /**
        * 币种
        */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
        * 汇率
        */
        private String exchangeRate;


    }
    /**
     * 装箱信息
     */
    @Data
    @NoArgsConstructor
    public static class PackingDTO {

        private String id;
        /**
         * 箱号
         */
        private String boxNo;

        /**
         * 箱子包装尺寸
         */
        private String boxSize;
        /**
         * 箱子包装重量
         */
        private String packageWeight;
        /**
         * 装箱SKU
         * 例：（sku*qty+sku*qty+...）
         */
        private String boxDesc;

        /**
         * 长宽高相乘结果
         */
        private BigDecimal multiplySize;
        /**
         * 体积重
         */
        private BigDecimal volumeWeight;
    }

    /**
     * 时间线
     */
    @Data
    @NoArgsConstructor
    public static class TimeLine {

        /**
         * 时间线名称
         */
        private String timeName;

        /**
         * 时间
         */
        private LocalDateTime time;

    }
    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 主键id
         */
        private String detailId;
    }

    @Data
    @NoArgsConstructor
    public static class LogisticFee {

        /**
         * 系统配置id
         */
        @NotBlank(message = "系统配置id不能为空")
        private String cfgCostId;
        /**
         * 预估费用
         */
        private BigDecimal estimatedFee;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 发货单Id
         */
        @NotBlank(message = "发货单Id不能为空")
        private String outstockId;

        /**
        * 运输方式
        */
        @NotBlank(message = "运输方式不能为空")
        private String shippingMethod;

        /**
        * 物流商id
        */
        @NotBlank(message = "物流商id不能为空")
        private String logisticsSupplierId;

        /**
        * 物流渠道Id
        */
        @NotBlank(message = "物流渠道Id不能为空")
        private String logisticsChannelId;

        /**
         * 运单号
         */
        private String transportNo;

        /**
         * 柜号
         */
        private String counterNo;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
        * 备注
        */
        private String remark;

        /**
        * 物流下单时间
        */
        private LocalDateTime logisticsOrderTime;

        /**
        * 实际重量
        */
        private BigDecimal actualWeight;

        /**
        * 实际体积重
        */
        private BigDecimal actualVolumeWeight;

        /**
         * 附件地址集合
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名称集合
         */
        private List<String> attachmentNameList;

        /**
         * 费用信息
         */
        private List<LogisticFee> logisticFeeList;

        /**
         * 开船时间
         */
        private LocalDateTime shipTime;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class FeeViewDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 系统配置id
         */
        private String cfgCostId;

        /**
         * 费用名称
         */
        private String costName;

        /**
         * 预估费用
         */
        private BigDecimal estimatedCost;

        /**
         * 实际费用
         */
        private BigDecimal actualCost;

        /**
         * 差异
         */
        private BigDecimal costDiff;

    }
}