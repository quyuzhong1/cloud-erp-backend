package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ProductProjectDTO
 * @Description TODO
 * @Date 2022-10-13 17:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductProjectDTO implements Serializable {

    /**
     * 产品id
     */
    private String productId;

    /**
     * 产品名
     */
    private String productName;

    /**
     * 产品id
     */
    private String projectId;
}
