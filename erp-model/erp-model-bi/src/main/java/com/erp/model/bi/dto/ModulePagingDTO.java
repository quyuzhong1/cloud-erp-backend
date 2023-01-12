package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ModulePagingDTO
 * @Description TODO
 * @Date 2022-12-12 11:27
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ModulePagingDTO implements Serializable {

    /**
     * 表id
     */
    private String id;

    /**
     * 模块名
     */
    private String name;

    /**
     * 系统模块名
     */
    private String sysModuleName;

    /**
     * 备注说明
     */
    private String remark;


    /**
     * 缩略图地址
     */
    private String imageUrl;

    /**
     * 状态
     */
    private Boolean state;


    /**
     * 本月使用次数
     */
    private Integer monthUsageCount = 0;


    /**
     * 使用次数
     */
    private Integer usageCount = 0;


}
