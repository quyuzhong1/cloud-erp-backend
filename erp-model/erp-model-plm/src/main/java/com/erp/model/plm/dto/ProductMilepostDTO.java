package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 产品里程碑DTO
 * @date 2022/11/18 15:30
 */
@Data
@NoArgsConstructor
public class ProductMilepostDTO implements Serializable {

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 里程碑名称
     */
    private String name;

    /**
     * 是否完成
     */
    private Integer isFinish;

    /**
     * 序号标识
     */
    private Integer seq;

    /**
     * 里程碑时间
     */
    private ProductMilepostDateDTO productMilepostDateDTO;
}
