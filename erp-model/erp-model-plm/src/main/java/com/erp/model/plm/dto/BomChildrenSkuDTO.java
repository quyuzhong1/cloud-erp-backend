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
 * @Description TODO
 * @Date 2023-02-14 19:36
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomChildrenSkuDTO  implements Serializable {

    /**
     * 父级skuId
     */
    private String parentSkuId;

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
