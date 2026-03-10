package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description 产品销售信息表请求参数
 * @Author Luo_WG
 * @Date 2022/9/23 12:14
 **/
@Data
@NoArgsConstructor
public class ProductSaleShowDTO implements Serializable {

    /**
     * 主键id 无id：新增 有id：修改
     */
    private String id;

    /**
     * 产品sku表id
     */
    private String skuId;

    /**
     * 产品sku图片
     */
    private String imagesUrl;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 年目标销售量
     */
    private Long yearSaleQty;

    /**
     * 年目标销售额
     */
    private BigDecimal yearSaleAmount;

    /**
     * 月目标销售量
     */
    private Long monthSaleQty;

    /**
     * 月目标销售额
     */
    private BigDecimal monthSaleAmount;

    /**
     * 销售国家
     */
    private String saleCountry;

    /**
     * 销售国家名称
     */
    private String saleCountryName;

    /**
     * 上市时间
     */
    private LocalDate listingTime;

    /**
     * 退市时间
     */
    private LocalDate delistingTime;

    /**
     * 图片是否完成 1.是 2.否
     */
    @StateEnumValue(intValues = {1, 2}, message = "图片是否完成1或者2")
    private Integer isFinishedImg;

    /**
     * 视频是否完成 1.是 2.否
     */
    private Integer isFinishedVideo;

    /**
     * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
     */
    private Integer saleState;

    /**
     * 产品上市（含培训）资料链接
     */
    private String dataUrl;

    /**
     * 首季度目标销量
     */
    private BigDecimal targetSalesQty;

    /**
     * 销售平台(ProductSalesPlatformEnum枚举)
     */
    private String salesPlatform;

    /**
     * 是否可销售(0否，1是)
     */
    private Integer isMarketable;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * 修改人名称
     */
    private String updateUserName;


    /**
     *禁止修改的字段
     */
    private List<String> disableFieldList;
    /**
     * 产品属性
     */
    private String productProperty;

    /**
     * 产品属性id
     */
    private String productPropertyId;

    /**
     * 保险属性
     */
    private List<String> insurancePropertyList;
    private List<String> insurancePropertyNameList;
    private String insuranceProperty;
    /**
     * 判断是否含电 true 展示输入输出电池电压功率  false不展示入输出电池电压功率
     */
    private boolean electric;
    /**
     * 输入参数 仅展示使用
     */
    private String inputParams;
    /**
     * 输出参数 仅展示使用
     */
    private String outputParams;
    /**
     * 输入电压
     */
    private BigDecimal inputVoltage;
    /**
     * 输出电压
     */
    private BigDecimal outputVoltage;

    /**
     * 电压单位  dict type=voltageUnit
     */
    private String voltageUnit;

    /**
     * 输入电流
     */
    private BigDecimal inputElectric;
    /**
     * 输出电流
     */
    private BigDecimal outputElectric;

    /**
     * 电流单位 dict type=electricUnit
     */
    private String electricUnit;

    /**
     * 输入功率
     */
    private BigDecimal inputPower;
    /**
     * 输出功率
     */
    private BigDecimal outputPower;

    /**
     * 功率单位 dict type=powerUnit
     */
    private String powerUnit;

    /**
     * 输入电池容量
     */
    private BigDecimal inputBatteryCapacity;
    /**
     * 输出电池容量
     */
    private BigDecimal outputBatteryCapacity;

    /**
     * 电池容量单位 dict type=batteryCapacityUnit
     */
    private String batteryCapacityUnit;

    /**
     * 电池重量（克）
     */
    private BigDecimal batteryWeight;

    private static final long serialVersionUID = 1L;
}