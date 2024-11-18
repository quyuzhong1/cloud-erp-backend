package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**   查询发起流程 列表
 * @Classname

 * @Date 2022-08-18 9:23
 * @Created by yl
 */
@Data
@Slf4j
public class QueryProcessDTO implements Serializable {

    //流程状态
    private Integer processState;


    //用户id
    private String userId;

}
