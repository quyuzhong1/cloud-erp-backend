package com.erp.model.plm.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Administrator
 * @Classname migrateTempVO
 * @Description TODO
 * @Date 2023-03-07 19:41
 * @Created by yl
 */
@Data
public class migrateTempVO implements Serializable {


    /**
     * 新产生的id
     */
    private String newCreateId;


    /**
     * 任务id
     */
    private String taskId;

    private String templateId;
}
