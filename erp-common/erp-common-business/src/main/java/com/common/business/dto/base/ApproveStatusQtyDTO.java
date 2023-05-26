package com.common.business.dto.base;

import lombok.Data;

import java.io.Serializable;

/**
 * 审核状态和数量实体
 * @CreateTime: 2023-05-26  14:13
 * @Author: zhangchunlin
 */
@Data
public class ApproveStatusQtyDTO implements Serializable {

    /**
     * 审核状态
     */
    private String approveStatus;

    /**
     * 数量
     */
    private Integer count = 0;

}