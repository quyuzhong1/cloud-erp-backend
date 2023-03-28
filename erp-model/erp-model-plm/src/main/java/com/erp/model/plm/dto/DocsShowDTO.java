package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname DocsShowDTO
 * @Description TODO
 * @Date 2022-09-15 11:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DocsShowDTO  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String id;

    /**
     * 文档名
     */
    private String name;

    /**
     * 启动状态1 启用  0 禁用
     */
    private Boolean startState;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新人id
     */
    private String updateUserId;

    /**
     * 更新人
     */
    private String updateUser;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 使用次数
     */
    private Integer useCount;
}
