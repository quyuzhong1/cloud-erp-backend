package com.erp.model.plm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 产品研发团队DTO
 * @TableName product_rdt_team
 */
@Data
public class ProductRDTTeamDTO implements Serializable {

    /**
     * 主键id 无id：修改 有id：新增
     */
    private String id;

    /**
     * 研发团队名称
     */
    private String name;

    private static final long serialVersionUID = 1L;
}

