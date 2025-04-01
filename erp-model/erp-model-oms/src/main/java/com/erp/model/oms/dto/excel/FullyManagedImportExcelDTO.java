package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.oms.enums.SoB2cExtendOrderSourceTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 全手工导入ExcelDTO
 *
 * @author qiuzhiq
 * @date 2024/01/10 10:41:51
 */
@Data
@NoArgsConstructor
public class FullyManagedImportExcelDTO {


    /**
     * 序号
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "序号", isNotBlank = true )
    private String index;

    /**
     * 平台订单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*平台订单号", index = 1)
    @FieldValid(fieldName = "平台订单号", isNotBlank = true)
    private String platformCode;

    /**
     * 平台
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*平台", index = 2)
    @FieldValid(fieldName = "平台", isNotBlank = true)
    private String dictPlatformName;
    @ExcelIgnore
    private String dictPlatform;

    /**
     * 店铺
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*店铺", index = 3)
    @FieldValid(fieldName = "店铺", isNotBlank = true)
    private String shopName;
    @ExcelIgnore
    private String shopId;

    /**
     * 订单金额
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*订单金额", index = 4)
    @FieldValid(fieldName = "订单金额",isNotBlank = true,formatPattern=FieldFormatPatternTypeEnum.AMOUNT)
    private BigDecimal amount;

    /**
     * 币别
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*币别", index = 5)
    @FieldValid(fieldName = "币别",isNotBlank = true,enumClass = CurrencyEnum.class)
    private String currencyCode;
    @ExcelIgnore
    private String currency;

    /**
     * 下单时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*下单时间", index = 6)
    @FieldValid(fieldName = "下单时间",isNotBlank = true)
    private String payTimeStr;
    @ExcelIgnore
    private LocalDateTime payTime;

    /**
     * 订单来源
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*订单来源", index = 7)
    @FieldValid(fieldName = "订单来源",isNotBlank = true,enumClass = SoB2cExtendOrderSourceTypeEnum.class)
    private String orderSourceTypeName;
    @ExcelIgnore
    private String orderSourceType;


    /**
     * 订单分类
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "订单分类", index = 8)
    @FieldValid(fieldName = "订单分类")
    private String categoryNameList;
    @ExcelIgnore
    private List<String> categoryIdList;


    /**
     * 订单备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "订单备注", index = 9)
    @FieldValid(fieldName = "订单备注")
    private String remark;

    /**
     * 物流渠道
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "物流渠道", index = 10)
    @FieldValid(fieldName = "物流渠道")
    private String channelName;
    @ExcelIgnore
    private String channelId;

    /**
     * 跟踪号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "跟踪号", index = 11)
    @FieldValid(fieldName = "跟踪号")
    private String trackNo;
    /**
     * 包装尺寸(长*宽*高)
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "包装尺寸(长*宽*高)", index = 12)
    @FieldValid(fieldName = "包装尺寸(长*宽*高)")
    private String packageSize;
    @ExcelIgnore
    private BigDecimal length;
    @ExcelIgnore
    private BigDecimal width;
    @ExcelIgnore
    private BigDecimal height;


    /**
     * 实际运费
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "实际运费", index = 13)
    @FieldValid(fieldName = "实际运费",formatPattern=FieldFormatPatternTypeEnum.AMOUNT)
    private BigDecimal actualShippingCost;


    /**
     * 要求发货时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "要求发货时间", index = 14)
    @FieldValid(fieldName = "要求发货时间")
    private String requiredDeliveryTimeStr;

    @ExcelIgnore
    private LocalDateTime requiredDeliveryTime;


    /**
     * 要求收货时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "要求收货时间", index = 15)
    @FieldValid(fieldName = "要求收货时间")
    private String requiredReceiveTimeStr;
    @ExcelIgnore
    private LocalDateTime requiredReceiveTime;


    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 16)
    @FieldValid(fieldName = "SKU",isNotBlank = true)
    private String skuNo;
    @ExcelIgnore
    private String skuId;


    /**
     * 下单数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*下单数量", index = 17)
    @FieldValid(fieldName = "下单数量",isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.INTEGER)
    private String qty;


    /**
     * 发货仓库
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "发货仓库", index = 18)
    @FieldValid(fieldName = "发货仓库")
    private String deliveryWarehouseName;
    @ExcelIgnore
    private String deliveryWarehouseId;


    /**
     * 真实售价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "真实售价", index = 19)
    @FieldValid(fieldName = "真实售价",isNotBlank = true,formatPattern=FieldFormatPatternTypeEnum.AMOUNT)
    private BigDecimal price;


    /**
     * 税率%
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "税率", index = 20)
    @FieldValid(fieldName = "税率")
    private BigDecimal taxRate;

    /**
     * 错误信息
     */
    @ColumnWidth(100)
    @ExcelProperty(value = "错误数据", index = 21)
    private String errorMsg;














}
