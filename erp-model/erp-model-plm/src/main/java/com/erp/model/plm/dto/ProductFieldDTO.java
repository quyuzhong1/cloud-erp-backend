package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname ProductFieldDTO
 * @Description TODO
 * @Date 2022-09-27 12:11
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductFieldDTO  implements Serializable {

    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;

    /**
     * 系统表字段id 不能为空
     */
    @NotBlank(message = "系统字段表id不能为空")
    private String sysFieldId;

    /** 是否必填 1：是 0 不是

     */
    @NotNull(message = "是否必填不能为空")
    private Integer ifRequired;
}
