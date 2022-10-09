package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Classname ProjectInfoDTO
 * @Description TODO
 * @Date 2022-09-19 11:52
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProjectInfoDTO  implements Serializable {


    /**
     * 总任务数
     */
    private Integer  totalTaskCount;

    /**
     * 产品名
     */
    private String productName;



    /**
     * 完成任务数
     */
    private Integer finishTaskCount;


    /**
     * 完成率
     */
    private Integer finishRatio;


    /**
     * 未完成的任务数
     */
    private Integer unfinishedTaskCount;



    /**
     * 延期的任务数
     */
    private Integer postponeTaskCount;



    /**
     * 延期率
     */
    private Integer postponeRatio;

    /**
     * 阶段分布
     */
    private PhaseDistributeDTO  phaseDistribute;


    /**
     * 完成趋势
     */
    private List<Map<String,Object>> finishTaskTrend;
}
