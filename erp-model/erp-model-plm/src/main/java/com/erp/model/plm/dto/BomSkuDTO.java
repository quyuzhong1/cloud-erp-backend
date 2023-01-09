package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String parentSkuNo="0";
    /**
     * sku
     */
    private String skuNo;

    /**
     * 数量
     */
    private Integer quantity;


    private List<BomSkuDTO> children;
}
