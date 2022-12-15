package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Classname BiSubjectDefaultEntity
 * @Description TODO
 * @Date 2022-12-09 14:15
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bi_subject_default")
public class BiSubjectDefaultEntity implements Serializable {


    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 主题id
     */
    private String subjectId;


    /**
     * 主题id
     */
    private String userId;



    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDate createTime;


    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDate updateTime;

}
