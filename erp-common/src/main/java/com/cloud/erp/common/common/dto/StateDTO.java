package com.cloud.erp.common.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname StateDTO
 * @Description TODO
 * @Date 2022-07-12 10:22
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class StateDTO implements Serializable {


    @NotBlank(message = "id不能为空")
    private String id;


    //@Pattern(regexp = "^[01]$", message = "状态有误")
    private Integer state;
}
