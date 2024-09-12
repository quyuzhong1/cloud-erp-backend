package com.erp.model.tms.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 报关单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
*/
@Data
@NoArgsConstructor
public class TmsDeclareBillDTO implements Serializable {

    /**
     * 删除
     */
    @Data
    @NoArgsConstructor
    public static class DeleteDTO {
        /**
         * id集合
         */
        @NotNull(message = "id集合不能为空")
        private List<String> ids;

    }


    /**
     * 更新报关状态DTO
     */
    @Data
    @NoArgsConstructor
    public static class MergeDeclareDTO {
        /**
         * id集合
         */
        @NotNull(message = "id集合不能为空")
        private List<String> ids;

        /**
         * 合同协议号
         */
        private String code;

    }

    /**
     * 更新报关状态DTO
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDeclareStatusDTO {
        /**
         * id集合
         */
        @NotNull(message = "id集合不能为空")
        private List<String> ids;

        /**
         * 报关日期
         */
        private LocalDate date;

    }

    /**
     * 查询可以生成报关单的DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuerySourceDTO {

        /**
         * 装箱状态
         */
        private String packingStatus;

        /**
         * 报关状态
         */
        private String declareStatus;

        /**
         * 发货单ids
         */
        private List<String> ids;
    }

    /**
     * 销售出库单信息
     */
    @Data
    @NoArgsConstructor
    public static class SoOutDTO {
        /**
         * 业务id（发货单id或销售出库单id）
         */
        private String sourceId;

        /**
         * 业务code（发货单code或销售出库单code）
         */
        private String sourceCode;
        /**
         * 业务类型
         */
        private String businessType;

        /**
         * 目的国
         */
        private String country;

        /**
         * 目的国名称
         */
        private String countryName;
        /**
         * 运输方式
         */
        private String shippingMethod;

        /**
         * 运输方式名称
         */
        private String shippingMethodName;

        /**
         * 物流商id
         */
        private String logisticsSupplierId;

        /**
         * 物流商名称
         */
        private String logisticsSupplierName;

        /**
         * 总箱数
         */
        private Integer boxQty;

        /**
         * 毛重
         */
        private BigDecimal grossWeight;

        /**
         * 净重
         */
        private BigDecimal netWeight;

        /**
         * 产品明细
         */
        private List<ProductDetail> productDetailList;
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
         * 业务id（发货单id或销售出库单id）
         */
        private String sourceId;

        /**
         * 业务code（发货单code或销售出库单code）
         */
        private String sourceCode;
        /**
         * 业务类型
         */
        private String businessType;

        /**
         * 目的国
         */
        private String country;

        /**
         * 目的国名称
         */
        private String countryName;
        /**
         * 运输方式
         */
        private String shippingMethod;

        /**
         * 运输方式名称
         */
        private String shippingMethodName;

        /**
         * 物流商id
         */
        private String logisticsSupplierId;

        /**
         * 物流商名称
         */
        private String logisticsSupplierName;

        /**
         * 柜号
         */
        private String counterNo;

        /**
         * 总箱数
         */
        private Integer boxQty;

        /**
         * 毛重
         */
        private BigDecimal grossWeight;

        /**
         * 净重
         */
        private BigDecimal netWeight;

        /**
         * 产品明细
         */
        private List<ProductDetail> productDetailList;
        /**
         * 装箱信息
         */
        private List<PackingDTO> packingDTOList;
    }

    /**
     * 装箱信息
     */
    @Data
    @NoArgsConstructor
    public static class PackingDTO {

        private String id;
        /**
         * sku
         */
        private String sku;
        /**
         * 关联单号
         */
        private String code;

        /**
         * 装箱SKU
         * 例：（sku*qty+sku*qty+...）
         */
        private String boxDesc;

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
         * 体积重
         */
        private BigDecimal volumeWeight;

        /**
         * 长宽高相乘结果
         */
        private BigDecimal multiplySize;
    }

    /**
     * 产品明细
     */
    @Data
    @NoArgsConstructor
    public static class ProductDetail {
        /**
         * sku id
         */
        private String skuId;
        /**
         * sku no
         */
        private String skuNo;
        /**
         * 中国海关编码(商品编号)
         */
        private String customsCode;
        /**
         * 报关中文名（商品名称）
         */
        private String declareChineseName;
        /**
         * 申报要素
         */
        private String declareElement;
        /**
         * 报关单位
         */
        private String declareUnit;

        /**
         * 报关单位名称
         */
        private String declareUnitName;
        /**
         * 单价
         */
        private BigDecimal price;
        /**
         * 数量
         */
        private Integer qty;

        /**
         * 总价
         */
        private BigDecimal totalPrice;

        /**
         * 报关币别
         */
        private String declareCurrency;

        /**
         * 报关币别名称
         */
        private String declareCurrencyName;
        /**
         * 报关币别符号
         */
        private String declareCurrencySymbol;
        /**
         * 原产国
         */
        private String sourceCountry;
        /**
         * 原产国名称
         */
        private String sourceCountryName;

        /**
         * 最终目的国（地区）
         */
        private String toCountry;

        /**
         * 最终目的国（地区）名称
         */
        private String toCountryName;
        /**
         * 境内货源地
         */
        private String sourceCargo;
        /**
         * 征免
         */
        private String exemption;
        /**
         * 净重
         */
        private BigDecimal netWeight;

    }

    @Data
    @NoArgsConstructor
    public static class MergedDTO {
        private String id;
        private String sourceCode;
    }

    /**
     * 报关单导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO {
        /**
         * id
         */
        private String id;

        private String type;

        private String sourceCode;

        /**
         * 预录入编号
         */
        private String preInputNo;

        /**
         * 申报地海关
         */
        private String destCustoms;
        /**
         * 发货人id
         */
        private String senderId;

        /**
         * 发货人名称
         */
        private String senderName;
        /**
         * 出境关别
         */
        private String exportCustomsName;
        /**
         * 出口日期
         */
        private LocalDate exportDate;

        /**
         * 报关日期
         */
        private LocalDate declareDate;

        /**
         * 收货人名称
         */
        private String receiverName;

        /**
         * 运输方式
         */
        private String shippingMethod;

        /**
         * 运输方式名称
         */
        private String shippingMethodName;

        /**
         * 提运单号
         */
        private String transportNo;

        /**
         * 监管方式
         */
        private String dictSupervisionMethod;

        /**
         * 监管方式名称 tms/drop/down/dict/list?key=declareSupervisionMethod
         */
        private String dictSupervisionMethodName;

        /**
         * 征免性质
         */
        private String dictNatureLevy;

        /**
         * 征免性质名称 tms/drop/down/dict/list?key=declareNatureLevy
         */
        private String dictNatureLevyName;

        /**
         * 许可证号
         */
        private String licenseNo;

        /**
         * 合同协议号
         */
        private String code;

        /**
         * 贸易国
         */
        private String tradingArea;

        /**
         * 运抵区
         */
        private String toArea;

        /**
         * 运抵港
         */
        private String toPort;

        /**
         * 出境口岸
         */
        private String exportPort;

        /**
         * 包装种类
         */
        private String dictPackType;

        /**
         * 包装种类名称 tms/drop/down/dict/list?key=declarePackType
         */
        private String dictPackTypeName;

        /**
         * 总箱数
         */
        private Integer boxQty;

        /**
         * 毛重
         */
        private BigDecimal grossWeight;

        /**
         * 净重
         */
        private BigDecimal netWeight;

        /**
         * 成交方式
         */
        private String dictTransactionMethod;

        /**
         * 成交方式名称  tms/drop/down/dict/list?key=declareTransactionMethod
         */
        private String dictTransactionMethodName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 运费
         */
        private BigDecimal shippingFee;

        /**
         * 保费
         */
        private BigDecimal insuranceFee;

        /**
         * 杂费
         */
        private BigDecimal otherFee;

        /**
         * 数量
         */
        private Integer totalQty;

        /**
         * 总价
         */
        private BigDecimal totalPrice;

        private String countryName;

        /**
         * 产品明细
         */
        private List<ExportProductDetail> productDetailList;
    }

    /**
     * 导出产品信息
     */
    @Data
    @NoArgsConstructor
    public static class ExportProductDetail {

        private String mainId;

        private Integer rowNum;

        /**
         * sku id
         */
        private String skuId;

        /**
         * 中国海关编码(商品编号)
         */
        private String customsCode;
        /**
         * 报关中文名（商品名称）
         */
        private String declareChineseName;
        /**
         * 申报要素
         */
        private String declareElement;
        /**
         * 报关单位
         */
        private String declareUnit;

        /**
         * 报关单位名称
         */
        private String declareUnitName;
        /**
         * 单价
         */
        private BigDecimal price;
        /**
         * 数量
         */
        private Integer qty;

        /**
         * 总价
         */
        private BigDecimal totalPrice;

        /**
         * 报关币别
         */
        private String declareCurrency;

        /**
         * 报关币别名称
         */
        private String declareCurrencyName;

        /**
         * 原产国
         */
        private String sourceCountry;
        /**
         * 原产国名称
         */
        private String sourceCountryName;

        /**
         * 最终目的国（地区）
         */
        private String toCountry;

        /**
         * 最终目的国（地区）名称
         */
        private String toCountryName;
        /**
         * 境内货源地
         */
        private String sourceCargo;
        /**
         * 征免
         */
        private String exemption;
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
        @ExcelIgnore
        private String id;

        /**
         * 合同协议号(可排序)
         */
        @ExcelProperty(value = "合同协议号")
        @ColumnWidth(20)
        private String code;

        /**
         * 合并来源Id
         */
        @ExcelIgnore
        private String mergeSourceId;

        /**
         * 来源编号
         */
        @ExcelIgnore
        private String sourceCode;

        /**
         * 报关状态(可排序) tms/common/enumDropDown?type=DeclareStatus
         */
        @ExcelIgnore
        private String declareStatus;

        /**
         * 报关状态名称
         */
        @ExcelProperty(value = "报关状态")
        @ColumnWidth(10)
        private String declareStatusName;

        /**
         * 物流商id
         */
        @ExcelIgnore
        private String logisticsSupplierId;

        /**
         * 物流商名称
         */
        @ExcelProperty(value = "物流商名称")
        @ColumnWidth(20)
        private String logisticsSupplierName;

        /**
         * 类型（发货类型或订单类型）(可排序)
         */
        @ExcelIgnore
        private String businessType;

        /**
         * 类型名称
         */
        @ExcelProperty(value = "类型名称")
        @ColumnWidth(10)
        private String businessTypeName;

        /**
         * 关联单号List
         */
        @ExcelProperty(value = "关联单号")
        @ColumnWidth(20)
        private String sourceCodeList;


        /**
         * 目的国家(可排序)
         */
        @ExcelIgnore
        private String country;

        /**
         * 目的国家名称(可排序)
         */
        @ExcelProperty(value = "目的国家名称")
        @ColumnWidth(10)
        private String countryName;

        /**
         * 商品数
         */
        @ExcelProperty(value = "商品数")
        @ColumnWidth(10)
        private Integer goodsQty;

        /**
         * 总箱数(可排序)
         */
        @ExcelProperty(value = "总箱数")
        @ColumnWidth(10)
        private Integer boxQty;

        /**
         * 总净重(可排序)
         */
        @ExcelProperty(value = "总净重(kg)")
        @ColumnWidth(10)
        private BigDecimal netWeight;
        /**
         * 总毛重(可排序)
         */
        @ExcelProperty(value = "总毛重(kg)")
        @ColumnWidth(10)
        private BigDecimal grossWeight;
        /**
         * 报关日期(可排序)
         */
        @ExcelProperty(value = "报关日期")
        @ColumnWidth(20)
        private LocalDate declareDate;
        /**
         * 报关类型(可排序)
         */
        @ExcelIgnore
        private String declareType;

        /**
         * 报关类型名称
         */
        @ExcelProperty(value = "报关类型名称")
        @ColumnWidth(15)
        private String declareTypeName;

        /**
         * 创建人(可排序)
         */
        @ExcelProperty(value = "创建人")
        @ColumnWidth(20)
        private String createUserName;

        /**
         * 创建时间(可排序)
         */
        @ExcelProperty(value = "创建时间")
        @ColumnWidth(20)
        private LocalDateTime createTime;

        /**
         * 是否合并
         */
        @ExcelIgnore
        private Boolean isMerged = Boolean.FALSE;

        /**
         * 是否作废
         */
        @ExcelIgnore
        private Boolean isInvalid= Boolean.FALSE;
    }

    /**
     * 列表统计返回结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatisticsAllDTO {

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
        private Integer count = 0;
    }
    /**
     * 列表统计返回结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatisticsDTO extends SortDTO{

        private LocalDateTime beginDate;

        private LocalDateTime endDate;

        private String declareStatus;

        private String type;
    }

    /**
     * 列表统计返回结果
     */
    @Data
    @NoArgsConstructor
    public static class StatisticsVO {

        /**
         * 上月发货
         */
        private Integer lastMonthDelivery;

        /**
         * 本月发货
         */
        private Integer thisMonthDelivery;

        /**
         * 上月报关
         */
        private Integer lastMonthDeclare;

        /**
         * 本月报关
         */
        private Integer thisMonthDeclare;
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

        /**
         * 类型，后端处理，前端不用管
         */
        private String type;

        /**
         * 导出时的报关状态
         */
        private List<String> exportDeclareStatus;
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
         * 业务id（发货单id或销售出库单id）
         */
        private String sourceId;

        /**
         * 业务code（发货单code或销售出库单code）
         */
        private List<String> sourceCodeList;

        /**
         * 提运单号
         */
        private String transportNo;

        /**
         * 预录入编号
         */
        private String preInputNo;

        /**
         * 申报地海关
         */
        private String destCustoms;

        /**
         * 报关类型 tms/drop/down/dict/list?key=declareDeclareType
         */
        private String declareType;

        /**
         * 报关类型名称
         */
        private String declareTypeName;


        /**
         * 发货人id
         */
        private String senderId;

        /**
         * 发货人名称
         */
        private String senderName;

        /**
         * 出境关别
         */
        private String exportCustomsName;

        /**
         * 出口日期
         */
        private LocalDate exportDate;

        /**
         * 报关日期
         */
        private LocalDate declareDate;

        /**
         * 收货人名称
         */
        private String receiverName;
        /**
         * 运输方式
         */
        private String shippingMethod;

        /**
         * 运输方式名称
         */
        private String shippingMethodName;

        /**
         * 物流商id
         */
        private String logisticsSupplierId;

        /**
         * 物流商名称
         */
        private String logisticsSupplierName;

        /**
         * 柜号
         */
        private String counterNo;

        /**
         * 监管方式
         */
        private String dictSupervisionMethod;

        /**
         * 监管方式名称 tms/drop/down/dict/list?key=declareSupervisionMethod
         */
        private String dictSupervisionMethodName;

        /**
         * 征免性质
         */
        private String dictNatureLevy;

        /**
         * 征免性质名称 tms/drop/down/dict/list?key=declareNatureLevy
         */
        private String dictNatureLevyName;

        /**
         * 许可证号
         */
        private String licenseNo;

        /**
         * 合同协议号
         */
        private String code;

        /**
         * 贸易国
         */
        private String tradingArea;

        /**
         * 运抵区
         */
        private String toArea;

        /**
         * 运抵港
         */
        private String toPort;

        /**
         * 出境口岸
         */
        private String exportPort;

        /**
         * 包装种类
         */
        private String dictPackType;

        /**
         * 包装种类名称 tms/drop/down/dict/list?key=declarePackType
         */
        private String dictPackTypeName;

        /**
         * 总箱数
         */
        private Integer boxQty;

        /**
         * 毛重
         */
        private BigDecimal grossWeight;

        /**
         * 净重
         */
        private BigDecimal netWeight;

        /**
         * 成交方式
         */
        private String dictTransactionMethod;

        /**
         * 成交方式名称  tms/drop/down/dict/list?key=declareTransactionMethod
         */
        private String dictTransactionMethodName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 运费
         */
        private BigDecimal shippingFee;

        /**
         * 保费
         */
        private BigDecimal insuranceFee;

        /**
         * 杂费
         */
        private BigDecimal otherFee;

        /**
         * 产品明细
         */
        private List<ProductDetail> productDetailList;
        /**
         * 装箱信息
         */
        private List<PackingDTO> packingDTOList;

    }

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusDTO {
        @NotNull(message = "ids不能为空")
        private List<String> ids;
        /**
         * 物流单状态
         */
        private String logisticsStatus;

        /**
         * 报关单状态
         */
        private String declareStatus;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 业务id（发货单id或销售出库单id）
         */
        @NotBlank(message = "来源类型：头程,B2B不能为空")
        private String sourceId;

        private Boolean isAuto = false;
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
         * 预录入编号
         */
        private String preInputNo;

        /**
         * 申报地海关
         */
        private String destCustoms;

        /**
         * 报关类型
         */
        @NotBlank(message = "报关类型不能为空")
        private String declareType;

        /**
         * 发货人id
         */
        private String senderId;

        /**
         * 出境关别
         */
        private String exportCustomsName;

        /**
         * 出口日期
         */
        private LocalDate exportDate;

        /**
         * 报关日期
         */
        private LocalDate declareDate;

        /**
         * 收货人名称
         */
        private String receiverName;

        /**
         * 监管方式
         */
        private String dictSupervisionMethod;

        /**
         * 征免性质
         */
        private String dictNatureLevy;

        /**
         * 许可证号
         */
        private String licenseNo;

        /**
         * 贸易国
         */
        private String tradingArea;

        /**
         * 运抵区
         */
        private String toArea;

        /**
         * 运抵港
         */
        private String toPort;

        /**
         * 出境口岸
         */
        private String exportPort;

        /**
         * 提运单号
         */
        private String transportNo;

        /**
         * 包装种类
         */
        private String dictPackType;

        /**
         * 成交方式
         */
        private String dictTransactionMethod;

        /**
         * 备注
         */
        private String remark;

        /**
         * 运费
         */
        private BigDecimal shippingFee;

        /**
         * 保费
         */
        private BigDecimal insuranceFee;

        /**
         * 杂费
         */
        private BigDecimal otherFee;

    }
}
