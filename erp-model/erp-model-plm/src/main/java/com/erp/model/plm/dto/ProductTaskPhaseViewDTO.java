package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 产品任务视图
 * @date 2022/11/22 18:09
 */
@Data
@NoArgsConstructor
public class ProductTaskPhaseViewDTO implements Serializable {

    /**
     * 阶段id不可用，阶段名称分组，seq用于前端固定上下级
     */
    private Integer seq;

    /**
     * 阶段名称
     */
    private String phaseName;

    /**
     * 计划开始日期
     */
    private Date planStartTime;

    /**
     * 计划结束日期
     */
    private Date planEndTime;

    /**
     * 子集
     */
    private List<ProductTaskPhaseChildDTO> childrenList;

}
