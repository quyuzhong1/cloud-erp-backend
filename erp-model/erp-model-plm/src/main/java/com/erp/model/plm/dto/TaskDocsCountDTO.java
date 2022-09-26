package com.erp.model.plm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Classname 任务文档统计
 * @Description TODO
 * @Date 2022-09-22 9:55
 * @Created by yl
 */
@Data
public class TaskDocsCountDTO  implements Serializable {


    //任务id
    private String taskId;

    //数量
    private Integer count;
}
