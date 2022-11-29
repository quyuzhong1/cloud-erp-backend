package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/28 14:47
 */
@Data
@NoArgsConstructor
public class ProductDetailOperateDTO implements Serializable {

    /**
     * 主键id
     */
    @NotBlank(message = "产品信息id不能为空")
    private String id;

    /**
     * 意见
     */
    private String comment;
}
