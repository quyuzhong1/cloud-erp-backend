package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname MemberPagingShowDTO
 * @Description TODO
 * @Date 2022-09-26 18:35
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class MemberPagingShowDTO implements Serializable {

    //id
    private String id;
    //角色名
    private String roleName;

    //角色名
    private String roleId;

    private String memberName;

    private String memberId;

    //总任务数
    private Integer totalTaskCount;

    //完成任务数
    private Integer finishTaskCount;

    //进行中任务数
    private Integer ingTaskCount;

    //逾期任务数
    private Integer postponeTaskCount;


    private Date createTime;

    //是否负责人 0 不是 1 是
    private Integer isCharge;
}
