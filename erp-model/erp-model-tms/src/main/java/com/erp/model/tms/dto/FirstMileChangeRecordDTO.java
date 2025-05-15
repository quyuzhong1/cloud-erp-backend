package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 头程调整记录请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class FirstMileChangeRecordDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 调整单号
        */
        private String code;

        /**
        * 调整类型:firstMileCost=费用调整,firstMileWeight=重量调整,thirdReceive=签收调整
        */
        private String sourceType;

        /**
        * 操作类型:manual=人工调整,auto=系统计算
        */
        private String type;

        /**
        * 修改范围:current=仅修改当前值,box=修改同箱同SKU,order=修改同单同SKU
        */
        private String changeRange;

        /**
        * 核算月份id
        */
        private String reportPeriodId;

        /**
        * 核算月份
        */
        private LocalDate reportPeriod;

        /**
        * 业务单号
        */
        private String businessCode;

        /**
        * 发货单id
        */
        private String deliveryId;

        /**
        * 发货单编码
        */
        private String deliveryCode;

        /**
        * 物流运单号
        */
        private String transportNo;

        /**
        * 平台skuNo
        */
        private String platformSkuNo;

        /**
        * skuId
        */
        private String skuId;

        /**
        * skuNO
        */
        private String skuNo;

        /**
        * 调整分类:boxNo=箱号,shippingCost=运费,declareCost=关税,otherTaxFee=其他税费,otherCost=其他费用
        */
        private String category;

        /**
        * 调整字段:current_period_allocated_cost=本期分摊费用,mid_period_transit_cost=冲期初在途费用,end_period_transit_cost=期末在途费用,end_period_estimated_cost=期末暂估费用,charged_weight=出库重量
        */
        private String categoryField;

        /**
        * 调整前数值
        */
        private String oldValue;

        /**
        * 调整后数值
        */
        private String newValue;

        /**
        * 是否最新记录
        */
        private Boolean isLatest;


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

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 调整类型:firstMileCost=费用调整,firstMileWeight=重量调整,thirdReceive=签收调整
        */
        @NotBlank(message = "调整类型:firstMileCost=费用调整,firstMileWeight=重量调整,thirdReceive=签收调整不能为空")
        @Size(max = 30,message = "调整类型:firstMileCost=费用调整,firstMileWeight=重量调整,thirdReceive=签收调整最大长度不能超过30位")
        private String sourceType;

        /**
        * 操作类型:manual=人工调整,auto=系统计算
        */
        @NotBlank(message = "操作类型:manual=人工调整,auto=系统计算不能为空")
        @Size(max = 30,message = "操作类型:manual=人工调整,auto=系统计算最大长度不能超过30位")
        private String type;

        /**
        * 修改范围:current=仅修改当前值,box=修改同箱同SKU,order=修改同单同SKU
        */
//        @NotBlank(message = "修改范围:current=仅修改当前值,box=修改同箱同SKU,order=修改同单同SKU不能为空")
        @Size(max = 30,message = "修改范围:current=仅修改当前值,box=修改同箱同SKU,order=修改同单同SKU最大长度不能超过30位")
        private String changeRange;

        /**
        * 核算月份id
        */
        private String reportPeriodId;

        /**
        * 核算月份
        */
        private LocalDate reportPeriod;

        /**
        * 业务单号
        */
        @NotBlank(message = "业务单号不能为空")
        @Size(max = 30,message = "业务单号最大长度不能超过30位")
        private String businessCode;

        /**
        * 发货单id
        */
        @NotBlank(message = "发货单id不能为空")
        @Size(max = 19,message = "发货单id最大长度不能超过19位")
        private String deliveryId;

        /**
        * 发货单编码
        */
        @NotBlank(message = "发货单编码不能为空")
        @Size(max = 30,message = "发货单编码最大长度不能超过30位")
        private String deliveryCode;

        /**
        * 物流运单号
        */
        @NotBlank(message = "物流运单号不能为空")
        @Size(max = 200,message = "物流运单号最大长度不能超过200位")
        private String transportNo;

        /**
        * 平台skuNo
        */
        @NotBlank(message = "平台skuNo不能为空")
        @Size(max = 255,message = "平台skuNo最大长度不能超过255位")
        private String platformSkuNo;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 调整分类:boxNo=箱号,shippingCost=运费,declareCost=关税,otherTaxFee=其他税费,otherCost=其他费用
        */
        @NotBlank(message = "调整分类:boxNo=箱号,shippingCost=运费,declareCost=关税,otherTaxFee=其他税费,otherCost=其他费用不能为空")
        @Size(max = 30,message = "调整分类:boxNo=箱号,shippingCost=运费,declareCost=关税,otherTaxFee=其他税费,otherCost=其他费用最大长度不能超过30位")
        private String category;

        /**
        * 调整字段:current_period_allocated_cost=本期分摊费用,mid_period_transit_cost=冲期初在途费用,end_period_transit_cost=期末在途费用,end_period_estimated_cost=期末暂估费用,charged_weight=出库重量
        */
        @NotBlank(message = "调整字段:current_period_allocated_cost=本期分摊费用,mid_period_transit_cost=冲期初在途费用,end_period_transit_cost=期末在途费用,end_period_estimated_cost=期末暂估费用,charged_weight=出库重量不能为空")
        @Size(max = 200,message = "调整字段:current_period_allocated_cost=本期分摊费用,mid_period_transit_cost=冲期初在途费用,end_period_transit_cost=期末在途费用,end_period_estimated_cost=期末暂估费用,charged_weight=出库重量最大长度不能超过200位")
        private String categoryField;

        /**
        * 调整前数值
        */
//        @NotBlank(message = "调整前数值不能为空")
        @Size(max = 255,message = "调整前数值最大长度不能超过255位")
        private String oldValue;

        /**
        * 调整后数值
        */
        @NotBlank(message = "调整后数值不能为空")
        @Size(max = 255,message = "调整后数值最大长度不能超过255位")
        private String newValue;

        /**
        * 是否最新记录
        */
        @NotNull(message = "是否最新记录不能为空")
        private Boolean isLatest;

        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 物流单id
         */
        private String logisticsBillId;
        /**
         * 箱id
         */
        private String boxId;

    }


    @Data
    @NoArgsConstructor
    public static class PagingVO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 调整单号【可排序】
         */
        private String code;

        /**
         * 调整类型:firstMileCost=费用调整,firstMileWeight=重量调整,thirdReceive=签收调整
         * 【可排序】
         */
        private String sourceType;
        private String sourceTypeName;

        /**
         * 操作类型:manual=人工调整,auto=系统计算
         * 【可排序】
         */
        private String type;
        private String typeName;

        /**
         * 修改范围:current=仅修改当前值,box=修改同箱同SKU,order=修改同单同SKU
         * 【可排序】
         */
        private String changeRange;
        private String changeRangeName;

        /**
         * 核算月份id
         */
        private String reportPeriodId;

        /**
         * 核算月份
         * 【可排序】
         */
        private LocalDate reportPeriod;
        private String reportPeriodName;

        /**
         * 业务单号【可排序】
         */
        private String businessCode;

        /**
         * 发货单id
         */
        private String deliveryId;

        /**
         * 发货单编码【可排序】
         */
        private String deliveryCode;

        /**
         * 物流运单号【可排序】
         */
        private String transportNo;

        /**
         * 平台skuNo【可排序】
         */
        private String platformSkuNo;

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNO【可排序】
         */
        private String skuNo;

        /**
         * 调整分类:boxNo=箱号,shippingCost=运费,declareCost=关税,otherTaxFee=其他税费,otherCost=其他费用
         * 【可排序】
         */
        private String category;
        private String categoryName;

        /**
         * 调整字段:current_period_allocated_cost=本期分摊费用,mid_period_transit_cost=冲期初在途费用,end_period_transit_cost=期末在途费用,
         * end_period_estimated_cost=期末暂估费用,charged_weight=出库重量
         * FirstMileChangeRecordCategoryFieldEnum
         *
         * 【可排序】
         */
        private String categoryField;
        private String categoryFieldName;

        /**
         * 调整前数值【可排序】
         */
        private String oldValue;

        /**
         * 调整后数值【可排序】
         */
        private String newValue;

        /**
         * 是否最新记录
         */
        private Boolean isLatest;
        /**
         * 操作人【可排序】
         */
        private String createUserName;
        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;

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
    }
}