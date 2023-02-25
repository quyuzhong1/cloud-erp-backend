package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname SetSchematicImageUrlDTO
 * @Description TODO
 * @Date 2023-02-25 12:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SetSchematicImageUrlDTO implements Serializable {

    @NotBlank(message = "产品图片不能为空")
    private String imageUrl;


    @NotBlank(message = "产品id不能为空")
    private String productId;
}
