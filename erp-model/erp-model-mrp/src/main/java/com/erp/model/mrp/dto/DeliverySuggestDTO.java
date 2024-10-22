package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 发货计划请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-27
*/
@Data
@NoArgsConstructor
public class DeliverySuggestDTO implements Serializable {


    /**
     * 高级查询参数
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
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class ListParamDTO {

        /**
         * 来源id
         */
        private String sourceId;
    }


    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;
        /**
         * 平台
         */
        private String platform;
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
         * 国家名称
         */
        private String countryName;

        /**
         * 编码
         */
        private String code;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编码
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;

        /**
         * 创建类型（auto系统，manual人工）
         */
        private String dataType;

        /**
         * 创建类型（auto系统，manual人工）
         */
        private String dataTypeName;

        /**
         * 建议发货量
         */
        private Integer suggestDeliveryQty;

        /**
         * 建议发货量（系统）
         */
        private Integer sysSuggestDeliveryQty;

        /**
         * 建议发货日期
         */
        private LocalDate suggestDeliveryDate;

        /**
         * 建议发货日期（系统）
         */
        private LocalDate sysSuggestDeliveryDate;

        /**
         * 物流方式,LogisticsMethodEnum枚举
         */
        private String logisticsMethod;

        /**
         * 物流方式（系统）,LogisticsMethodEnum枚举
         */
        private String sysLogisticsMethod;

        /**
         * 物流方式名称,LogisticsMethodEnum枚举
         */
        private String logisticsMethodName;

        /**
         * 物流方式名称（系统）,LogisticsMethodEnum枚举
         */
        private String sysLogisticsMethodName;

        /**
         * 物流时效（天）
         */
        private Integer logisticsDays;

        /**
         * 物流时效（天）（系统）
         */
        private Integer sysLogisticsDays;

        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;

        /**
         * 预计可售日期（系统）
         */
        private LocalDate sysEstimateSalesDate;

        /**
         * 物流成本
         */
        private BigDecimal logisticsCost;

        /**
         * 物流成本（系统）
         */
        private BigDecimal sysLogisticsCost;
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
        * 编码
        */
        private String code;

        /**
        * 创建类型（auto系统，manual人工）
        */
        private String dataType;

        /**
        * 建议发货量
        */
        private Integer suggestDeliveryQty;

        /**
        * 建议发货日期
        */
        private LocalDate suggestDeliveryDate;

        /**
        * 物流方式,LogisticsMethodEnum枚举
        */
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        private Integer logisticsDays;

        /**
        * 预计可售日期
        */
        private LocalDate estimateSalesDate;

        /**
        * 物流成本
        */
        private BigDecimal logisticsCost;

        /**
        * 作废状态
        */
        private String invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;


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
    public static class UpdateDTO  {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 计划发货量（计划修正值）
         */
        private String planDeliveryQty;

        /**
         * 实际发货量（运营确认值）
         */
        private String actualDeliveryQty;

        /**
         * 发货备货量
         */
        private String deliveryStockUpQty;

        /**
         * 物流方式,LogisticsMethodEnum枚举
         */
        private String logisticsMethod;

        /**
         * 物流时效（天）
         */
        private Integer logisticsDays;

        /**
         * 建议发货日期
         */
        private LocalDate suggestDeliveryDate;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 创建类型（auto系统，manual人工）
        */
        @NotBlank(message = "创建类型（auto系统，manual人工）不能为空")
        @Size(max = 32,message = "创建类型（auto系统，manual人工）最大长度不能超过32位")
        private String dataType;

        /**
        * 建议发货量
        */
        @NotNull(message = "建议发货量不能为空")
        private Integer suggestDeliveryQty;

        /**
        * 建议发货日期
        */
        private LocalDate suggestDeliveryDate;

        /**
        * 物流方式,LogisticsMethodEnum枚举
        */
        @NotBlank(message = "物流方式,LogisticsMethodEnum枚举不能为空")
        @Size(max = 32,message = "物流方式,LogisticsMethodEnum枚举最大长度不能超过32位")
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        @NotNull(message = "物流时效（天）不能为空")
        private Integer logisticsDays;

        /**
        * 预计可售日期
        */
        private LocalDate estimateSalesDate;

        /**
        * 物流成本
        */
        @NotNull(message = "物流成本不能为空")
        @Digits(integer = 12, fraction = 4, message = "物流成本整数位不能超过12位，小数位不能超过4位")
        private BigDecimal logisticsCost;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;


    }

    /**
     * 下推发货计划
     */
    @Data
    @NoArgsConstructor
    public static class ViewPushDeliveryPlanDTO {
        /**
         * 单据类型
         */
        private String type;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 国家
         */
        private String country;
        /**
         * 国家名称
         */
        private String countryName;
        /**
         * 期望发货时间
         */
        private LocalDate deliveryDate;
        /**
         * 期望物流方式
         */
        private String logisticsMethod;
        /**
         * 期望物流方式名称
         */
        private String logisticsMethodName;
        /**
         * 备注
         */
        private String remark;
        /**
         * 下推发货计划明细
         */
        private List<ViewPushDeliveryPlanDetailDTO> detailList;
    }
    /**
     * 下推发货计划明细
     */
    @Data
    @NoArgsConstructor
    public static class ViewPushDeliveryPlanDetailDTO {
        /**
         * 编号
         */
        private String code;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 品名
         */
        private String productName;

        /**
         * 发货备货量
         */
        private Integer deliveryStockUpQty;

        /**
         * 计划发货量（计划修正值）
         */
        private Integer planDeliveryQty;
        /**
         * MSKU
         */
        private String mSKu;
        /**
         * FNSKU
         */
        private String fnSku;
        /**
         * ASIN
         */
        private String asin;
        /**
         * 平台产品名称
         */
        private String platformSkuName;
    }
}