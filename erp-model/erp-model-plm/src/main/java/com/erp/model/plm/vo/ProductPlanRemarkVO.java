package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 10:38
 */
@Data
@NoArgsConstructor
public class ProductPlanRemarkVO implements Serializable {

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private String createTime;
}
