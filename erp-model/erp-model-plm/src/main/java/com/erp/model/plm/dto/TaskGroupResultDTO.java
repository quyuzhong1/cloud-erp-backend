package com.erp.model.plm.dto;

import lombok.Data;

/**
 *  任务分组 返回结果
 * @Classname TaskGroupDTO
 * @Description TODO
 * @Date 2022-11-08 10:03
 * @Created by yl
 */
@Data
public class TaskGroupResultDTO {

    /**
     * 名字
     */
    private String name;


    /**
     * 任务数
     */
    private Integer taskCount;

    /**
     * 分组后的标示
     * 产品id 或者日期
     */
    private String groupFlag;
}
