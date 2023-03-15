package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 供应商联系人信息
 * @author Lambda
 * @Classname SupplierContactDTO
 * @Description TODO
 * @Date 2023-03-15 17:01
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierContactDTO implements Serializable {


    /**
     * 表id
     */
    private String id;


    /**
     * 联系人
     */
    private String person;

    /**
     * 职位
     */
    private String position;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 是否默认 true  是
     */
    private Boolean isDefault;

    /**
     * 开启状态 true 开启
     */
    private Boolean openStatus;

    /**
     * 备注信息
     */
    private String remark;
}
