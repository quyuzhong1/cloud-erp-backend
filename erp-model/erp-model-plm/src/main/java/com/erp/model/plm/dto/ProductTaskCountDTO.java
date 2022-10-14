package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 产品任务数据统计
 * @Classname TaskCountDTO
 * @Description TODO
 * @Date 2022-10-13 16:38
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductTaskCountDTO {

    /**
     * 总任务数
     */
    private Integer  totalTaskCount;

    /**
     * 完成任务数
     */
    private Integer finishTaskCount;


    /**
     * 未完成的任务数
     */
    private Integer unfinishedTaskCount;

    /**
     * 延期的任务数
     */
    private Integer postponeTaskCount;
}
