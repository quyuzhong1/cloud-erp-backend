package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
* @Description 查询无规格明细信息实体类（VO）
* @Author Luo_WG
* @Date 2022/9/22 14:46
**/
@Data
@NoArgsConstructor
public class ProductNoDetailDTO {
    /**
     * 产品信息列表id
     */
    private String id;
    /**
     * spu
     */
    private String spuNo;

    /**
     * 产品图片
     */
    private String imagesUrl;

    /**
     * skuNo
     */
    private String skuNo;
    /**
     * skuId
     */
    private String skuId;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    private String productState;

    /**
     * SKU名称
     */
    private String name;

    /**
     * SKU名称(英文)
     */
    private String nameEn;
    /**
     * SPU名称
     */
    private String spuName;

    /**
     * SPU英文名称
     */
    private String spuNameEn;

    /**
     * 产品经理
     */
    private String chargeName;

    /**
     * 产品经理Id
     */
    private String chargeId;

    /**
     * 品牌
     */
    private String brandName;

    /**
     * 品牌id
     */
    private String brandId;

    /**
     * 产品属性
     */
    private String property;

    /**
     * 产品属性Id
     */
    private String propertyId;

    /**
     * 产品等级id
     */
    private String gradeId;

    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品类别
     */
    private String category;

    /**
     * 产品类别Id
     */
    private String categoryId;

    /**
     * 产品类型，ProductTypeEnum枚举，/plm/dict/list?type=productType
     */
    private String type;

    /**
     * 迭代产品关联skuid
     */
    private String iterateRefSkuId;

    /**
     * 迭代产品关联skuNo
     */
    private String iterateRefSkuNo;

    /**
     * 销售方式
     */
    private String saleMethod;

    /**
     * 产品卖点
     */
    private String sellSpot;

    /**
     * 产品功能描述
     */
    private String functionDesc;

    /**
     * 产品用途
     */
    private String usageDesc;

    /**
     * 存在侵权风险 1：有侵权风险 2：无侵权风险
     */
    private String pirateRisk;

    /**
     * 主要材质
     */
    private String materials;

    /**
     * 计划上市时间
     */

    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private String planListingTime;

    /**
     * 首批量产入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String firstMassProductDate;

    /**
     * 单位表id
     */
    private String unitId;

    /**
     * 单位表名称
     */
    private String unitName;

    /**
     * 委托开发成本
     */
    private BigDecimal entrustedDevelopCost;

    /**
     * 模具成本
     */
    private BigDecimal moldCost;

    /**
     * 样品费用
     */
    private BigDecimal sampleFee;

    /**
     * 是否客户定制(0否，1是)
     */
    private Integer isCustomized;

    /**
     * 销售渠道
     */
    private String salesChannel;

    /**
     * 产品分类id集合
     */
    private List<String> categoryIdList;
}
