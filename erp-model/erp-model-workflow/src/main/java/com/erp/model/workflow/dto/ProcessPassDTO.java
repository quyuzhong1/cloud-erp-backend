package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 流程通过后会 得到的信息
 * @Classname
 * @Description TODO
 * @Date 2023-01-30 16:35
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProcessPassDTO implements Serializable {

    /**
     * 流程id
     */
    private String processId;

    /**
     * 具体业务表id
     */
    private String businessTableId;
}
