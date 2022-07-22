package com.cloud.erp.admin.modules.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Classname SysPostDTO
 * @Description TODO
 * @Date 2022-07-12 16:22
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysPostDTO {

    private String id;

    @NotBlank(message = "岗位名不能为空")
    private String postName;

    private String postRemark;
}
