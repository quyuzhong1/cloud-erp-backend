package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname TaskProcessNodeDTO
 * @Description TODO
 * @Date 2022-10-20 14:38
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskProcessNodeDTO  implements Serializable {

    /**
     * 操作人
     */
    private String operateUserName;

    /**
     * 操作时间
     */
    private Date operateTime;

    /**
     *  节点名
     */
    private String  nodeName;

    /**
     *  节点状态
     */
    private Integer  nodeState;
}
