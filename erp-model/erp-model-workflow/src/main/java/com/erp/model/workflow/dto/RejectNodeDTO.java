package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname RejectNodeDTO
 * @Description TODO
 * @Date 2022-08-17 11:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class RejectNodeDTO extends ProcessBaseDTO {

    //节点id
    private String activityId;
}
