package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

/**
 * @Classname SysUserPagingSearchDTO
 * @Description TODO
 * @Date 2022-07-14 10:19
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserPagingSearchDTO   {

    //角色id
    private List<String> roleIds;

    //搜素类型 mobile，real_name，user_name
    @NotBlank(message = "探索类型不能为空")
 //   @Pattern(regexp = "^[mobile realName userName]$", message = "搜素类型有误")
    private String searchType;

    private String searchKeyword;

    //开始时间
    private Date startTime;

    //结束时间
    private Date endTime;

    //状态 1 正常  0 不正常
    private Integer state;
}
