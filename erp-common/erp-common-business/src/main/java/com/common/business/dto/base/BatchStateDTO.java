package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
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

    @Min(value = 0,message = "状态码有误 只能是0或者1")
    @Max(value = 1,message = "状态码有误 只能是0或者1")
    private Integer state;
}
