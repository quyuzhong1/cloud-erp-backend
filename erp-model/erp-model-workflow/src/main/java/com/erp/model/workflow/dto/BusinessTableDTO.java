package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 根据用户id和业务表id 查询到 业务流程信息
 * @Classname
 * @Description TODO
 * @Date 2023-02-01 9:19
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BusinessTableDTO implements Serializable {


    /**
     * 用户id
     */
    private String userId;


    /**
     * 具体业务表id
     */
    private String businessTableId;
}
