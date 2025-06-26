package com.erp.model.scm.dto;

import java.math.BigDecimal;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.scm.entity.CfgSupplierSalesConditionEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * <p>
 * 销量设置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-06-13
*/
@Data
@NoArgsConstructor
public class CfgSupplierSalesDTO implements Serializable {

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class ListAllDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 供应商id
         */
        private String supplierId;
        private String supplierCode;
        private String supplierName;

        /**
         * 页面权限：view=仅查看,download=查看并下载
         */
        private String permission;

        /**
         * 日均销量类型：dailyAvg3Days=按3天日均计算,dailyAvg7Days=按7天日均计算,dailyAvg30Days=按30天日均计算,dailyAvg60Days=按60天日均计算,dailyAvg90Days=按90天日均计算
         */
        private String dailySalesType;

        /**
         * 销量比例类型：purchaseRatio=按照供应商采购比例,salesStatisticRatio=按照销量统计比例
         */
        private String salesRatioType;

        /**
         * 销量比例值
         */
        private BigDecimal salesRatio;

        /**
         * 是否启用通知
         */
        private Boolean noticeEnabled;

        /**
         * 统计维度：deliveryTime=按照出库时间,paymentTime=按照付款时间
         */
        private String dimension;

        /**
         * 禁用状态
         */
        private Boolean disabled;

        /**
         * 字段显示
         */
        private String displayField;

        /**
         * 是否启用sku黑名单
         */
        private Boolean isBlack = Boolean.FALSE;
        /**
         * sku黑名单集合
         */
        private List<CfgSupplierSalesConditionEntity> blackList;

        /**
         * sku查看配置
         */
        private List<CfgSupplierSalesConditionEntity> skuList;

        /**
         * 仓库类型
         */
        private String warehouseType;
        /**
         * 可销库存配置
         */
        private List<CfgSupplierSalesConditionEntity> saleableStockList ;

        /**
         * 销量统计配置
         */
        private List<CfgSupplierSalesConditionEntity> salesStatisticList ;
        /**
         * 通知配置执行条件
         */
        private List<CfgSupplierSalesConditionEntity> noticeList;



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
         * 供应商id
         */
        private String supplierId;
        private String supplierCode;
        private String supplierName;

        /**
         * 页面权限：view=仅查看,download=查看并下载
         */
        private String permission;
        private String permissionName;

        /**
         * 日均销量类型：dailyAvg3Days=按3天日均计算,dailyAvg7Days=按7天日均计算,dailyAvg30Days=按30天日均计算,dailyAvg60Days=按60天日均计算,dailyAvg90Days=按90天日均计算
         */
        private String dailySalesType;
        private String dailySalesTypeName;

        /**
         * 销量比例类型：purchaseRatio=按照供应商采购比例,salesStatisticRatio=按照销量统计比例
         */
        private String salesRatioType;
        private String salesRatioTypeName;

        /**
         * 销量比例值
         */
        private BigDecimal salesRatio;

        /**
         * 是否启用通知
         */
        private Boolean noticeEnabled;

        /**
         * 统计维度：deliveryTime=按照出库时间,paymentTime=按照付款时间
         */
        private String dimension;
        private String dimensionName;

        /**
         * 禁用状态
         */
        private Boolean disabled;

        /**
         * 字段显示
         */
        private String displayField;
        private List<String> displayFieldList;
        private List<String> displayFieldNameList;

        /**
         * 是否启用sku黑名单
         */
        private Boolean isBlack = Boolean.FALSE;
        /**
         * sku黑名单集合
         */
        private List<String> blackList;
        private List<String> blackNameList;

        /**
         * sku查看配置
         */
        private List<CfgSupplierSalesConditionDTO.View> skuList;

        /**
         * 仓库类型
         */
        private String warehouseType;
        private String warehouseTypeName;
        /**
         * 可销库存配置
         */
        private List<CfgSupplierSalesConditionDTO.View> saleableStockList ;

        /**
         * 销量统计配置
         */
        private List<CfgSupplierSalesConditionDTO.View> salesStatisticList ;
        /**
         * 通知配置执行条件
         */
        private List<CfgSupplierSalesConditionDTO.View> noticeList;

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
         * 主键id
         */
        private String id;

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;
        private String supplierCode;
        private String supplierName;

        /**
        * 页面权限：view=仅查看,download=查看并下载
        */
        @NotBlank(message = "页面权限不能为空")
        private String permission;

        /**
        * 日均销量类型：dailyAvg3Days=按3天日均计算,dailyAvg7Days=按7天日均计算,dailyAvg30Days=按30天日均计算,dailyAvg60Days=按60天日均计算,dailyAvg90Days=按90天日均计算
        */
        @NotBlank(message = "日均销量不能为空")
        private String dailySalesType;

        /**
        * 销量比例类型：purchaseRatio=按照供应商采购比例,salesStatisticRatio=按照销量统计比例
        */
        @NotBlank(message = "销量比例不能为空")
        private String salesRatioType;

        /**
        * 销量比例值(%)
        */
        @Digits(integer = 3, fraction = 2, message = "销量比例值整数位不能超过3位，小数位不能超过2位")
        private BigDecimal salesRatio;

        /**
        * 是否启用通知
        */
        @NotNull(message = "是否启用通知不能为空")
        private Boolean noticeEnabled;

        /**
        * 统计维度：deliveryTime=按照出库时间,paymentTime=按照付款时间
        */
        @NotBlank(message = "统计维度不能为空")
        private String dimension;

        /**
        * 禁用状态
        */
        @NotNull(message = "禁用状态不能为空")
        private Boolean disabled;

        /**
        * 字段显示
        */
        private String displayField;
        @NotEmpty(message = "字段显示不能为空")
        private List<String> displayFieldList;

        /**
         * 是否启用sku黑名单
         */
        private Boolean isBlack;
        /**
         * sku黑名单集合
         */
        private List<String> blackList;

        private CfgSupplierSalesConditionDTO.ConditionDTO blackCondition;

        /**
         * sku查看配置
         */
        private List<CfgSupplierSalesConditionDTO.ConditionDTO> skuList;

        /**
         * 仓库类型
         */
        private String warehouseType;
        /**
         * 可销库存配置
         */
        private List<CfgSupplierSalesConditionDTO.ConditionDTO> saleableStockList ;

        /**
         * 销量统计配置
         */
        private List<CfgSupplierSalesConditionDTO.ConditionDTO> salesStatisticList ;
        /**
         * 通知配置执行条件
         */
        private List<CfgSupplierSalesConditionDTO.ConditionDTO> noticeList;

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO {

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商编码
         */
        private String supplierCode;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 页面权限：view=仅查看,download=查看并下载
         */
        private String permission;
        private String permissionName;

        /**
         * 日均销量类型：dailyAvg3Days=按3天日均计算,dailyAvg7Days=按7天日均计算,dailyAvg30Days=按30天日均计算,dailyAvg60Days=按60天日均计算,dailyAvg90Days=按90天日均计算
         */
        private String dailySalesType;
        private String dailySalesTypeName;

        /**
         * 销量比例类型：purchaseRatio=按照供应商采购比例,salesStatisticRatio=按照销量统计比例
         */
        private String salesRatioType;
        private String salesRatioTypeName;

        /**
         * 销量比例值
         */
        private BigDecimal salesRatio;
        private String salesRatioStr ;

        /**
         * 是否启用通知
         */
        private Boolean noticeEnabled;
        private String noticeEnabledName;

        /**
         * 统计维度：deliveryTime=按照出库时间,paymentTime=按照付款时间
         */
        private String dimension;
        private String dimensionName;

        /**
         * 禁用状态
         */
        private Boolean disabled;
        private String disabledName;
    }


    @Data
    @NoArgsConstructor
    public static class BaseDTO {
        /**
         * id
         */
        private String id;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }


    /**
     * 分页列表查询参数
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

        /**
         * 主键id
         */
        private List<String> ids;

    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class EnableStatusDTO {

        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        @NotNull(message = "状态不能为空")
        private Boolean disabled;

    }

}