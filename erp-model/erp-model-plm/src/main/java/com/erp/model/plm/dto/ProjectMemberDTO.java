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

    private String memberId;

    private String memberName;
}
