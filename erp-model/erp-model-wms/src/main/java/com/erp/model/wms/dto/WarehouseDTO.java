package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname WarehouseDTO
 * @Description TODO
 * @Date 2023-03-16 16:30
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WarehouseDTO implements Serializable {

    /**
     * 表id
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 仓库类型 对应dict 表id
     */
    private String typeId;


    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 负责人id
     */
    private String chargeId;

    /**
     * 负责人名
     */
    private String chargeName;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 联系人电话
     */
    private String contactTelNumber;

    /**
     * 状态
     *  true 启用
     *   false 未启用
     */
    private Boolean status;

    /**
     * 地址
     */
    private String address;

    /**
     * 组织id 对应 核算公司表id
     */
    private String orgId;

    /**
     * 组织名称
     */
    private String orgName;
}
