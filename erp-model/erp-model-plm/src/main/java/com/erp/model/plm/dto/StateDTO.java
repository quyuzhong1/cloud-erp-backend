package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Classname StateDTO
 * @Description TODO
 * @Date 2022-09-15 10:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class StateDTO {


    @NotBlank(message = "id 不能为空")
    private String id;

    @StateEnumValue(intValues = {0, 1}, message = "账户状态只能是0或者1")
    private Integer state;
}
