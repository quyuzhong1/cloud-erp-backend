package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/29 14:02
 */
@Data
@NoArgsConstructor
public class TaskChargeDistributionDTO  implements Serializable {

    private static final long serialVersionUID = 2405172041950251807L;

    /**
     * 分配类型（0角色，1人员，2上级人员负责人）
     */
    private Integer distributionType;

    /**
     * 人员储存id，角色储存名称，上级人员负责人储存枚举值（ChargeSuperiorEnum）
     */
    @NotBlank(message = "分配人员不能为空")
    private List<String> chargeList;

    /**
     * 分配人，逗号分隔
     */
    private String charges;

    /**
     * 分配人名称,逗号分割
     */
    private String chargeNames;

    /**
     * 负责人id,逗号分隔
     */
    private String chargeIds;
}
