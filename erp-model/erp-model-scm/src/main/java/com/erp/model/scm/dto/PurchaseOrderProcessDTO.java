package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 15:10
 */
@Data
@NoArgsConstructor
public class PurchaseOrderProcessDTO implements Serializable {

    /**
     * 操作节点
     */
    private String operation;

    /**
     * 操作人名称
     */
    private String userName;

    /**
     * 操作时间
     */
    private String time;

    /**
     * 是否到达节点
     */
    private Boolean isArrive;
}
