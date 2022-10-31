package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Set;

/**
 * @Classname BatachSavePostUserDTO
 * @Description TODO
 * @Date 2022-07-29 12:26
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BatchSavePostUserDTO implements Serializable {
    //部门id
    @NotBlank(message = "岗位id 不能为空")
    private String postId;

    //用户id集合
    @NotEmpty(message = "用户id集合 不能为空")
    private Set<String> userIds;

}
