package com.erp.model.tms.dto;

import java.math.BigDecimal;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 报关明细中间表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-04-27
*/
@Data
@NoArgsConstructor
public class DeliveryDeclareDetailMidDTO implements Serializable {



     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;

         /**
         * 数量
         */
         private Integer count;

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

     }


    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 备注
        */
        private String remark;

        /**
        * 报关状态：
        */
        private String declareStatus;

        /**
        * 生成状态
        */
        private String generateStatus;

        /**
        * 来源单据id
        */
        private String sourceId;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 单据分类: firstMileDelivery=头程发货单, soDeliveryNotice=B2B发货通知单
        */
        private String sourceType;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 业务单据id
        */
        private String businessId;

        /**
        * 业务单号
        */
        private String businessCode;

        /**
        * 业务单据类型
        */
        private String businessType;

        /**
        * 合同协议号
        */
        private String contractNo;

        /**
        * 商品id
        */
        private String skuId;

        /**
        * 商品SKU
        */
        private String skuNo;

        /**
        * 组合品SKU
        */
        private String comboSkuNo;

        /**
        * 币种
        */
        private String currency;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 报关单主表id
        */
        private String declareId;

        /**
        * 报关单号
        */
        private String declareCode;

        /**
        * 报关单明细id
        */
        private String declareDetailId;

        /**
        * 箱号
        */
        private String boxNo;

        /**
        * 中国海关编码
        */
        private String hsCode;

        /**
        * 报关中文名称
        */
        private String productNameCn;

        /**
        * 申报要素
        */
        private String declareElement;

        /**
        * 单位
        */
        private String unit;

        /**
        * 出口申报单价
        */
        private BigDecimal unitPrice;

        /**
        * 最新中国海关编码
        */
        private String latestHsCode;

        /**
        * 最新报关中文名称
        */
        private String latestProductNameCn;

        /**
        * 最新申报要素
        */
        private String latestDeclareElement;

        /**
        * 最新单位
        */
        private String latestUnit;

        /**
        * 最新出口申报单价
        */
        private BigDecimal latestUnitPrice;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 发货仓ID
        */
        private String fromWarehouseId;

        /**
        * 发货仓名称
        */
        private String fromWarehouseName;

        /**
        * 目的仓ID
        */
        private String destWarehouseId;

        /**
        * 目的仓名称
        */
        private String destWarehouseName;

        /**
        * 中转仓IDs(逗号分隔)
        */
        private String transferWarehouseIds;

        /**
        * 销售组织ID
        */
        private String salesOrgId;

        /**
        * 销售组织名称
        */
        private String salesOrgName;

        /**
        * 报关状态名称
        */
        private String declareStatusName;

        /**
        * 生成状态名称
        */
        private String generateStatusName;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

    }


    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        private String  id;

        /**
        * 备注
        */
        private String remark;

        /**
        * 报关状态：
        */
        private String declareStatus;

        /**
        * 生成状态
        */
        private String generateStatus;

        /**
        * 来源单据id
        */
        private String sourceId;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 单据分类: firstMileDelivery=头程发货单, soDeliveryNotice=B2B发货通知单
        */
        private String sourceType;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 业务单据id
        */
        private String businessId;

        /**
        * 业务单号
        */
        private String businessCode;

        /**
        * 业务单据类型
        */
        private String businessType;

        /**
        * 合同协议号
        */
        private String contractNo;

        /**
        * 商品id
        */
        private String skuId;

        /**
        * 商品SKU
        */
        private String skuNo;

        /**
        * 组合品SKU
        */
        private String comboSkuNo;

        /**
        * 币种
        */
        private String currency;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 报关单主表id
        */
        private String declareId;

        /**
        * 报关单号
        */
        private String declareCode;

        /**
        * 报关单明细id
        */
        private String declareDetailId;

        /**
        * 箱号
        */
        private String boxNo;

        /**
        * 中国海关编码
        */
        private String hsCode;

        /**
        * 报关中文名称
        */
        private String productNameCn;

        /**
        * 申报要素
        */
        private String declareElement;

        /**
        * 单位
        */
        private String unit;

        /**
        * 出口申报单价
        */
        private BigDecimal unitPrice;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 发货仓ID
        */
        private String fromWarehouseId;

        /**
        * 发货仓名称
        */
        private String fromWarehouseName;

        /**
        * 目的仓ID
        */
        private String destWarehouseId;

        /**
        * 目的仓名称
        */
        private String destWarehouseName;

        /**
        * 中转仓IDs(逗号分隔)
        */
        private String transferWarehouseIds;

        /**
        * 销售组织ID
        */
        private String salesOrgId;

        /**
        * 销售组织名称
        */
        private String salesOrgName;


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
    public static class CommonDTO extends SuperDTO {

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 报关状态：
        */
        @NotBlank(message = "报关状态：不能为空")
        @Size(max = 50,message = "报关状态：最大长度不能超过50位")
        private String declareStatus;

        /**
        * 生成状态
        */
        @Size(max = 50,message = "生成状态最大长度不能超过50位")
        private String generateStatus;

        /**
        * 来源单据id
        */
        @NotBlank(message = "来源单据id不能为空")
        @Size(max = 19,message = "来源单据id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源单号
        */
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 255,message = "来源单号最大长度不能超过255位")
        private String sourceCode;

        /**
        * 单据分类: firstMileDelivery=头程发货单, soDeliveryNotice=B2B发货通知单
        */
        @NotBlank(message = "单据分类: firstMileDelivery=头程发货单, soDeliveryNotice=B2B发货通知单不能为空")
        @Size(max = 255,message = "单据分类: firstMileDelivery=头程发货单, soDeliveryNotice=B2B发货通知单最大长度不能超过255位")
        private String sourceType;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 业务单据id
        */
        @NotBlank(message = "业务单据id不能为空")
        @Size(max = 19,message = "业务单据id最大长度不能超过19位")
        private String businessId;

        /**
        * 业务单号
        */
        @NotBlank(message = "业务单号不能为空")
        @Size(max = 50,message = "业务单号最大长度不能超过50位")
        private String businessCode;

        /**
        * 业务单据类型
        */
        @NotBlank(message = "业务单据类型不能为空")
        @Size(max = 50,message = "业务单据类型最大长度不能超过50位")
        private String businessType;

        /**
        * 合同协议号
        */
        @NotBlank(message = "合同协议号不能为空")
        @Size(max = 100,message = "合同协议号最大长度不能超过100位")
        private String contractNo;

        /**
        * 商品id
        */
        @NotBlank(message = "商品id不能为空")
        @Size(max = 19,message = "商品id最大长度不能超过19位")
        private String skuId;

        /**
        * 商品SKU
        */
        @Size(max = 64,message = "商品SKU最大长度不能超过64位")
        private String skuNo;

        /**
        * 组合品SKU
        */
        @Size(max = 64,message = "组合品SKU最大长度不能超过64位")
        private String comboSkuNo;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 64,message = "币种最大长度不能超过64位")
        private String currency;

        /**
        * 币种符号
        */
        @NotBlank(message = "币种符号不能为空")
        @Size(max = 64,message = "币种符号最大长度不能超过64位")
        private String currencySymbol;

        /**
        * 报关单主表id
        */
        @NotBlank(message = "报关单主表id不能为空")
        @Size(max = 19,message = "报关单主表id最大长度不能超过19位")
        private String declareId;

        /**
        * 报关单号
        */
        @NotBlank(message = "报关单号不能为空")
        @Size(max = 100,message = "报关单号最大长度不能超过100位")
        private String declareCode;

        /**
        * 报关单明细id
        */
        @NotBlank(message = "报关单明细id不能为空")
        @Size(max = 19,message = "报关单明细id最大长度不能超过19位")
        private String declareDetailId;

        /**
        * 箱号
        */
        @NotBlank(message = "箱号不能为空")
        @Size(max = 50,message = "箱号最大长度不能超过50位")
        private String boxNo;

        /**
        * 中国海关编码
        */
        @NotBlank(message = "中国海关编码不能为空")
        @Size(max = 50,message = "中国海关编码最大长度不能超过50位")
        private String hsCode;

        /**
        * 报关中文名称
        */
        @NotBlank(message = "报关中文名称不能为空")
        @Size(max = 255,message = "报关中文名称最大长度不能超过255位")
        private String productNameCn;

        /**
        * 申报要素
        */
        @NotBlank(message = "申报要素不能为空")
        private String declareElement;

        /**
        * 单位
        */
        @NotBlank(message = "单位不能为空")
        @Size(max = 32,message = "单位最大长度不能超过32位")
        private String unit;

        /**
        * 出口申报单价
        */
        @NotNull(message = "出口申报单价不能为空")
        @Digits(integer = 14, fraction = 4, message = "出口申报单价整数位不能超过14位，小数位不能超过4位")
        private BigDecimal unitPrice;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 发货仓ID
        */
        private String fromWarehouseId;

        /**
        * 发货仓名称
        */
        private String fromWarehouseName;

        /**
        * 目的仓ID
        */
        private String destWarehouseId;

        /**
        * 目的仓名称
        */
        private String destWarehouseName;

        /**
        * 中转仓IDs(逗号分隔)
        */
        private String transferWarehouseIds;

        /**
        * 销售组织ID
        */
        private String salesOrgId;

        /**
        * 销售组织名称
        */
        private String salesOrgName;


    }

    /**
     * 自动生成中间表数据DTO
     */
    @Data
    @NoArgsConstructor
    public static class AutoGenerateMidDTO implements Serializable {
        /**
         * 来源单据id
         */
        private String sourceId;

        /**
         * 单据分类: firstMileDelivery | soDeliveryNotice
         */
        private String sourceType;

        /**
         * 生成时机: afterAdd | afterApprove
         */
        private String billGenerateTiming;

        /**
         * 是否检查自动生成配置
         */
        private Boolean checkCfg = true;
    }

}
