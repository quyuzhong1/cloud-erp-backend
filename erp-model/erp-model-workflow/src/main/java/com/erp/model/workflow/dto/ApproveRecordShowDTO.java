package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ApproveRecordVO
 * @Description TODO
 * @Date 2022-08-18 11:17
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ApproveRecordShowDTO implements Serializable {

    //节点名称
    private String activityName;

    //节点类型
    private String activityType;

    //开始时间
    private String startTime;

    //结束时间
    private String endTime;

    //处理多长时间
    private String handleTime;


    //处理人
    private String handleUserName;


    //意见
    private String comment;

}
