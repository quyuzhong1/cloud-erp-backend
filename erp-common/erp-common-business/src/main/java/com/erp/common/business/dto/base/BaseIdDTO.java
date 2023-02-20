package com.erp.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Classname BaseIdDTO
 * @Description TODO
 * @Date 2022-09-20 11:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BaseIdDTO   extends  PermissionsDTO  {

    @NotBlank(message = "id不能为空")
    private String id;

    private String name;
}
