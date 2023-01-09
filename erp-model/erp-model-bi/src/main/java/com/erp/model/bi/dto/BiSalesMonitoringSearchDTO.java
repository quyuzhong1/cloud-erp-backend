package com.erp.model.bi.dto;

import com.erp.common.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/5 10:08
 */
@Data
@NoArgsConstructor
public class BiSalesMonitoringSearchDTO extends PermissionsDTO {

    /**
     * 模块id
     */
    @NotBlank(message = "模块id不能为空")
    private String moduleId;

}
