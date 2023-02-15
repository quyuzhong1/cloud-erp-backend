package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname FindCustomizeFieldDTO
 * @Description TODO
 * @Date 2023-02-08 16:22
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FindCustomizeFieldDTO implements Serializable {



    private String userId;

    @NotBlank(message = "模块code 不能为空")
    private String moduleCode;
}
