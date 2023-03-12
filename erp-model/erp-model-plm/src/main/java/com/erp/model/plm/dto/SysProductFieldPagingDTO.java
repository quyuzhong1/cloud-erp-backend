package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname SysProductFieldPagingDTO
 * @Description TODO
 * @Date 2022-09-19 16:26
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysProductFieldPagingDTO {

    /**
     * 表id
     */
    private String id;

    /**
     * 使用范围 使用范围 1：全局  0： 单个
     */
    private Integer scope;

    /**
     * 字段名
     */
    private String name;

    /**
     * 是否必填 1：是 0 不是
     */
    private Integer ifRequired;



    /**
     * 字段类型 1：单选框 2 文本框 3：多选框 4 ： 日期  5：成员
     */
    private Integer type;



    /**
     * 是否启用 1 启用  0 未启用
     */
    private Boolean state;


    /**
     * 内容
     */
    private String content;


    /**
     * 创建人 id
     */
    private String createUserId;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
