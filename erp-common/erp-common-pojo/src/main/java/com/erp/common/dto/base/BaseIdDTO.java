package com.erp.common.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname BaseIdDTO
 * @Description TODO
 * @Date 2022-09-20 11:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BaseIdDTO implements Serializable {

    @NotBlank(message = "id不能为空")
    private String id;
}
