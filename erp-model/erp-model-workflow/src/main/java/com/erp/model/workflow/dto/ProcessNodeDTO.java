package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ProcessNodeDTO
 * @Description TODO
 * @Date 2022-10-17 10:39
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProcessNodeDTO implements Serializable {


    /**
     * 流程 id
     */
    private String processId;


    /**
     * 当前 节点 id
     */
    private String currentNodeId;
}
