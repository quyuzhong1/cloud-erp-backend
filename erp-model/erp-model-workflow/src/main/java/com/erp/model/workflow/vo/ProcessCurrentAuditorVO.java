package com.erp.model.workflow.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 *  当前审核人
 * @Classname
 * @Description TODO
 * @Date 2023-02-08 19:57
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProcessCurrentAuditorVO implements Serializable {

    /**
     * 流程id
     */
    private String processId;

    /**
     * 具体业务表id
     */
    private String businessTableId;



    private List<String> handleUserIdList;
}
