package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @Classname BasicProductIdDTO
 * @Description TODO
 * @Date 2022-09-13 17:37
 * @Created by yl
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BasicProductIdDTO {

    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;
}
