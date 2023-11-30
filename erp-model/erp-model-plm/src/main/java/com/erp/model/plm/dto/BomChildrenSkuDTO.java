package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname BomChildrenSkuDTO

 * @Date 2023-02-14 19:36
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomChildrenSkuDTO  implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * bomId
     */
    private String bomId;


    /**
     * 类型combination 组合 single 单品
     */
    private String type;

    /**
     * bom 编号
     */
    private String serialNumber;

    /**
     * bom历史id
     */
    private String bomHistoryId;
    /**
     * bom版本
     */
    private String bomVersion;

    /**
     * 父级skuId
     */
    private String parentSkuId;

    /**
     * 父级skuNo
     */
    private String parentSkuNo;

    /**
     * sku
     */
    @NotBlank(message = "sku不能为空")
    private String skuNo;


    private String skuId;


    private String productId;

    /**
     * sku名称
     */
    private String skuName;

    /**
     * 单位
     */
    private String unitName;

    /**
     * 层级
     */
    private Integer level;


    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @DecimalMax(value = "9999",message ="最大值为9999" )
    @DecimalMin(value = "1",message ="最小值为1" )
    private Integer quantity;

}
