package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description 产品物流信息表
 * @Author Luo_WG
 * @Date 2022/9/23 15:06
 **/
@Data
@NoArgsConstructor
public class ProductLogisticsDTO implements Serializable {

    /**
     * 主键id 无id：新增 有id：修改
     */
    private String id;

    /**
     * sku表id
     */
    private String skuId;

    /**
     * 产品属性
     */
    private String productProperty;

    /**
     * 产品属性id
     */
//    @NotBlank(message = "产品物流属性ID不能为空")
    private String productPropertyId;

    /**
     * 报关型号
     */
    @Size(max = 50,message = "报关型号最大100字符")
    private String declareModel;

    /**
     * 报关中文名
     */
    @Size(max = 200,message = "报关中文名最大200字符")
    private String declareChineseName;

    /**
     * 报关英文名
     */
    @Size(max = 200,message = "报关英文名最大200字符")
    private String declareEnglishName;

    /**
     * 报关申报价格
     */
    @Digits(integer = 16,fraction = 4,message = "报关申报价格最大16字符，小数位不能大于4位")
    private BigDecimal declarePrice;

    /**
     * 海关编码
     */
    @Size(max = 50,message = "海关编码不能大于50字符")
    private String customsCode;

    /**
     * 报关单位
     */
    @Size(max = 200,message = "报关单位最大200字符")
    private String declareUnit;

    /**
     * 申报要素
     */
    @Size(max = 200,message = "申报要素最大200字符")
    private String declareElement;

    /**
     * 英文材质
     */
    @Size(max = 200,message = "英文材质最大200字符")
    private String englishMaterial;

    /**
     * 英文用途
     */
    @Size(max = 200,message = "英文用途最大200字符")
    private String englishUsage;

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

    private static final long serialVersionUID = 1L;
}