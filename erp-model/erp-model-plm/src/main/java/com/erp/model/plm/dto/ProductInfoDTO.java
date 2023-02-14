package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * @Description 产品基础信息请求参数
 * @Author Luo_WG
 * @Date 2022/9/23 17:26
 **/
@Data
@NoArgsConstructor
public class ProductInfoDTO {
    /**
     * 主键id
     */
    private String id;

    /**
     * 产品名
     */
    @Size(max = 50,message = "产品名最大50字符")
    private String name;

    /**
     * 产品名称（英文）
     */
    @Size(max = 200,message = "产品名称（英文）最大200字符")
    private String nameEn;

    /**
     * 产品类别
     */
    private String category;

    /**
     * 产品属性
     */
    private String property;

    /**
     * 产品属性id
     */
    private String propertyId;

    /**
     * 产品负责人
     */
    private String chargeName;

    /**
     * 负责人id
     */
    private String chargeId;

    /**
     * 产品等级id
     */
    private String gradeId;

    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品品牌
     */
    private String brandName;

    /**
     * 品牌id
     */
    private String brandId;

    /**
     * 分类id
     */
    private String categoryId;

    /**
     * spuNo
     */
    @Size(max = 50,message = "SPU最大50字符")
    private String spuNo;

    /**
     * 产品卖点
     */
    @Size(max = 500,message = "产品卖点最大500字符")
    private String sellSpot;

    /**
     * 产品功能描述
     */
    @Size(max = 500,message = "产品功能描述最大500字符")
    private String functionDesc;

    /**
     * 产品用途
     */
    @Size(max = 500,message = "产品用途最大500字符")
    private String usageDesc;

    /**
     * 存在侵权风险 1：有侵权风险 2：无侵权风险
     */
    private Integer pirateRisk;

    /**
     * 主要材质
     */
    @Size(max = 500,message = "主要材质最大500字符")
    private String materials;

    /**
     * 规格类型  1：无规格  2：多规格
     */
    private Integer specType;

    /**
     * 销售方式
     */
    private String saleMethod;

    private Integer approvalStatus;

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
     * 销售渠道
     */
    @NotBlank(message = "销售渠道不能为空")
    private String salesChannel;

    /**
     * 是否客户定制(0否，1是)
     */
    @NotNull(message = "是否客户定制不能为空")
    @StateEnumValue(intValues = {0, 1}, message = "是否客户定制值错误")
    private Integer isCustomized;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * 修改人名称
     */
    private String updateUserName;
}