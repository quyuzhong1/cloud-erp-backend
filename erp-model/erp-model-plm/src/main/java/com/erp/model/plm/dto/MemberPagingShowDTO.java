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


    /**
     * 项目成员表id
     */
    private String id;
    /**
     * 角色成员关系表id
     */
    private String roleRefMemberId;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 角色id
     */
    private String roleId;

    /**
     * 成员名
     */
    private String memberName;

    /**
     * 成员id
     */
    private String memberId;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 总任务数
     */
    private Integer totalTaskCount;


    /**
     * 完成任务数
     */
    private Integer finishTaskCount;


    /**
     * 进行中任务数
     */
    private Integer ingTaskCount;



    /**
     * 逾期任务数
     */
    private Integer postponeTaskCount;






    /**
     * 是否负责人 0 不是 1 是
     */
    private Integer isCharge;
}
