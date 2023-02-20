package com.erp.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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


    @Min(value = 0,message = "状态码有误 只能是0或者1")
    @Max(value = 1,message = "状态码有误 只能是0或者1")
    private Integer state;
}
