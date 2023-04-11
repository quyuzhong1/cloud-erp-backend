package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname BatchStateDTO
 * @Description TODO
 * @Date 2022-07-29 9:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BatchStateDTO implements Serializable {


    @NotEmpty(message = "ids集合不能为空")
    private List<String> ids;

    /**
     * true 禁用
     * false 启用
     */
    @NotNull(message = "状态不能为空")
    private Boolean state;
}
