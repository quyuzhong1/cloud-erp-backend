package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description 产品物流信息表
 * @Author Luo_WG
 * @Date 2022/9/23 15:06
 **/
@Data
@NoArgsConstructor
public class ProductLogisticsShowDTO implements Serializable {

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
     * 产品属性
     */
    private String productProperty;

    /**
     * 产品属性id
     */
    private String productPropertyId;

    /**
     * 报关型号
     */
    private String declareModel;

    /**
     * 报关中文名
     */
    private String declareChineseName;

    /**
     * 报关英文名
     */
    private String declareEnglishName;

    /**
     * 报关申报价格
     */
    private BigDecimal declarePrice;

    /**
     * 海关编码
     */
    private String customsCode;

    /**
     * 申报单位
     */
    private String declareUnit;

    /**
     * 申报要素
     */
    private String declareElement;

    /**
     * 英文材质
     */
    private String englishMaterial;

    /**
     * 英文用途
     */
    private String englishUsage;
    /**
     * 判断是否含电 true 展示输入输出电池电压功率  false不展示入输出电池电压功率
     */
    private Boolean electric;

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

    private static final long serialVersionUID = 1L;
}