package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * @Classname SaveTaskCommentDTO
 * @Description TODO
 * @Date 2022-10-13 17:54
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskCommentDTO implements Serializable {

    /**
     * 任务id
     */
    @NotBlank(message = "任务id 不能为空")
    private String taskId;


    /**
     * 评论内容
     */
    @NotBlank(message = "内容不能为空")
    @Size(max = 1000,message = "最大1000字符")
    private String comment;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人名
     */
    private Date createUserName;

}
