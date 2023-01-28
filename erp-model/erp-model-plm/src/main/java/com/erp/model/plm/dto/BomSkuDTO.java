package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * bom 的sku
 *
 * @Classname
 * @Description TODO
 * @Date 2023-01-09 12:10
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomSkuDTO implements Serializable {


    /**
     * 父级 sku
     */
    private String parentSkuNo = "0";
    /**
     * sku
     */
    @NotBlank(message = "sku不能为空")
    private String skuNo;


    private String skuId;

    /**
     * 层级
     */
    private String level;


    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    private Integer quantity;


    private List<BomSkuDTO> children;
}
