package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
     * sku
     */
    @NotBlank(message = "sku不能为空")
    private String skuNo;


    private String skuId;


    private String productId;

    /**
     * 层级
     */
    private Integer level;



    @Size(min = 1,message = "至少需要一个子物料")
    @Valid
    private List<BomChildrenSkuDTO> children;
}
