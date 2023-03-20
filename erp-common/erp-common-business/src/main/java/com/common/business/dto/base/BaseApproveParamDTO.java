package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 18:16
 */
@Data
@NoArgsConstructor
public class BaseApproveParamDTO {

    /**
     * 主键id集合
     */
    @NotEmpty(message = "请选择需要审核的数据")
    private List<String> ids;

    /**
     * 类型（pass、审核通过，reject、审核不通过）
     */
    @NotBlank(message = "审核类型不能为空")
    private String type;

    /**
     * 意见
     */
    private String comment;
}
