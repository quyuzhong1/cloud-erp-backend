package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname UpdateStateDTO
 * @Description TODO
 * @Date 2022-11-07 14:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdateStateDTO  implements Serializable {

    @NotBlank(message = "id不能为空")
    private String id;


    /**
     * true 打开
     * false 关闭
     */
    @NotNull(message = "状态值不能为空")
    private Boolean state;
}
