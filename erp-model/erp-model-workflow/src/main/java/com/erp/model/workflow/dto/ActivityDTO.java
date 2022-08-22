package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ActivityDTO
 * @Description TODO
 * @Date 2022-08-12 11:34
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ActivityDTO implements Serializable {

    //当前的活动id
    private String  nowActivityId;

    //流程id
    private String processInstanceId;
}
