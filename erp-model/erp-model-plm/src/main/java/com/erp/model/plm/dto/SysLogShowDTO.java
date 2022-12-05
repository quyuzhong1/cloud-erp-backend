package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 20:39
 */
@Data
@NoArgsConstructor
public class SysLogShowDTO implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * 类路径
     */
    private String classPath;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 业务id
     */
    private String businessId;

    /**
     * 旧值
     */
    private String oldValue;

    /**
     * 新值
     */
    private String newValue;

    /**
     * 内容
     */
    private String content;

}
