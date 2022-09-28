package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
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

    @NotBlank(message = "产品id不能为空")
    private String productId;

    @NotBlank(message = "系统字段表id不能为空")
    private String sysFieldId;
}
