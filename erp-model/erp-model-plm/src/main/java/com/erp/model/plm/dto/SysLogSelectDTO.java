package com.erp.model.plm.dto;

import com.erp.common.dto.base.BaseSearchDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 21:26
 */
@Data
@NoArgsConstructor
public class SysLogSelectDTO extends BaseSearchDTO {

    /**
     * 表名
     */
    @NotBlank(message = "表名不能为空")
    private String className;

    /**
     * 业务id
     */
    @NotBlank(message = "业务id为空")
    private String businessId;

    /**
     * 内容
     */
    private String content;

}
