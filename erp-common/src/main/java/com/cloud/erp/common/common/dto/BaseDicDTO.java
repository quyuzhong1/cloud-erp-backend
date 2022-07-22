package com.cloud.erp.common.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname BaseDicDTO
 * @Description TODO
 * @Date 2022-07-20 17:11
 * @Created by yl
 */
@Data
public class BaseDicDTO implements Serializable {

    @NotBlank(message = "类型不能为空")
    private String dicType;

    private String searchKeyword;
}
