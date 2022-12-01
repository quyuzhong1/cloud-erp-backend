package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname UpdateUserNoticeStateDTO
 * @Description TODO
 * @Date 2022-11-14 10:57
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdateUserNoticeStateDTO implements Serializable {

    @NotBlank(message = "id不能为空")
    private String id;


    /**
     * true 打开
     * false 关闭
     */
    @NotNull(message = "状态值不能为空")
    private Boolean state;

    /**
     *unionId
     */
    @NotBlank(message = "unionId不能为空")
    private String unionId;
}
