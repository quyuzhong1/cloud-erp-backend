package com.erp.model.bi.dto;

import com.erp.common.modules.validator.UpdateGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/27 15:49
 */
@Data
@NoArgsConstructor
public class ModuleSysConfigurationDTO implements Serializable {

    /**
     * 表id
     */
    @NotBlank(message = "id不能为空",  groups = {UpdateGroup.class} )
    private String id;

    /**
     * 父级id
     * 默认为0
     */
    private String pid;

    /**
     * 模块名称
     */
    @NotBlank(message = "模块名称不能为空")
    @Size(max = 30, message = "最大30字符")
    private String name;

    /**
     * 数据来源(0市场数据，1供应链数据，2经营数据，3财务数据)
     */
    @NotNull(message = "数据来源不能为空")
    private Integer dataSource;

    /**
     * 数据指标
     */
    @NotBlank(message = "数据指标不能为空")
    private String targetNames;

    /**
     * 数据维度（1年趋势，2季度趋势，3月趋势，4周趋势，5日趋势）
     */
    @NotNull(message = "数据维度不能为空")
    private Integer dataDimension;

}