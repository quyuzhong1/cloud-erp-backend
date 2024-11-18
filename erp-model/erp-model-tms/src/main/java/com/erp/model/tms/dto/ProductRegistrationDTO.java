package com.erp.model.tms.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品备案表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-03-14
*/
@Data
@NoArgsConstructor
public class ProductRegistrationDTO implements Serializable {

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class ViewVO  {
        /**
         * sku
         */
        private String skuNo;

        /**
         * 备案状态
         */
        private String status;

        /**
         * 备案状态名称
         */
        private String statusName;

        /**
         * 备案平台
         */
        private String declarePlatform;

        /**
         * 备案平台名称
         */
        private String declarePlatformName;
        /**
         * 报关物流商名称
         */
        private String declareSupplierName;

        /**
         * 备案推送/拉取时间(最新)(可排序)
         */
        private LocalDateTime latestTime;

        /**
         * 详情
         */
        private List<ViewDetailVO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDetailVO  {
        /**
         * 接口字段中文
         */
        private String interfaceFieldCn;
        /**
         * 接口字段英文
         */
        private String interfaceFieldEn;

        /**
         * 数大臣字段
         */
        private String erpField;

        /**
         * 推送信息
         */
        private String pushValue;

        /**
         * 拉取信息
         */
        private String pullValue;

        /**
         * 最新信息
         */
        private String latestValue;
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

    /**
     * 分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingVO {

        @ExcelIgnore
        private String id;
        /**
         * skuId
         */
        @ExcelIgnore
        private String skuId;

        /**
         * sku(可排序)
         */
        @ExcelProperty(value = "sku")
        @ColumnWidth(20)
        private String skuNo;

        /**
         * 报关型号规格(可排序)
         */
        @ExcelProperty(value = "报关型号规格")
        @ColumnWidth(20)
        private String spu;

        /**
         * 报关物流商名称(可排序)
         */
        @ExcelProperty(value = "备案平台")
        private String declareSupplierName;

        /**
         * 备案平台(可排序)
         */
        @ExcelIgnore
        private String declarePlatform;

        /**
         * 备案平台名称(可排序)
         */
        @ExcelIgnore
        private String declarePlatformName;

        /**
         * 备案状态(可排序)
         */
        @ExcelIgnore
        private String status;

        /**
         * 备案状态名称(可排序)
         */
        @ExcelProperty(value = "备案状态")
        private String statusName;

        /**
         * 备案审核状态
         */
        @ExcelIgnore
        private String registrationApproveStatus;

        /**
         * 备案审核状态名称
         */
        @ExcelProperty(value = "备案审核状态")
        private String registrationApproveStatusName;

        /**
         * 中文报关名(可排序)
         */
        @ExcelProperty(value = "中文报关名")
        @ColumnWidth(20)
        private String declareCnName;

        /**
         * 报关申报价
         */
        @ExcelIgnore
        private BigDecimal declarePrice;

        /**
         * 币种符号
         */
        @ExcelIgnore
        private String declareCurrencySymbol;

        /**
         * 报关申报价+币种
         */
        @ExcelProperty(value = "报关申报价")
        private String completePrice;

        /**
         * 报关HSCODE(可排序)
         */
        @ExcelProperty(value = "报关HSCODE")
        @ColumnWidth(20)
        private String customsCode;

        /**
         * 申报要素(可排序)
         */
        @ExcelProperty(value = "申报要素")
        @ColumnWidth(40)
        private String declareElement;


        /**
         * 备案推送/拉取时间(最新)(可排序)
         */
        @ExcelProperty(value = "备案推送/拉取时间(最新)")
        @ColumnWidth(20)
        private LocalDateTime latestTime;

        /**
         * 备案不通过原因
         */
        @ExcelProperty(value = "备案不通过原因")
        @ColumnWidth(30)
        private String failureReason;
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
        private Integer count;
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
        * sku id
        */
        private String skuId;

        /**
        * sku no
        */
        private String skuNo;

        /**
        * 报关平台
        */
        private String declarePlatform;

        /**
        * 备注
        */
        private String remark;

        /**
        * 备案状态 draft 暂存  registering 备案中   registered 已备案  freeze 冻结  
        */
        private String status;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 产品英文名称
        */
        private String productNameEn;

        /**
        * 报关单位
        */
        private String declareUnit;

        /**
        * 商品id
        */
        private String goodId;

        /**
        * 产品型号
        */
        private String spu;

        /**
        * 申报币种
        */
        private String currency;

        /**
        * 申报价格
        */
        private BigDecimal declarePrice;

        /**
        * 毛重(g)
        */
        private BigDecimal grossWeight;

        /**
        * 长
        */
        private BigDecimal length;

        /**
        * 宽
        */
        private BigDecimal width;

        /**
        * 高
        */
        private BigDecimal height;

        /**
        * 是否带电
        */
        private Boolean isBattery;

        /**
        * 电池类型
        */
        private String batteryType;

        /**
        * 报关中文名
        */
        private String declareNameCn;

        /**
        * 海关编码
        */
        private String customsCode;

        /**
        * 第一数量
        */
        private BigDecimal firstNumber;

        /**
        * 第二数量
        */
        private BigDecimal secondNumber;

        /**
        * 申报要素
        */
        private String declareElement;

        /**
        * 供应商代码
        */
        private String supplierCode;

        /**
        * 条码类型
        */
        private String barcodeType;

        /**
        * 自定义条码
        */
        private String customBarcode;

        /**
        * 是否带发票
        */
        private Boolean isInvoice;

        /**
        * 是否零件类
        */
        private Boolean isParts;

        /**
        * 电池二级分类
        */
        private String batteryNote;

        /**
        * 图片url
        */
        private String url;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * skuIds
         */
        private List<String> skuIds;

        /**
         * 备案平台id
         */
        @NotBlank(message = "备案平台不能为空")
        private String declareSupplierId;

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
        * sku id
        */
        @NotBlank(message = "sku id不能为空")
        @Size(max = 19,message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
        * 报关平台
        */
        @NotBlank(message = "报关平台不能为空")
        @Size(max = 32,message = "报关平台最大长度不能超过32位")
        private String declarePlatform;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 备案状态 draft 暂存  registering 备案中   registered 已备案  freeze 冻结  
        */
        @NotBlank(message = "备案状态 draft 暂存  registering 备案中   registered 已备案  freeze 冻结  不能为空")
        @Size(max = 32,message = "备案状态 draft 暂存  registering 备案中   registered 已备案  freeze 冻结  最大长度不能超过32位")
        private String status;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 64,message = "产品名称最大长度不能超过64位")
        private String productName;

        /**
        * 产品英文名称
        */
        @NotBlank(message = "产品英文名称不能为空")
        @Size(max = 64,message = "产品英文名称最大长度不能超过64位")
        private String productNameEn;

        /**
        * 报关单位
        */
        @NotBlank(message = "报关单位不能为空")
        @Size(max = 32,message = "报关单位最大长度不能超过32位")
        private String declareUnit;

        /**
        * 商品id
        */
        @NotBlank(message = "商品id不能为空")
        @Size(max = 32,message = "商品id最大长度不能超过32位")
        private String goodId;

        /**
        * 产品型号
        */
        @NotBlank(message = "产品型号不能为空")
        @Size(max = 64,message = "产品型号最大长度不能超过64位")
        private String spu;

        /**
        * 申报币种
        */
        @NotBlank(message = "申报币种不能为空")
        @Size(max = 255,message = "申报币种最大长度不能超过255位")
        private String currency;

        /**
        * 申报价格
        */
        private BigDecimal declarePrice;

        /**
        * 毛重(g)
        */
        private BigDecimal grossWeight;

        /**
        * 长
        */
        private BigDecimal length;

        /**
        * 宽
        */
        private BigDecimal width;

        /**
        * 高
        */
        private BigDecimal height;

        /**
        * 是否带电
        */
        @NotNull(message = "是否带电不能为空")
        private Boolean isBattery;

        /**
        * 电池类型
        */
        @NotBlank(message = "电池类型不能为空")
        @Size(max = 32,message = "电池类型最大长度不能超过32位")
        private String batteryType;

        /**
        * 报关中文名
        */
        @NotBlank(message = "报关中文名不能为空")
        @Size(max = 255,message = "报关中文名最大长度不能超过255位")
        private String declareNameCn;

        /**
        * 海关编码
        */
        @NotBlank(message = "海关编码不能为空")
        @Size(max = 255,message = "海关编码最大长度不能超过255位")
        private String customsCode;

        /**
        * 第一数量
        */
        private BigDecimal firstNumber;

        /**
        * 第二数量
        */
        private BigDecimal secondNumber;

        /**
        * 申报要素
        */
        @NotBlank(message = "申报要素不能为空")
        @Size(max = 255,message = "申报要素最大长度不能超过255位")
        private String declareElement;

        /**
        * 供应商代码
        */
        @NotBlank(message = "供应商代码不能为空")
        @Size(max = 255,message = "供应商代码最大长度不能超过255位")
        private String supplierCode;

        /**
        * 条码类型
        */
        @NotBlank(message = "条码类型不能为空")
        @Size(max = 16,message = "条码类型最大长度不能超过16位")
        private String barcodeType;

        /**
        * 自定义条码
        */
        @NotBlank(message = "自定义条码不能为空")
        @Size(max = 255,message = "自定义条码最大长度不能超过255位")
        private String customBarcode;

        /**
        * 是否带发票
        */
        @NotNull(message = "是否带发票不能为空")
        private Boolean isInvoice;

        /**
        * 是否零件类
        */
        @NotNull(message = "是否零件类不能为空")
        private Boolean isParts;

        /**
        * 电池二级分类
        */
        private String batteryNote;

        /**
        * 图片url
        */
        private String url;


    }


}