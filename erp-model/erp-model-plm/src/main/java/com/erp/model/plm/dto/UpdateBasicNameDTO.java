package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * @Classname UpdateBasicCategoryDTO
 * @Description TODO
 * @Date 2022-09-13 14:03
 * @Created by yl
 */
@Data
@NoArgsConstructor
@Validated
public class UpdateBasicNameDTO {


    private String id;

    @NotBlank(message = "名称不能为空")
    private String name;
}
