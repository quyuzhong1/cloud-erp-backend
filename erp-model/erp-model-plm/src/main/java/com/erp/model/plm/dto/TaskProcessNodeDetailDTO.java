package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname TaskProcessNodeDTO
 * @Description TODO
 * @Date 2022-10-20 14:38
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskProcessNodeDetailDTO implements Serializable {

    /**
     *  是否达到该节点
     */
    private Boolean  ifFinishNode;

    /**
     * 开始时间
     */
    private String startDate;

    /**
     * 节点明细信息
     */
    private List<TaskProcessNodeDTO> list;

}
