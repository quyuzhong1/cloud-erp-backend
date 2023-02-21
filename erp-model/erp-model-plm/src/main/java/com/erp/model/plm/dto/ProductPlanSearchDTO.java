package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 9:53
 */
@Data
@NoArgsConstructor
public class ProductPlanSearchDTO implements Serializable {

    /**
     * 产品名称
     */
    private String name;

    /**
     * 类型：尚未开始，已立项，开发中，延期
     */
    private String type;
}
