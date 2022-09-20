package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String id;

    private Integer scope;

    private String name;

    private Integer ifRequired;


    //字段类型 1：单选框 2 文本框 3：多选框 4 ： 日期  5：成员
    private Integer type;


    //1 启用  0 未启用
    private Boolean state;


    private String content;


    private String createUserId;

    private String createUserName;

    private Date createTime;

    private Date updateTime;

}
