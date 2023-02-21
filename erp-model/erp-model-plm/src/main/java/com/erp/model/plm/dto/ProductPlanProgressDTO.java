package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 13:38
 */
@Data
@NoArgsConstructor
public class ProductPlanProgressDTO implements Serializable {

    /**
     * 产品进度类型（新建规划、转产品开发、项目启动、项目完成）
     */
    private String typeName;

    /**
     * 人员姓名
     */
    private String userName;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 进度节点是否完成
     */
    private Boolean isComplete;
}
