package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname WorkflowBusinessProcessEntity
 * @Description TODO
 * @Date 2023-01-30 15:09
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_business_process")
public class WorkflowBusinessProcessEntity  implements Serializable {


    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private String create_user_id;

    /**
     * 流程id
     */
    private String processId;

    /**
     * 业务表id
     *  该 id 是具体的业务表的id
     */
    private String businessTableId;



    /**
     * 业务表id
     * workflow_business
     */
    private String businessId;
}
