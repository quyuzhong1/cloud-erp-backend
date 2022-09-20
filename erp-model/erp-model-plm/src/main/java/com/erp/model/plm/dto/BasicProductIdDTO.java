package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Classname BasicProductIdDTO
 * @Description TODO
 * @Date 2022-09-13 17:37
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BasicProductIdDTO {

    @NotBlank(message = "产品id不能为空")
    private String productId;
}
