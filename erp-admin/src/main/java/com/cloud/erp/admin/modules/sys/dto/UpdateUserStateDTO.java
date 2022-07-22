package com.cloud.erp.admin.modules.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname UpdateUserStateDTO
 * @Description TODO
 * @Date 2022-07-15 12:07
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdateUserStateDTO  implements Serializable {

    @NotNull(message = "id不能为空")
    private List<String> ids;


    //@Pattern(regexp = "^[01]$", message = "状态有误")
    private Integer state;
}
