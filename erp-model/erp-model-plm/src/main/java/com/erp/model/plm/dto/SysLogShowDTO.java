package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @author Will
 * @version 1.0

 * @date 2022/12/5 20:39
 */
@Data
@NoArgsConstructor
public class SysLogShowDTO implements Serializable {


    /**
     * 操作
     */
    private String operation;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createUserName;

}
