package com.erp.model.oms.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 客户分组实体返回
 *
 * @author zdy
 * @ClassName CustomerInfoVo
 * @description: TODO
 * @date 2023年10月08日
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class CustomerInfoVO implements Serializable {

    /**
     * 主键
     */
    private String id;
    /**
     * code
     */
    private String code;

    /**
     * 客户名称
     */
    private String name;

    /**
     * 分组id
     */
    private String groupId;

    /**
     * 分组名
     */
    private String groupName;

    /**
     * 客户属性
     */
    private String customerProperty;
}
