package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname 任务处理情况
 * @Description TODO
 * @Date 2022-09-27 9:30
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskConductDTO  implements Serializable {

    private String membersId;
     //总的任务数
    private Integer totalTaskCount;

    //完成的任务数
    private Integer finishTaskCount;

    //完成的任务数
    private Integer ingTaskCount;

    //延期
    private Integer postponeTaskCount;

}
