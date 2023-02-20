package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Classname StateDTO
 * @Description TODO
 * @Date 2022-09-15 10:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class StateDTO {


    /**
     * 表id
     */
    @NotBlank(message = "id 不能为空")
    private String id;

    /**
     * 模板状态
     */
    @NotNull(message = "模板状态 不能为空")
    @StateEnumValue(intValues = {0, 1}, message = "模板状态只能是0或者1")
    private Integer state;
}
