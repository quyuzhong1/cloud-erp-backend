package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
    private String productPropertyId;

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

    private static final long serialVersionUID = 1L;
}