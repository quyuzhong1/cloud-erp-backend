package com.erp.model.tms.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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
     * 批量更新字段
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateFieldDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 主键id集合
         */
        @NotEmpty(message = "至少选择一条报关单")
        private List<String> ids;

        /**
         * 修改的字段编号
         */
        @NotEmpty(message = "修改字段不能为空")
        private List<@Valid BatchUpdateFieldListDTO> fieldList;

    }

    /**
     * 批量更新字段
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateFieldListDTO {
        /**
         * 修改的字段编号
         */
        @NotBlank(message = "修改字段不能为空")
        private String updateFiledCode;

        /**
         * 字段值
         */
        private Object selectValue;

        /**
         * 字段显示值
         */
        private String selectLabel;
    }

    /**
     * 批量更新字段下拉配置
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateFieldDropDownDTO {
        /**
         * 字段编码
         */
        private String field;

        /**
         * 字段名称
         */
        private String name;

        /**
         * 控件类型（input/date/select）
         */
        private String controls;

        /**
         * 下拉接口地址
         */
        private String url;


        /**
         *下拉框显示值
         */
        private String selectLabel;


        /**
         *下拉框绑定值
         */
        private String selectValue;

        /**
         * 排序
         */
        private Integer index;
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
     * 报关状态详情
     */
    @Data
    @NoArgsConstructor
    public static class DeclareStatusDetailDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 报关状态
         */
        private String declareStatus;

        /**
         * 报关状态名称
         */
        private String declareStatusName;

        /**
         * 报关确认日期
         */
        private LocalDate declarConfirmDate;

        /**
         * 报关确认人id
         */
        private String declarUserId;

        /**
         * 报关确认人
         */
        private String declarUserName;
    }

    /**
     * 确认报关状态
     */
    @Data
    @NoArgsConstructor
    public static class ConfirmDeclareStatusDTO {
        /**
         * 主键id集合
         */
        @NotEmpty(message = "id集合不能为空")
        private List<String> ids;
        /**
         * 报关状态
         */
        @NotBlank(message = "报关状态不能为空")
        private String declareStatus;

        /**
         * 报关确认日期
         */
        private LocalDate declarConfirmDate;

        /**
         * 报关确认人id
         */
        private String declarUserId;

        /**
         * 报关确认人
         */
        private String declarUserName;
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
     * 发货单信息
     */
    @Data
    @NoArgsConstructor
    public static class SoOutDTO {
        /**
         * 业务id（发货单id或发货通知单id）
         */
        private String sourceId;

        /**
         * 业务code（发货单code或发货通知库单code）
         */
        private String sourceCode;

        /**
         *  销售出库单id
         */
        private String soOutstockId;
        /**
         *  销售出库单编码
         */
        private String soOutstockCode;

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
         * 要货申请id
         */
        private String requisitionId;

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
         * 合并报关明细（用于新增/编辑页面回显）
         */
        private List<MergeDeclareBillDetailDTO> mergeDetailList;
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

        /**
         *  来源id
         */
        private String sourceId;
        /**
         * 关联单号
         */
        private String sourceCode;
        /**
         *  销售出库单id
         */
        private String soOutstockId;
        /**
         *  销售出库单编码
         */
        private String soOutstockCode;
        /**
         * sku
         */
        private String sku;

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
     * 选中SKU查询报关表头参数
     */
    @Data
    @NoArgsConstructor
    public static class SelectedSkuHeaderParamDTO {
        /**
         * 选中的来源SKU明细
         */
        @NotEmpty(message = "选中的SKU信息不能为空")
        private List<SourceDeliveryDetailDTO> sourceDeliveryDetailList;
    }

    /**
     * 选中SKU查询报关表头返回
     */
    @Data
    @NoArgsConstructor
    public static class SelectedSkuHeaderDTO {
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
         * 提运单号
         */
        private String transportNo;

        /**
         * 件数
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
         * 业务单号【可排序】
         */
        private String businessCode;
        /**
         * 报关确认日期【可排序】
         */
        private LocalDate declareConfirmDate;
        /**
         * 报关员名称【可排序】
         */
        private String declareUserName;

        /**
         * 来源编号【可排序】
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
         * 发货仓名称【可排序】
         */
        private String fromWarehouseName;

        /**
         * 目的仓名称【仅头程报关单存在】【可排序】
         */
        private String destWarehouseName;

        /**
         * 中转仓名称【可排序】
         */
        private String transferWarehouseName;

        /**
         * 销售组织名称【仅B2B报关单存在】【可排序】
         */
        private String salesOrgName;
        /**
         * 备注【可排序】
         */
        private String remark;
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
         * 发货人类型
         */
        private String senderType;

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
         * 收货人id
         */
        private String receiverId;
        /**
         * 收货人名称
         */
        private String receiverName;
        /**
         * 收货人类型
         */
        private String receiverType;
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
         *  明细信息
         */
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList;
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

        /**
         * 是否按规则重新合并后保存
         */
        private Boolean isMerge = false;

        /**
         *  明细信息
         */
        @NotEmpty(message = "明细信息不能为空")
        private List<MergeDeclareBillDetailDTO> mergeDetailList;
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
         * 是否按规则重新合并后保存
         */
        private Boolean isMerge = false;

        /**
         *  明细信息
         */
        @NotEmpty(message = "明细信息不能为空")
        private List<MergeDeclareBillDetailDTO> mergeDetailList;

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
         * 发货人类型
         */
        private String senderType;

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
         * 收货人id
         */
        private String receiverId;
        /**
         * 收货人名称
         */
        private String receiverName;
        /**
         * 收货人类型
         */
        private String receiverType;

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



    @Data
    @NoArgsConstructor
    public static class BillSourceDTO {

        /**
         * 来源id
         */
        private String sourceId;
        /**
         *  来源编码
         */
        private String sourceCode;
        /**
         *  来源类型
         */
        private String sourceType;
        /**
         * 业务id
         */
        private String businessId;
        /**
         *  业务编码
         */
        private String businessCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListBillSourceDTO {

        /**
         *  来源id集合
         */
        private List<String> sourceIdList;
        /**
         * 来源编码集合
         */
        private List<String> sourceCodeList;
        /**
         * 业务id集合
         */
        private List<String> businessIdList;
        /**
         * 业务编码集合
         */
        private List<String> businessCodeList;
    }


    @Data
    @NoArgsConstructor
    public static class NotGenerateParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotGenerateDetailDTO {
        /**
         * 来源id
         */
        private String sourceId;
        /**
         *  来源编码
         */
        private String sourceCode;
        /**
         *  来源类型
         */
        private String sourceType;
        /**
         *  来源明细id
         */
        private String sourceDetailId;

        /**
         *  skuId
         */
        private String skuId;
        /**
         *  sku编码
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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddSplitDeclareDTO {
        /**
         *  拆分数据不能为空
         */
        @NotEmpty(message = "拆分数据不能为空")
        private List<SplitDeclareDTO> splitDeclareDTOList;
    }

    /**
     *  拆分信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitDeclareDTO {
        /**
         *  主表报关id
         */
        private String id;
        /**
         *  来源id
         */
        private String sourceId;
        /**
         *  箱号
         */
        private String boxNo;
        /**
         *  sku信息
         */
        private String skuDesc;
        /**
         * sku信息
         */
        private List<SplitDetailDTO> skuDetailList;
    }

    /**
     * 拆分明细信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitDetailDTO {
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         *  skuId
         */
        private String skuId;
        /**
         *  SKU编码
         */
        private String skuNo;
        /**
         *  数量
         */
        private Integer qty;
    }
    
    /**
     * 合并报关信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MergeDeclareBillDTO {
        /**
         *  报关单明细集合
         */
        private List<MergeDeclareBillDetailDTO> declareBillList;
    }

    /**
     *  合并报关明细信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MergeDeclareBillDetailDTO {
        /**
         *  主键id
         */
        private String id;

        /**
         *  业务单号+箱号
         */
        private String businessDesc;

        /**
         * 合并来源业务单号（英文逗号拼接，不含箱号）
         */
        private String businessOrderNos;

        /**
         * 合并代表 SKU id（取合并集合第一条来源明细）
         */
        private String leadSkuId;

        /**
         *  sku编码
         */
        private String skuNo;

        /**
         * 中国海关编码(商品编号)
         */
        private String hsCode;
        /**
         * 报关中文名（商品名称）
         */
        private String productNameCn;
        /**
         * 申报要素
         */
        private String declareElement;
        /**
         * 报关单位
         */
        private String unit;

        /**
         * 报关单位名称
         */
        private String unitName;
        /**
         * 单价
         */
        private BigDecimal unitPrice;
        /**
         * 数量
         */
        private Integer qty;

        /**
         * 总价（单价 × 合并后数量）
         */
        private BigDecimal totalAmount;

        /**
         * 原产国（合并集合第一条 SKU）
         */
        private String sourceCountry;

        /**
         * 原产国名称
         */
        private String sourceCountryName;

        /**
         * 最终目的国/运抵国
         */
        private String toCountry;

        /**
         * 最终目的国名称
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
         * 合并规则说明
         */
        private String mergeRemark;

        /**
         * 原发货明细数据
         */
        private List<SourceDeliveryDetailDTO> sourceDeliveryDetailList;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDeliveryDetailDTO {
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         *  来源类型
         */
        private String sourceType;

        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         *  业务id
         */
        private String businessId;
        /**
         *  业务但还要
         */
        private String businessCode;
        /**
         *  箱号
         */
        private String boxNo;
        /**
         *  skuId
         */
        private String skuId;
        /**
         *  sku编码
         */
        private String skuNo;

        /**
         * 中国海关编码(商品编号)
         */
        private String hsCode;
        /**
         * 报关中文名（商品名称）
         */
        private String productNameCn;
        /**
         * 申报要素
         */
        private String declareElement;
        /**
         * 报关单位
         */
        private String unit;

        /**
         * 报关单位名称
         */
        private String unitName;
        /**
         * 单价
         */
        private BigDecimal unitPrice;
        /**
         * 数量
         */
        private Integer qty;
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
         * 运抵国/最终目的国编码（国家维度）
         */
        private String countryId;

        /**
         * 运抵国/最终目的国名称
         */
        private String countryName;

        /**
         * 原产国（产品物流）
         */
        private String sourceCountry;

        /**
         * 原产国名称（产品物流）
         */
        private String sourceCountryName;

        /**
         * 境内货源地（产品物流）
         */
        private String sourceCargo;

        /**
         * 征免（产品物流）
         */
        private String exemption;

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
         * 中转仓名称
         */
        private String transferWarehouseNames;
        /**
         * 销售组织ID
         */
        private String salesOrgId;
        /**
         * 销售组织名称
         */
        private String salesOrgName;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PushDeclareBeforeParamDTO {
        /**
         *  是否合并,true是，false否
         */
        private Boolean isMultipleMerge = false;
        /**
         *  下推的主表ids
         */
        @NotEmpty(message = "选择ids不能为空")
        private List<String> ids;
        /**
         *  下推的明细ids
         */
        private List<String> detailIds;
    }



    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutoMergeDeclareBillViewDTO {
        /**
         *  是否批量合并,true是，false否
         */
        private Boolean isMultipleMerge = false;
        /**
         * 需要报关信息
         */
        private List<SourceDeliveryDetailDTO> sourceDeliveryDetailList;

    }


    /**
     * 拆分保存时按原报关单合同号递增后缀：{@code 原号_1}、{@code 原号_2}…（再次拆分时原号若已为 {@code xxx_1} 则得到 {@code xxx_1_1}）。
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class SplitDeclareCodeSequence {
        private final String baseCode;
        private int sequence;

        public SplitDeclareCodeSequence(String baseCode) {
            this.baseCode = baseCode;
        }

        public String nextCode() {
            sequence++;
            return baseCode + "_" + sequence;
        }
    }
}
