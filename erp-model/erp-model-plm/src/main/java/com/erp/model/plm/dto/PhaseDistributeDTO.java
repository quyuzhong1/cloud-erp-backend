package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Classname PhaseDistributeDTO
 * @Description TODO
 * @Date 2022-09-19 12:16
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PhaseDistributeDTO  implements Serializable {


    /**
     * 状态列表
     */
    private List<Map<String,Object>> statusList;



    /**
     * 任务阶段
     */
    private List<Map<String,List<Map<String,Object>>>> phaseList;
}
