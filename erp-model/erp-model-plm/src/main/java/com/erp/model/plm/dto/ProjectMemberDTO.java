package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ProjectMemberDTO
 * @Description TODO
 * @Date 2022-09-19 11:33
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProjectMemberDTO implements Serializable {

    /**
     * 成员id
     */
    private String memberId;

    /**
     * 成员名
     */
    private String memberName;

    /**
     * 任务数
     */
    private Long taskCount;


}
