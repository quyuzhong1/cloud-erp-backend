package com.erp.model.dmp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:54
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CfgApiFieldMapVO {

    /**
     * 本系统的字段
     */
    private String selfField;

    /**
     * 本系统的字段中文描述
     */
    private String selfFieldName;

    /**
     * 外部系统的字段（多层结构可逗号分割）
     */
    private String apiField;

    /**
     * 取值方式 ApiFieldType枚举
     */
    private Integer fieldType;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
