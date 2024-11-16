package com.erp.model.plm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Administrator
 * @Classname migrateTempVO

 * @Date 2023-03-07 19:41
 * @Created by yl
 */
@Data
public class MigrateTempVO implements Serializable {


    /**
     * 新产生的id
     */
    private String newCreateId;

    private String  name;


    /**
     * 任务id
     */
    private String taskId;

    private String templateId;
}